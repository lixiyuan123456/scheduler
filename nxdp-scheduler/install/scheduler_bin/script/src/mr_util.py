# -*- coding: utf-8 -*-

import os, sys,stat, re, json

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)

import constant
import log_util, parser_configs
from interpreter_util import InterpreterBase

# 只需要过滤的参数列表(只过滤参数不过滤值)
filter_List = ["--main_jar", "--main_class", "--input_path", "--output_path", "--other_args"]

class MapReduceInterpreter(InterpreterBase):

    # 'Interpreter of MapReduce'
    # query_name is executor id
    def interpret(self, job_id, query_name, namespace, choose_time):
        update_command = self.parse_dispatch_command(job_id, choose_time)
        if update_command["code"] == 1:
            log_util.logger.error("[MapReduceInterpreter] update dispatch_command error with return %s", update_command["desc"])
            update_command["desc"] = "[MapReduceInterpreter] update dispatch_command error with return %s" % update_command["desc"]
            return json.dumps(update_command)

        prepare_params = self.prepare_interp_env(job_id)
        log_util.logger.debug("[MapReduceInterpreter] %s", prepare_params)

        if prepare_params["code"] == 1:
            log_util.logger.debug("[MapReduceInterpreter] error: %s", prepare_params["desc"])
            return json.dumps(prepare_params)

        # 拼接mr命令
        queue_name = prepare_params["queue_name"]
        dispatch_command = prepare_params["dispatch_command"]
        jar_path = prepare_params["jar_path"]
        hadoop_user_name = prepare_params["hadoop_user_name"]
        user_name = prepare_params["user_name"]
        job_name = prepare_params["job_name"]

        # 将语句做一些简单的处理
        code, dispatch_command = self.handler_order(dispatch_command, queue_name, user_name, job_name, query_name)
        if code==1:
            # 语法检测失败，直接返回
            return json.dumps({"code": 1,"desc": dispatch_command})
        tmp_script_folder_path = os.path.join(parser_configs.tmp_scripts_path, query_name)
        log_util.logger.debug("[MapReduceInterpreter] tmp_script_folder_path: %s", tmp_script_folder_path)

        if not os.path.isdir(tmp_script_folder_path):
            os.makedirs(tmp_script_folder_path)
        os.chmod(tmp_script_folder_path, stat.S_IRWXU|stat.S_IRWXG|stat.S_IRWXO)

        log_util.logger.debug("*******************************get to local start")
        # get the command of get job file form hdfs
        get2local_str = self.get_to_local_str(tmp_script_folder_path, jar_path)
        log_util.logger.debug("[MapReduceInterpreter] get_to_local: %s", get2local_str)
        log_util.logger.debug("*******************************get to local stop")

        tmp_script_path = os.path.join(tmp_script_folder_path, "%s.sh" % query_name)
        command_path = os.path.join(tmp_script_folder_path, "%s.command" % query_name)
        header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
'''
        env = '''source %s \nexport HADOOP_USER_NAME=%s\n ''' % (parser_configs.executor_env, hadoop_user_name)
        # env = '''source %s \n''' % (parser_configs.executor_env )
        self.writeFile(command_path, [header, env, get2local_str, dispatch_command, ], None)
        log_util.logger.debug("[MapReduceInterpreter] scripts path: %s", command_path)
        pid_path = '''%s/%s.pid''' % (tmp_script_folder_path, query_name)

        sh_header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
echo $$ > %s
''' % pid_path

        sh_footer = '''
'''
        self.writeFile(tmp_script_path, [sh_header, "sh %s " % command_path , sh_footer, ], None)

        cmd = '''sh %s''' % (tmp_script_path)

        log_util.logger.debug("[MapReduceInterpreter] cmd: %s", cmd)
        return json.dumps({"code": 0, "cmd": cmd, "query_name": query_name})

    # MapReduce语句处理函数
    def handler_order(self, dispatch_command, queue_name, user_name, job_name, query_name):
        code = 0
        # 语法检测，所有的参数必须以--开头
        dispatch_command = dispatch_command.strip()
        for line in dispatch_command.split("\n"):
            if line:
                match_obj = re.match(r'--', line.strip(), re.M|re.I)
                if not match_obj:
                    code = 1
                    dispatch_command = "语法错误:参数必须使用--开头。"
                    break
        # 替换所有的回车换行符
        dispatch_command = dispatch_command.replace("\n", " ").replace("\\", " ")
        # 过滤所有的参数(只是过滤参数，不过滤对应的值)
        for args in filter_List:
            args_match = re.search(r'%s' % args, dispatch_command, re.M | re.I)
            if args_match:
                agr = args_match.group()
                if agr == '--input_path':
                    dispatch_command = dispatch_command.replace(agr,
                            " -Dmapreduce.job.queuename=" + queue_name + " -Dzzdp.mapreduce.job.name=" + job_name +
                            " -Dzzdp.mapreduce.job.user=" + user_name + " -Dzzdp.mapreduce.job.id=" + query_name)
                else:
                    #替换所有的参数
                    dispatch_command = dispatch_command.replace(agr, "")

        dispatch_command = "hadoop jar " + dispatch_command
        log_util.logger.debug("MapReduce ORDER:%s" % dispatch_command)
        return code, dispatch_command

    def __verfication_config(self,config):
        if not config or config == '':
            log_util.logger.error('Empty input config')
            return ''

        sub_pattern = re.compile(r'\s*=\s*')
        pattern = re.compile(r'([\w\-]+\.){1,}[\w\-]+\s*=\s*((?!=)\S)+')
        match = pattern.match(config)
        if not match or match.group() != config:
            log_util.logger.error('invaild input config: ' + config)
            return ''
        else:
            log_util.logger.debug('added config ' + " -D" + sub_pattern.sub('=',config))
            return " -D" + sub_pattern.sub('=',config)

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

    def parse_dispatch_command(self, job_id, choose_time):
        details = self.mysql_util.get_details_by_id(job_id)
        if details["code"] == 1:
            log_util.logger.error("[MapReduceInterpreter] %s", details["desc"])
            return details
        json_str = json.loads(details["desc"])
        jar_path = json_str["filePath"]
        main_jar = json_str["filePath"].split('/')[-1]
        main_class = json_str["jobPackage"]
        input_path = self.replace_variable(json_str["inputPath"], choose_time)
        output_path = self.replace_variable(json_str["outputPath"], choose_time)
        other_args = self.replace_variable(json_str["otherArgs"], choose_time)
        # 是否需要换成json，方便后续取值
        command = '''
--main_jar %s \n
--main_class %s \n
--input_path %s \n
--output_path %s \n
--other_args %s
''' % (main_jar, main_class, input_path, output_path, other_args)
        result = self.mysql_util.update_dispatch_command_by_id(job_id, command, jar_path)
        return result

if __name__ == "__main__":
    MapReduce_test = MapReduceInterpreter()
    # print MapReduce_test.interpret(34494, "34494_1394", '2018-03-27', '2018-03-27')
    order = '--main_jar secondarysort.jar \n --main_class com.szw.test.Test \n --input_path /user/hadoop/README /user/hadoop/o\n --output_path mapreduce.input.fileinputformat.split.maxsize=1\n --other_args mapreduce.input.fileinputformat.split.maxsize=2'
    # order=" --mainjar secondarysort.jar \n" \
    #       +" --mainclass com.szw.test.Test \n" \
    #       +" --mainargs /user/hadoop/README /user/hadoop/o\n" \
    #       +" --param mapreduce.input.fileinputformat.split.maxsize=1\n" \
    #       +" --param mapreduce.input.fileinputformat.split.maxsize=2"
    code,order = MapReduce_test.handler_order(order,"root.offline.algo")
    print code
    print order

