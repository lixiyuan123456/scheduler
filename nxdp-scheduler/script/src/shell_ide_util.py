# -*- coding: utf-8 -*-

from datetime import timedelta, date, datetime
import sys, os, re,stat, json, subprocess, time, shlex
import operator

reload(sys)
sys.setdefaultencoding('utf8')

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)

import parser_configs
import log_util, mysql_util, constant
from interpreter_util import InterpreterBase

class ShellIdeInterpreter(InterpreterBase):

    def __init__(self):
        self.orders = {}
        self.mysql_util = mysql_util.MySqlUtil()
        self.replacement = {}

    # 对查询做变量替换
    def do_replacement(self, hql_order):
        for key in self.replacement.keys():
            hql_order = hql_order.replace(key, self.replacement[key])
        return hql_order

        # 预存要执行的命令
    def add_order(self, order_name, order):
        if self.orders.has_key(order_name):
            self.orders[order_name] += order
        else:
            self.orders[order_name] = order

    # 预存要执行的shell查询语句
    def add_hql(self, hql_order_name, hql_order):
        hql_order = self.do_replacement(hql_order)
        self.add_order(hql_order_name, hql_order)

    # 清空预存的命令集
    def clear(self):
        self.orders.clear()

    #'Interpreter of HIVE'
    def interpret(self, job_id, query_name, namespace, choose_time):
        update_command, scripts_command, other_args = self.parse_dispatch_command(job_id, choose_time)
        if update_command["code"] == 1:
            log_util.logger.error("[ShellIdeInterpreter] update dispatch_command error with return %s", update_command["desc"])
            update_command["desc"] = "[ShellIdeInterpreter] update dispatch_command error with return %s" % update_command["desc"]
            return json.dumps(update_command)

        prepare_params = self.prepare_interp_env(job_id)
        log_util.logger.debug("[ShellIdeInterpreter] %s", prepare_params)

        if prepare_params["code"] == 1:
            log_util.logger.debug("[ShellIdeInterpreter] error: %s", prepare_params["desc"])
            return json.dumps(prepare_params)

        queue_name = prepare_params["queue_name"]
        # dispatch_command = prepare_params["dispatch_command"]
        dispatch_command = scripts_command
        hadoop_user_name = prepare_params["hadoop_user_name"]

        # clauses = dispatch_command.strip().split("\n")
        # clauses = clauses[:-1] if (not clauses[-1]) or len(clauses[-1].strip()) == 0 else clauses
        # pattern_match = '\${[a-zA-Z_]\w*}'
        # pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+%\'\"-]*}'
        # # 替换普通变量
        # for clause in clauses:
        #     if len(clause.strip()) == 0:
        #         continue
        #     # 如果查询中使用了预定义变量，存下来；如果使用的变量既没定义也不是预定义变量，报错返回
        #     variables = re.findall(pattern_match, clause)
        #     for variable in variables:
        #         if variable in constant.CONSTANT.keys():
        #             self.replacement[variable] = constant.CONSTANT[variable](choose_time)
        #         if variable not in self.replacement.keys():
        #             return json.dumps({"code": 1, "query_name": query_name, "desc": "variable %s is not defined." % variable})
        #     self.add_hql(query_name, clause + '\n')
        # final_result = self.orders[query_name]
        # self.orders.clear()
        # # 替换bash变量, 再遍历一遍
        # clauses = final_result.strip().split("\n")
        # clauses = clauses[:-1] if (not clauses[-1]) or len(clauses[-1].strip()) == 0 else clauses
        # for clause in clauses:
        #     if len(clause.strip()) == 0:
        #         continue
        #     variables_bash = re.findall(pattern_match_bash, clause)
        #     for variable_bash in variables_bash:
        #         log_util.logger.debug("[ShellIdeInterpreter] $bash variable %s" % variable_bash)
        #         if variable_bash.replace(" ", "").startswith("$bash{date"):
        #             self.replacement[variable_bash] = constant.CONSTANT["$bash{"](variable_bash)
        #     self.add_hql(query_name, clause + '\n')
        # final_result = self.orders[query_name]
        log_util.logger.debug("[ShellIdeInterpreter] scripts : %s", dispatch_command)

        tmp_script_folder_path = os.path.join(parser_configs.tmp_scripts_path, query_name)
        log_util.logger.debug("[ShellIdeInterpreter] %s" % tmp_script_folder_path)
        if not os.path.isdir(tmp_script_folder_path):
            os.makedirs(tmp_script_folder_path)
        os.chmod(tmp_script_folder_path, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)

        command_path = '''%s/%s.command''' % (tmp_script_folder_path, query_name)
        scripts_path = '''%s/%s.bash''' % (tmp_script_folder_path, query_name)
        tmp_script_path = '''%s/%s.sh''' % (tmp_script_folder_path, query_name)
        pid_path = '''%s/%s.pid''' % (tmp_script_folder_path, query_name)
        self.writeFile(scripts_path, "%s" % dispatch_command, None)

        header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
'''
        env = '''source %s \nexport HADOOP_USER_NAME=%s\n ''' % (parser_configs.executor_env, hadoop_user_name)
        # env = '''source %s \n''' % (parser_configs.executor_env)
        self.writeFile(command_path, [header, env, "sh %s %s" % (scripts_path, other_args)], None)

        sh_header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
echo $$ > %s
''' % pid_path
        sh_footer = '''
'''
        self.writeFile(tmp_script_path, [env, sh_header, "sh %s " % command_path, sh_footer, ], None)

        cmd = '''sh %s''' % tmp_script_path

        # 清空预存的命令集
        self.clear()

        log_util.logger.debug("[ShellIdeInterpreter] cmd: %s", cmd)
        return json.dumps({"code": 0, "cmd": cmd, "query_name": query_name})

    def parse_dispatch_command(self, job_id, choose_time):
        details = self.mysql_util.get_details_by_id(job_id)
        if details["code"] == 1:
            log_util.logger.error("[ShellIdeInterpreter] %s", details["desc"])
            return details
        # 统一单引号为双引号 for mysql
        json_str = json.loads(details["desc"])
        sp = json_str["scripts"].replace('\'', '\"')
        scripts_command = json_str["scripts"]
        other_args = self.replace_variable(json_str["otherArgs"], choose_time)

        result = self.mysql_util.update_dispatch_command_by_id(job_id, sp, '')
        return result, scripts_command, other_args

    def replace_variable(self, arg_str, choose_time):
        pattern_match = '\${[a-zA-Z_]\w*}'
        variable_map = {}
        variables = re.findall(pattern_match, arg_str)
        for variable in variables:
            if variable in constant.CONSTANT.keys():
                variable_map[variable] = constant.CONSTANT[variable](choose_time)
                # if variable not in self.variable_map.keys():
                #     return json.dumps({"code": 1, "query_name": query_name, "desc": "variable %s is not defined." % variable})
        for key in variable_map.keys():
            arg_str = arg_str.replace(key, variable_map[key])
        pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+%\'\"-]*}'
        bash_map = {}
        bashs = re.findall(pattern_match_bash, arg_str)
        for variable_bash in bashs:
            if variable_bash.replace(" ", "").startswith("$bash{date"):
                bash_map[variable_bash] = constant.CONSTANT["$bash{"](variable_bash)
        for key in bash_map.keys():
            arg_str = arg_str.replace(key, bash_map[key])
        return arg_str

if __name__ == "__main__":
    scripts = ShellIdeInterpreter()
    # print hive.parse_dispatch_command(34316)
    print scripts.interpret(34606, "2335", "2018-06-26", "2018-06-26 19:53:54")









