# -*- coding: utf-8 -*-

import os,sys
src_path = os.path.dirname(os.path.abspath(__file__))

mysql_host = '10.48.186.32'
mysql_db = 'skynet'
mysql_user = 'root'
mysql_passwd = 'UDP@mj505'

hive_mysql_host = 'skynet.db.zhuaninc.com'
hive_mysql_db = 'dbzz_dphive'
hive_mysql_port = 3327
hive_mysql_user = 'skynet_rw'
hive_mysql_passwd = '280e23d50980f5b9'

# executor
tmp_scripts_path = '/opt/log/zzdp/log/tmp_scripts'
#tmp_scripts_path = '/home/work/sunzhiwei/log/tmp_scripts'
upload_hdfs_path = '/zzScheduler/upload'
executor_env = '/opt/soft/scheduler_bin/script/etc/executor_env'
#executor_env = '/home/work/sunzhiwei/scheduler_bin/script/etc/executor_env'

log_path = '/opt/log/zzdp/log/parser.log'  # 用于存放解释器执行的日志文件地址
#log_path = '/home/work/sunzhiwei/log/cmdparser.log'  # 用于存放解释器执行的日志文件地址
log_level = 'DEBUG'

spark_high_submit = '/opt/soft/zdp/spark-2.3.1-bin-2.7.5/bin'
