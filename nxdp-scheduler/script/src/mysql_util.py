# -*- coding: utf-8 -*-
import json
import sys, os
import functools
import MySQLdb
from datetime import *

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)
import parser_configs, log_util

def mysql_exceptions_handle(func):

    'Encapsulate mysql exceptions handling routine.'

    @functools.wraps(func)
    def wrapper(*args, **kwds):
        try:
            return {"code":0, "desc":func(*args, **kwds)}
        except MySQLdb.Error, e:
            try:
                log_util.logger.error("[MySQL Error] [%d]: %s", e.arg[0], e.arg[1])
                return {"code":1, "desc":"[MySQL Error] [%d]: %s" % (e.arg[0], e.arg[1])}
            except:
                log_util.logger.error("[MySQL Error]: %s", str(e))
                return {"code":1, "desc":"[MySQL Error]: %s" % str(e)}
        except Exception, e:
            log_util.logger.error("[MySQL Error]: %s", str(e))
            return {"code":1, "desc":"[MySQL Error]: %s" % str(e)}
    return wrapper


def mysql_connect(mysql_query_func):

    'manage mysql connections, including creating and closing.'

    @functools.wraps(mysql_query_func)
    def wrapper(*args, **kwds):
        conn = MySQLdb.connect(host=parser_configs.mysql_host, user=parser_configs.mysql_user,
                               passwd=parser_configs.mysql_passwd, db=parser_configs.mysql_db,
                               use_unicode=True, charset="utf8", port=parser_configs.mysql_port)
        conn.autocommit(True)
        cursor = conn.cursor()
        query_result = mysql_query_func(*(args+(cursor,)), **kwds)
        cursor.close()
        conn.close()
        return query_result
    return wrapper

class MySqlUtil:
    def __init__(self):
        pass

    @mysql_exceptions_handle
    @mysql_connect
    def get_scripttype_by_jobId(self, job_id, cursor):

        cursor.execute("set names 'UTF8'")
        query_type = '''
        SELECT t_dict_job_type.type
        FROM t_job_schedule
        JOIN t_dict_job_type
        ON t_job_schedule.job_type = t_dict_job_type.id
        WHERE t_job_schedule.job_id = %s
        ''' % job_id

        cursor.execute(query_type)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any job script type with job_id: %s.' % job_id)
        else:
            script_type, = query_result
        return script_type

    @mysql_exceptions_handle
    @mysql_connect
    def get_executor_info(self, job_id, cursor):

        cursor.execute("set names 'UTF8'")
        executor_info = '''
        SELECT dept_id, user_name, job_name, dispatch_command, job_level, hadoop_queue_id, jar_path
        FROM t_job_schedule WHERE job_id = %s;
        ''' % job_id

        cursor.execute(executor_info)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception("CAN NOT find any executor information from t_job_schedule for %s " % job_id)
        else:
            dept_id, user_name, job_name, dispatch_command, job_level, hadoop_queue_id, jar_path = query_result

        # get queue_name
        queue_info = '''
        SELECT queue_name from t_dict_queue WHERE id = %s;
        ''' % hadoop_queue_id
        try:
            cursor.execute(queue_info)
        except Exception,e:
            raise Exception("jobId %s,  %s" % (job_id, queue_info) + " error .")

        queue_info_result = cursor.fetchone()
        if (not queue_info_result) or len(queue_info_result) == 0:
            raise Exception("CAN NOT find queue name from t_dict_queue for %s " % hadoop_queue_id)
        else:
            queue_name, = queue_info_result

        # get hadoop_user_name
        user_info = '''
        SELECT hadoop_user_name FROM t_hadoop_user WHERE department_id = %s;
        ''' % dept_id
        try:
            cursor.execute(user_info)
        except Exception,e:
            raise Exception("jobId %s,  %s" % (job_id, user_info) + " error .")
        user_info_result = cursor.fetchone()
        if (not user_info_result) or len(user_info_result) == 0:
            raise Exception("CAN NOT find hadoop user name from t_hadoop_user for %s " % job_id)
        else:
            hadoop_user_name, = user_info_result

        executor_info_json = {"hadoop_user_name": hadoop_user_name, "dispatch_command": dispatch_command,
                              "job_level": job_level, "queue_name": queue_name, "jar_path": jar_path,
                              "user_name": user_name, "job_name": job_name}
        return executor_info_json

    @mysql_exceptions_handle
    @mysql_connect
    def get_details_by_id(self, job_id, cursor):

        cursor.execute("set names 'UTF8'")
        query_type = '''
        SELECT details
        FROM t_job_config
        WHERE id = %s
        ''' % job_id

        cursor.execute(query_type)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any job details with job_id: %s.' % job_id)
        else:
            details, = query_result
        return details

    @mysql_exceptions_handle
    @mysql_connect
    def update_dispatch_command_by_id(self, job_id, command, jar_path, cursor):

        cursor.execute("set names 'UTF8'")
        query_type = '''
        UPDATE t_job_schedule
        SET dispatch_command = '%s', jar_path = '%s', update_time = now()
        WHERE job_id = %s
        ''' % (command, jar_path, job_id)

        result = cursor.execute(query_type)
        return result

    @mysql_exceptions_handle
    @mysql_connect
    def get_db_info(self, db_id, cursor):

        cursor.execute("set names 'UTF8'")
        query_type = '''
        SELECT host, port, username, password, server_type
        FROM t_server_config
        WHERE id = %s
        ''' % db_id

        cursor.execute(query_type)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any db info with db_id: %s.' % db_id)
        else:
            host, port, username, password, server_type = query_result

        db_info_json = {"host": host, "port": port, "username": username, "password": password,
                        "server_type": server_type}
        return db_info_json

    @mysql_exceptions_handle
    @mysql_connect
    def get_job_info_by_id(self, job_id, cursor):

        cursor.execute("set names 'UTF8'")
        query_type = '''
        SELECT user_name, job_name
        FROM t_job_schedule
        WHERE job_id = %s
        ''' % job_id

        cursor.execute(query_type)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any db info with job_id: %s.' % job_id)
        else:
            user_name, job_name = query_result

        job_info_json = {"user_name": user_name, "job_name": job_name}
        return job_info_json


if __name__ == "__main__":
    mysql = MySqlUtil()
    type = mysql.get_executor_info(4)
    print mysql.get_scripttype_by_jobId(4)
    print mysql.get_details_by_id(4)
    # print mysql.update_dispatch_command_by_id(34316, 'fsdf')
    print type


