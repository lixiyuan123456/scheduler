# -*- coding: utf-8 -*-

import os, sys,stat, re, json

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)

import log_util, constant, parser_configs
from interpreter_util import InterpreterBase

#只需要过滤的参数列表(只过滤参数不过滤值)
filter_List = ["--spark_other_args", "--main_jar", "--main_class_args"]

class SparkStreamingInterpreter(InterpreterBase):

    # 'Interpreter of SPARK Streaming'
    def interpret(self, job_id, query_name, namespace, choose_time):
        update_command,spark_version= self.parse_dispatch_command(job_id, choose_time)
        if update_command["code"] == 1:
            log_util.logger.error("[SparkStreamingInterpreter] update dispatch_command error with return %s", update_command["desc"])
            update_command["desc"] = "[SparkStreamingInterpreter] update dispatch_command error with return %s" % update_command["desc"]
            return json.dumps(update_command)

        prepare_params = self.prepare_interp_env(job_id)
        log_util.logger.debug("[SparkStreamingInterpreter] %s", prepare_params)

        if prepare_params["code"] == 1:
            log_util.logger.debug("[SparkStreamingInterpreter] error: %s", prepare_params["desc"])
            return json.dumps(prepare_params)

        queue_name = prepare_params["queue_name"]
        dispatch_command = prepare_params["dispatch_command"]
        jar_path = prepare_params["jar_path"]
        hadoop_user_name = prepare_params["hadoop_user_name"]
        user_name = prepare_params["user_name"]
        job_name = prepare_params["job_name"]
        spark_yarn_app_name = query_name + "_" + user_name + "_" + job_name

        # 将语句做一些简单的处理
        code, dispatch_command = self.handler_order(dispatch_command, queue_name,spark_version)
        if code == 1:
            # 语法检测失败，直接返回
            return json.dumps({"code":1,"desc":dispatch_command})
        tmp_script_folder_path = os.path.join(parser_configs.tmp_scripts_path, query_name)
        log_util.logger.debug("[SparkStreamingInterpreter] tmp_script_folder_path: %s", tmp_script_folder_path)

        if not os.path.isdir(tmp_script_folder_path):
            os.makedirs(tmp_script_folder_path)
        os.chmod(tmp_script_folder_path,stat.S_IRWXU|stat.S_IRWXG|stat.S_IRWXO)

        log_util.logger.debug("*******************************get to local start")
        get2local_str = self.get_to_local_str(tmp_script_folder_path, jar_path)
        log_util.logger.debug("[SparkStreamingInterpreter] get_to_local: %s", get2local_str)
        log_util.logger.debug("*******************************get to local stop")

        tmp_script_path = os.path.join(tmp_script_folder_path, "%s.sh" % query_name)
        command_path = os.path.join(tmp_script_folder_path, "%s.command" % query_name)

        header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
'''
        env = '''source %s \nexport HADOOP_USER_NAME=%s\nexport SPARK_YARN_APP_NAME=%s\n''' % (parser_configs.executor_env, hadoop_user_name, spark_yarn_app_name)
        # env = '''source %s \n''' % parser_configs.executor_env
        self.writeFile(command_path, [header, env, get2local_str, dispatch_command, ], None)
        log_util.logger.debug("[SparkStreamingInterpreter] scripts path: %s", tmp_script_path)
        pid_path = '''%s/%s.pid''' % (tmp_script_folder_path, query_name)

        sh_header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
echo $$ > %s
''' % (pid_path)

        sh_footer = '''
'''
        self.writeFile(tmp_script_path, [sh_header, "sh %s " % command_path , sh_footer, ], None)

        cmd = '''sh %s''' % (tmp_script_path)
        return json.dumps({"code": 0, "cmd": cmd, "query_name": query_name})

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
                # 变量替换有没有重复的问题
                bash_map[variable_bash] = constant.CONSTANT["$bash{"](variable_bash)
        for key in bash_map.keys():
            arg_str = arg_str.replace(key, bash_map[key])
        return arg_str

    def parse_dispatch_command(self, job_id, choose_time):
        details = self.mysql_util.get_details_by_id(job_id)
        if details["code"] == 1:
            log_util.logger.error("[SparkStreamingInterpreter] %s", details["desc"])
            return details
        json_str = json.loads(details["desc"])
        jar_path = json_str["filePath"]
        main_jar = json_str["filePath"].split('/')[-1]
        main_class = json_str["jobPackage"]
        spark_master = json_str["sparkMaster"]
        num_executors = json_str["numExecutors"]
        executor_cores = json_str["executorCores"]
        executor_memory = json_str["executorMemory"]
        spark_other_args = json_str["sparkOtherArgs"]
        spark_version=json_str["sparkVersion"]
        main_class_args = self.replace_variable(json_str["mainClassArgs"], choose_time)
        command = '''
--class %s \n
--master %s \n
--num-executors %s \n
--executor-cores %s \n
--executor-memory %sg \n
--spark_other_args %s \n
--main_jar %s \n
--main_class_args %s
''' % (main_class, spark_master, num_executors, executor_cores, executor_memory, spark_other_args, main_jar, main_class_args)
        result = self.mysql_util.update_dispatch_command_by_id(job_id, command, jar_path)
        return result,spark_version

    def handler_order(self, dispatch_command, queue_name,spark_version):
        code = 0
        # 语法检测，所有的参数必须以--开头
        dispatch_command = dispatch_command.strip()
        for line in dispatch_command.split("\n"):
            if line:
                matchObj = re.match(r'--',line.strip(),re.M|re.I)
                if not matchObj:
                    code = 1
                    dispatch_command = "语法错误: %s 参数必须使用--开头。" % line
                    break
        # 替换所有的回车换行符
        dispatch_command = dispatch_command.replace("\n", " ").replace("\\", " ")
        # 过滤所有的参数(只是过滤参数，不过滤对应的值)
        for args in filter_List:
            args_match = re.search(r'%s' % args, dispatch_command, re.M | re.I)
            if args_match:
                agr = args_match.group()
                # 替换所有的参数
                dispatch_command = dispatch_command.replace(agr, "")
        # 获取用户组。即资源提交的队列
        # dispatch_command = "%s --queue %s  %s" % ("spark-submit", queue_name, dispatch_command)
        if spark_version == '2.3.1':
            dispatch_command = "%s --queue %s  %s" % (parser_configs.spark_high_submit+"/spark-submit", queue_name, dispatch_command)
        else:
            dispatch_command = "%s --queue %s  %s" % ("spark-submit", queue_name, dispatch_command)
        return code, dispatch_command

if __name__ == "__main__":
    spark = SparkStreamingInterpreter()
    # spark.parse_dispatch_command(5)
    print spark.interpret(5, "5_1245", "2017-02-09")
    # s = "/Users/wangyu/Downloads/files/jar/GoodsSimilarityOptimize2_1517391561752.jar"
    # print s[:s.rfind("/")]
    # order="--class org.apache.spark.examples.SparkPi \n" \
    #       +"--master yarn-cluster\n" \
    #       +"--num-executors 3\n" \
    #       +"--driver-memory 4g\n" \
    #       +"--executor-memory 2g\n" \
    #       +"--executor-cores 1\n" \
    #       +"--queue thequeue\n" \
    #       +"--mainjar lib/spark-examples*.jar \n" \
    #       +"--mainargs 21 1953 /user/bre/dtf/latend_model_pre/cate_level=1 /user/bre/dtf/predict_mapping_test/predict_mapping_test_data.txt /user/bre/dtf/latend_result_out_test2/l_date=$BDMS_YESTERDAY/ /user/bre/latend_data/first_cate_mapping.txt\n    "
    # code,order = spark.handler_order(order,"hadoop")
    # print order
