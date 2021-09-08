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

class HiveInterpreter(InterpreterBase):

    def __init__(self):
        self.orders = {}
        self.mysql_util = mysql_util.MySqlUtil()
        self.replacement = {}

    # 对查询做变量替换
    def do_replacement(self, hql_order):
        for key in self.replacement.keys():
            # hql_order = re.sub(key.replace('$', '\\$'), self.replacement[key], hql_order)
            hql_order = hql_order.replace(key, self.replacement[key])
        return hql_order

        # 预存要执行的命令
    def add_order(self, order_name, order):
        if self.orders.has_key(order_name):
            self.orders[order_name] += order
        else:
            self.orders[order_name] = order

    # 预存要执行的hive查询语句
    def add_hql(self, hql_order_name, hql_order):
        hql_order = self.do_replacement(hql_order)
        self.add_order(hql_order_name, hql_order)

    # 清空预存的命令集
    def clear(self):
        self.orders.clear()

    #'Interpreter of HIVE'
    def interpret(self, job_id, query_name, namespace, choose_time):
        update_command, sql = self.parse_dispatch_command(job_id)
        if update_command["code"] == 1:
            log_util.logger.error("[HiveInterpreter] update dispatch_command error with return %s", update_command["desc"])
            update_command["desc"] = "[HiveInterpreter] update dispatch_command error with return %s" % update_command["desc"]
            return json.dumps(update_command)

        prepare_params = self.prepare_interp_env(job_id)
        log_util.logger.debug("[HiveInterpreter] %s", prepare_params)

        if prepare_params["code"] == 1:
            log_util.logger.debug("[HiveInterpreter] error: %s", prepare_params["desc"])
            return json.dumps(prepare_params)

        queue_name = prepare_params["queue_name"]
        # dispatch_command = prepare_params["dispatch_command"]
        dispatch_command = sql
        hadoop_user_name = prepare_params["hadoop_user_name"]
        user_name = prepare_params["user_name"]
        job_name = prepare_params["job_name"]

#         queue_name = "queue1"
#         dispatch_command = '''

        clauses = dispatch_command.strip().split(";")
        clauses = clauses[:-1] if (not clauses[-1]) or len(clauses[-1].strip()) == 0 else clauses
        pattern_match = '\${[a-zA-Z_]\w*}'
        pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+%\'\"-]*}'
        # 替换普通变量
        for clause in clauses:
            if len(clause.strip()) == 0:
                continue
            # 如果查询中使用了预定义变量，存下来；如果使用的变量既没定义也不是预定义变量，报错返回
            variables = re.findall(pattern_match, clause)
            for variable in variables:
                if variable in constant.CONSTANT.keys():
                    self.replacement[variable] = constant.CONSTANT[variable](choose_time)
                if variable not in self.replacement.keys():
                    return json.dumps({"code": 1, "query_name": query_name, "desc": "variable %s is not defined." % variable})
            self.add_hql(query_name, clause + ';')
        final_result = self.orders[query_name]
        self.orders.clear()
        # 替换bash变量, 再遍历一遍
        clauses = final_result.strip().split(";")
        clauses = clauses[:-1] if (not clauses[-1]) or len(clauses[-1].strip()) == 0 else clauses
        for clause in clauses:
            if len(clause.strip()) == 0:
                continue
            variables_bash = re.findall(pattern_match_bash, clause)
            for variable_bash in variables_bash:
                log_util.logger.debug("[HiveInterpreter] $bash variable %s" % variable_bash)
                if variable_bash.replace(" ", "").startswith("$bash{date"):
                    self.replacement[variable_bash] = constant.CONSTANT["$bash{"](variable_bash)
            self.add_hql(query_name, clause + ';')
        final_result = self.orders[query_name]
        log_util.logger.debug("[HiveInterpreter] hql: %s", final_result)

        tmp_script_folder_path = os.path.join(parser_configs.tmp_scripts_path, query_name)
        log_util.logger.debug("[HiveInterpreter] %s" % tmp_script_folder_path)
        if not os.path.isdir(tmp_script_folder_path):
            os.makedirs(tmp_script_folder_path)
        os.chmod(tmp_script_folder_path, stat.S_IRWXU|stat.S_IRWXG|stat.S_IRWXO)

        command_path = '''%s/%s.command''' % (tmp_script_folder_path, query_name)
        hql_path = '''%s/%s.hql''' % (tmp_script_folder_path, query_name)
        tmp_script_path = '''%s/%s.sh''' % (tmp_script_folder_path, query_name)
        pid_path = '''%s/%s.pid''' % (tmp_script_folder_path, query_name)
        self.writeFile(hql_path, "%s" % final_result, None)

        header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
'''
        env = '''source %s \nexport HADOOP_USER_NAME=%s\n ''' % (parser_configs.executor_env, hadoop_user_name)
        # env = '''source %s \n''' % (parser_configs.executor_env)
        # self.writeFile(command_path, [header, env,
        #         "hive -hiveconf hive.cli.print.header=true -hiveconf mapreduce.job.queuename=%s -f %s" % (queue_name, hql_path)], None)

        self.writeFile(command_path, [header, env,
                  "hive -hiveconf hive.cli.print.header=true -hiveconf mapreduce.job.queuename=" + queue_name +
                  " -hiveconf zzdp.mapreduce.job.name=" + job_name + " -hiveconf zzdp.mapreduce.job.user=" + user_name +
                  " -hiveconf zzdp.mapreduce.job.id=" + query_name + " -f " + hql_path], None)

        sh_header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
echo $$ > %s
''' % pid_path
        sh_footer = '''
'''
        self.writeFile(tmp_script_path, [env,sh_header, "sh %s " % command_path , sh_footer, ], None)

        cmd = '''sh %s''' % (tmp_script_path)

        # 清空预存的命令集
        self.clear()

        log_util.logger.debug("[HiveInterpreter] cmd: %s", cmd)
        return json.dumps({"code": 0, "cmd": cmd, "query_name": query_name})

    def parse_dispatch_command(self, job_id):
        details = self.mysql_util.get_details_by_id(job_id)
        if details["code"] == 1:
            log_util.logger.error("[HiveInterpreter] %s", details["desc"])
            return details
        # 统一单引号为双引号 for mysql
        sql = json.loads(details["desc"])["sql"].replace('\'', '\"')
        sql_command = json.loads(details["desc"])["sql"]
        result = self.mysql_util.update_dispatch_command_by_id(job_id, sql, '')
        return result, sql_command

if __name__ == "__main__":
    hive = HiveInterpreter()
    # print hive.parse_dispatch_command(34316)
    print hive.interpret(34577, "2335", "2018-06-26", "2018-06-26 19:53:54")









