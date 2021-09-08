# -*- coding: utf-8 -*-

import sys, os, re, json
import stat
from datetime import timedelta, date, datetime

reload(sys)
sys.setdefaultencoding('utf8')

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)

import log_util, constant, parser_configs
import zz_crypt, hive_meta_sql
from interpreter_util import InterpreterBase

# 只需要过滤的参数列表(只过滤参数不过滤值)
filter_List = ["--mysql-to-hive"]

# hive 预留关键字列表
key_list = ["date", "timestamp"]

class SqoopInterpreter(InterpreterBase):

    # 'Interpreter of Sqoop'
    def interpret(self, job_id, query_name, namespace, choose_time):
        update_command, hive_command = self.parse_dispatch_command(job_id, choose_time)
        if update_command["code"] == 1:
            log_util.logger.error("[SqoopInterpreter:interpret] update dispatch_command error with return %s", update_command["desc"])
            update_command["desc"] = "[SqoopInterpreter:interpret] update dispatch_command error with return %s" % update_command["desc"]
            return json.dumps(update_command)

        prepare_params = self.prepare_interp_env(job_id)
        log_util.logger.debug("[SqoopInterpreter:interpret] %s", prepare_params)

        if prepare_params["code"] == 1:
            log_util.logger.debug("[SqoopInterpreter:interpret] error: %s", prepare_params["desc"])
            return json.dumps(prepare_params)

        queue_name = prepare_params["queue_name"]
        dispatch_command = prepare_params["dispatch_command"]
        jar_path = prepare_params["jar_path"]
        hadoop_user_name = prepare_params["hadoop_user_name"]
        user_name = prepare_params["user_name"]
        job_name = prepare_params["job_name"]

        # 命令中添加queue_name
        code, dispatch_command = self.handler_order(dispatch_command, queue_name, job_name, user_name, query_name)
        if code == 1:
            # 语法检测失败，直接返回
            return json.dumps({"code": 1, "desc": dispatch_command})

        tmp_script_folder_path = os.path.join(parser_configs.tmp_scripts_path, query_name)
        log_util.logger.debug("[SqoopInterpreter:interpret] tmp_script_folder_path: %s", tmp_script_folder_path)

        if not os.path.isdir(tmp_script_folder_path):
            os.makedirs(tmp_script_folder_path)
        os.chmod(tmp_script_folder_path, stat.S_IRWXU|stat.S_IRWXG|stat.S_IRWXO)

        tmp_script_path = os.path.join(tmp_script_folder_path, "%s.sh" % query_name)
        command_path = os.path.join(tmp_script_folder_path, "%s.command" % query_name)

        header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
'''
        env = '''source %s \nexport HADOOP_USER_NAME=%s\n ''' % (parser_configs.executor_env, hadoop_user_name)
        log_util.logger.debug("[SqoopInterpreter:interpret:szw] scripts path: %s", hive_command)
        # env = '''source %s \n''' % parser_configs.executor_env
        self.writeFile(command_path, [header, env, dispatch_command, hive_command], None)
        log_util.logger.debug("[SqoopInterpreter:interpret] scripts path: %s", tmp_script_path)
        pid_path = '''%s/%s.pid''' % (tmp_script_folder_path, query_name)

        sh_header = '''
#!/bin/bash
HOME=$(cd `dirname $0`; pwd)
cd $HOME
echo $$ > %s
''' % (pid_path)

        sh_footer = '''
'''
        self.writeFile(tmp_script_path, [sh_header, "sh %s " % command_path, sh_footer], None)

        cmd = '''sh %s''' % (tmp_script_path)
        return json.dumps({"code": 0, "cmd": cmd, "query_name": query_name})

    def parse_dispatch_command(self, job_id, choose_time):
        details = self.mysql_util.get_details_by_id(job_id)
        if details["code"] == 1:
            log_util.logger.error("[SqoopInterpreter:parse_dispatch_command] %s", details["desc"])
            return details
        json_str = json.loads(details["desc"])

        # source
        source = json_str["source"]
        source_type = source["type"]
        source_server_id = source["serverId"]
        map_num = source["mapNum"]
        source_db_name = source["database"]
        source_table_name = source["table"]
        source_fields_array = source["fields"]
        source_fields_str = ""
        for j in source_fields_array:
            if j["extract"]:
                source_fields_str = source_fields_str + j["name"] + ", "
        source_fields_str = source_fields_str[:-2]
        source_where = self.replace_variable(source["where"], choose_time, True)

        # target type不同，json中的key不一样，目前只支持hive
        target = json_str["target"]
        target_server_id = target["serverId"]  # 暂时不用，mysql to mysql 使用
        target_type = target["type"]
        target_db_name = target["database"]
        target_table_name = target["table"]
        target_fileds_array = target["fields"]  # 暂时不用，mysql to mysql 使用
        target_partition = self.replace_variable(target["partition"], choose_time, False)
        # 集群默认压缩
        target_compress = target["compress"]
        hive_args = target["hiveArgs"]

        # mysql to hive
        if source_type <= 2 and target_type >= 3:
            db_info = self.mysql_util.get_db_info(source_server_id)
            if db_info["code"] == 1:
                log_util.logger.error("[SqoopInterpreter:parse_dispatch_command] %s", db_info["desc"])
            db_info_json = db_info["desc"]

            connect_str = "jdbc:mysql://" + db_info_json["host"] + ":" + db_info_json["port"] + "/" + \
                          source_db_name + "?tinyInt1isBit=false&zeroDateTimeBehavior=round"
            user_name = db_info_json["username"]
            password = zz_crypt.ZzCrypt().xor_decrypt(db_info_json["password"])

            hive_meta = hive_meta_sql.HiveMetaUtil()

            sd_id_str = hive_meta.get_table_sd_id(target_table_name)
            if sd_id_str["code"] == 1:
                log_util.logger.error("[SqoopInterpreter:parse_dispatch_command] %s", sd_id_str["desc"])
                return sd_id_str
            sd_id = sd_id_str['desc']

            location_str = hive_meta.get_location_by_sd_id(sd_id)
            if location_str["code"] == 1:
                log_util.logger.error("[SqoopInterpreter:parse_dispatch_command] %s", location_str["desc"])
                return location_str

            # 判断是否需要分区
            if (not target_partition) or len(target_partition) == 0:
                hdfs_location = location_str["desc"] + ".tmp"
            else:
                hdfs_location = location_str["desc"] + "/" + target_partition.replace("\'", "").replace(",", "/")

            separator_str = hive_meta.get_field_terminated_by_sd_id(sd_id)
            if separator_str["code"] == 1:
                log_util.logger.warn("[SqoopInterpreter:parse_dispatch_command] %s", separator_str["desc"])
                separator_str["desc"] = "\t"
            field_terminated_str = separator_str['desc']

            command = '''
--mysql-to-hive \n
--connect "%s" \n
--username %s \n
--password %s \n
--table "%s" \n
--columns "%s" \n
--where "%s"  \n
--num-mappers %s \n
--fields-terminated-by "%s" \n
--target-dir %s \n
--delete-target-dir \n
--hive-drop-import-delims \n
--lines-terminated-by \\'\\\\n\\' \n
--null-string \\'\\\\\\\\\\N\\' \n
--null-non-string \\'\\\\\\\\\\N\\' \n
''' % (connect_str, user_name, password, source_table_name, source_fields_str, source_where, map_num,
       field_terminated_str, hdfs_location)
# --null-string ''
# --null-non-string ''
# --split-by

            # 判断是否有分区字段
            if len(target_partition) > 0:
                # alter table hdp_ubu_zhuanzhuan_defaultdb.t_info add IF NOT EXISTS partition (`date`=${dateSuffix})
                # location '/home/hdp_ubu_zhuanzhuan/warehouse/hdp_ubu_zhuanzhuan_defaultdb/t_info/date=${dateSuffix}';
                # 判断分区字段是否是保留关键字
                equal_index = target_partition.find("=")
                if equal_index:
                    for key in key_list:
                        key_match = re.search(r'%s' % key, target_partition, re.M | re.I)
                        if key_match:
                            target_partition = target_partition.replace(key_match.group(), "\\`%s\\`" % key_match.group())

                # `date`
                hive_command = '''
hive -e "alter table %s.%s drop IF EXISTS partition (%s);
alter table %s.%s add IF NOT EXISTS partition (%s) location '%s';"
''' % (target_db_name, target_table_name, target_partition, target_db_name, target_table_name, target_partition, hdfs_location)
            else:
                hive_command = '''
hive -e "load data inpath '%s' overwrite into table %s.%s;"
''' % (hdfs_location, target_db_name, target_table_name)
            # command = command + "\n" + hive_command
        else:
            pass

        result = self.mysql_util.update_dispatch_command_by_id(job_id, command, '')
        return result, hive_command

    def replace_variable(self, arg_str, choose_time, is_db):
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
        if is_db:
            # mysql 转义'
            arg_str = arg_str.replace("\'", "\\\'")
        # 统一将字符串中的"转成'
        arg_str = arg_str.replace("\"", "\\\'")
        return arg_str

    def handler_order(self, dispatch_command, queue_name, job_name, user_name, query_name):
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
        dispatch_command = dispatch_command.replace("\n", " ")  # .replace("\\", " ")

        sqoop_type = ''
        # 过滤所有的参数(只是过滤参数，不过滤对应的值)
        for args in filter_List:
            args_match = re.search(r'%s' % args, dispatch_command, re.M | re.I)
            if args_match:
                agr = args_match.group()
                # 替换所有的参数
                dispatch_command = dispatch_command.replace(agr, "")
                # 加个dict？？？还是？？？
                if agr == "--mysql-to-hive":
                    sqoop_type = "import"
        # 获取用户组。即资源提交的队列
        dispatch_command = "%s %s -D mapreduce.job.queuename=%s -D zzdp.mapreduce.job.name=%s " \
                           "-D zzdp.mapreduce.job.user=%s -Dzzdp.mapreduce.job.id=%s %s" \
                           % ("sqoop", sqoop_type, queue_name, job_name, user_name, query_name, dispatch_command)
        return code, dispatch_command

if __name__ == "__main__":
    print("######main start#############")
    sqoop_test = SqoopInterpreter()
    order = '''
             sqoop import --connect 11 --database bdms_web --lines-terminated-by '\n' --fields-terminated-by '\t'
             --table 'ddl_storeformat' --num-mappers 4 --append  --columns "id,name,is_valid" --target-dir /lupan
             --query "l_date>'$YESTERDAY_BEGINTIME' and l_date='$YESTERDAY_ENDTIME' $YESTERDAY $TODAY ----  $RANDOM"
            '''
    print("#############order######:%s" %order)
    # order = sqoop_test.command_replace(order)
    # print("[ORDER]:%s" %order)
    # sqoop_test.parse_dispatch_command(1, "2017-02-09")
    sqoop_test.interpret(34568, '2292', "2018-06-26", "2018-06-26 16:12:52")
    print '\\\\\\n'
