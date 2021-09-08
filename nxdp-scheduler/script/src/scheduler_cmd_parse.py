# -*- coding: utf-8 -*-

import sys, json, os

src_path = os.path.dirname(os.path.abspath(__file__))
sys.path.append(src_path + "/..")

etc_path = src_path + "/../etc"
sys.path.append(etc_path)
import parser_configs

import log_util, mysql_util
from interpreter_util import InterpreterBase
from hive_util import HiveInterpreter
from shell_util import ShellInterpreter
from spark_util import SparkInterpreter
from spark_streaming_util import SparkStreamingInterpreter
from mr_util import MapReduceInterpreter
from sqoop_utils import SqoopInterpreter
from shell_ide_util import ShellIdeInterpreter



class CmdParse(InterpreterBase):
    def __init__(self):
        self.mysql_util = mysql_util.MySqlUtil()

    def cmdParse(self, json_str, current=None):
        params = json.loads(json_str)

        job_id = params['jobId']
        namespace = params['namespace']
        query_name = params['queryName']
        choose_time = params['chooseTime']

        interpreter = None
        script_type = self.mysql_util.get_scripttype_by_jobId(job_id)

        if script_type["code"] == 1:
            return json.dumps(script_type)

        if script_type["desc"].upper() == "HIVE":
            interpreter = HiveInterpreter()
        elif script_type["desc"].upper() == "SHELL":
            interpreter = ShellInterpreter()
        elif script_type["desc"].upper() == "SPARK":
            interpreter = SparkInterpreter()
        elif script_type["desc"].upper() == "SPARK_STREAMING":
            interpreter = SparkStreamingInterpreter()
        elif script_type["desc"].upper() == "MAPREDUCE":
            interpreter = MapReduceInterpreter()
        elif script_type["desc"].upper() == "SQOOP":
            interpreter = SqoopInterpreter()
        elif script_type["desc"].upper() == "SHELL_IDE":
            interpreter = ShellIdeInterpreter()
        else:
            return json.dumps({"code": 1, "desc": "we haven't provided this job type for now. we'll support it later."})
        log_util.logger.debug("[CmdParse] jobId - %s and qureyName - %s is %s" % (job_id, query_name, script_type["desc"]))
        return interpreter.interpret(job_id, query_name, namespace, choose_time)
        # return interpreter.interpret(job_id, query_name, namespace)


if __name__ == "__main__":
    # param_test = {}
    # param_test["jobId"] = 4
    # param_test["namespace"] = "2018-01-30"
    # param_test = json.dumps(param_test)
    # cmdP = CmdParse()
    # res = cmdP.cmdParse(param_test, current=None)
    # {"jobId":"5","namespace":"2018-02-24","queryName":"773"}
    cmdP = CmdParse()
    # log_util.logger.debug("[CmdParse] argv is " + sys.argv[1])
    res = cmdP.cmdParse(sys.argv[1], current=None)
    # res = cmdP.cmdParse('{"jobId":"34613","namespace":"2018-11-08","queryName":"9344","chooseTime":"2018-11-08 15:10:41"}', current=None)
    # res = {"cmd": "sh /home/work/sunzhiwei/log/tmp_scripts/test/test.sh", "code": 0, "query_name": "test"}
    print res