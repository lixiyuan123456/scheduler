# -*- coding: utf-8 -*-

import os,sys
src_path = os.path.dirname(os.path.abspath(__file__))

# mysql_host = '192.168.187.213'
# mysql_db = 'skynet'
# mysql_user = 'root'
# mysql_passwd = 'test@0905'

mysql_host = 'nxdpskynet.db.netlearning.tech'
mysql_db = 'dbnx_skynet'
mysql_port = 3364
mysql_user = 'skynet_rw'
mysql_passwd = '5d58a1f601356cea'

hive_mysql_host = 'skynet.db.netlearning.tech'
hive_mysql_db = 'dbnx_dphive'
hive_mysql_port = 3327
hive_mysql_user = 'skynet_rw'
hive_mysql_passwd = '280e23d50980f5b9'

# executor
tmp_scripts_path = './logs/tmp_scripts'    # shell脚本目录
upload_hdfs_path = '/nxScheduler/upload'   # hdfs存放job相关的运行文件
executor_env = '../etc/executor_env'

log_path = './cmdparser.log'  # 用于存放解释器执行的日志文件地址
log_level = 'DEBUG'

# spark path
spark_high_submit = '/opt/soft/zdp/spark-2.3.1-bin-2.7.5/bin'