# -*- coding: utf-8 -*-

import os, sys, re, json, subprocess, stat,time
from datetime import timedelta, date, datetime
import shutil

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)
import parser_configs, log_util, mysql_util

class InterpreterBase(object):

    'Base class of interpreter'

    def __init__(self):
        self.mysql_util = mysql_util.MySqlUtil()

    # 获取解释器的执行环境参数
    def prepare_interp_env(self, job_id):

        params = self.mysql_util.get_executor_info(job_id)
        if params["code"] == 1:
            log_util.logger.error(params["desc"])
            return params

        params = params["desc"]
        log_util.logger.debug("[INTERPRET parameters] %s", params)

        user_name = params['user_name']
        dispatch_command = params['dispatch_command']
        queue_name = params['queue_name']
        jar_path = params['jar_path']
        hadoop_user_name = params['hadoop_user_name']
        job_name = params['job_name']

        result = dict(code=0, job_id=job_id, user_name=user_name, dispatch_command=dispatch_command,
                      queue_name=queue_name, jar_path=jar_path, hadoop_user_name=hadoop_user_name, job_name=job_name)
        # res.update(queue_name)
        return result

    def get_to_local_str(self, localfolder, hdfs_path, exec_user="work"):

        if os.path.exists(localfolder):
            if not os.path.isdir(localfolder):
                result = {"code": 1, "desc": "localfolder is not a dir"}
                return json.dumps(result)
        else :
            os.makedirs(localfolder)
        # 拼出来可执行的HADOOP get语句
        get_hdfs_str = '''
hadoop fs -test -e %s
if [ $? -eq 0 ]; then
    for((i=1;i<=3;i++))
    do
        hadoop fs -get %s %s
        if [ $? -eq 0 ]; then
            break
        fi
    done
fi

''' % (hdfs_path, hdfs_path, localfolder)
        return get_hdfs_str

    def writeFile(self, filepath, contentList, client= None):
        path = os.path.basename(filepath)
        folder = os.path.dirname(filepath)
        log_util.logger.debug("[INTERPRET] folder:%s,path:%s"%(folder,path))
        log_util.logger.debug("[INTERPRET] clinet:%s"%(client))

        if not os.path.isdir(folder):
            try:
                os.makedirs(folder)
            except OSError, why :
                return False
        os.chmod(folder, stat.S_IRWXU|stat.S_IRWXG|stat.S_IRWXO)

        tmp_script = open(filepath, 'w')

        for content in contentList:
            tmp_script.write(content)

        tmp_script.close()

        os.chmod(filepath, stat.S_IRWXU)
        return True

if __name__ == "__main__":
    pass