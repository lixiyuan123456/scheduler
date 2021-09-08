# -*- coding: utf-8 -*-

import json
import sys, os
import functools
import MySQLdb
from datetime import *

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)
import parser_configs

import log_util


def mysql_exceptions_handle(func):

    # 'Encapsulate mysql exceptions handling routine.'

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
                return {"code":1, "desc":"[MySQL d Error]: %s" % str(e)}
        except Exception, e:
            log_util.logger.error("[MySQL Error]: %s", str(e))
            return {"code":1, "desc":"[MySQL a Error]: %s" % str(e)}
    return wrapper


def mysql_connect(mysql_query_func):

    # 'manage mysql connections, including creating and closing.'

    @functools.wraps(mysql_query_func)
    def wrapper(*args, **kwds):
        conn = MySQLdb.connect(host=parser_configs.hive_mysql_host, user=parser_configs.hive_mysql_user,
                               passwd=parser_configs.hive_mysql_passwd, db=parser_configs.hive_mysql_db,
                               use_unicode=True, charset="utf8", port=parser_configs.hive_mysql_port)
        conn.autocommit(True)
        cursor = conn.cursor()
        query_result = mysql_query_func(*(args+(cursor,)), **kwds)
        cursor.close()
        conn.close()
        return query_result
    return wrapper

class HiveMetaUtil:
    def __init__(self):
        pass

    @mysql_exceptions_handle
    @mysql_connect
    def get_table_sd_id(self, table_name, cursor):

        cursor.execute("set names 'UTF8'")
        query_str = '''
        SELECT SD_ID
        FROM TBLS
        WHERE TBL_NAME = '%s'
        ''' % table_name

        cursor.execute(query_str)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any sd_id with table_name: %s.' % table_name)
        else:
            sd_id, = query_result
        return sd_id

    @mysql_exceptions_handle
    @mysql_connect
    def get_location_by_sd_id(self, sd_id, cursor):

        cursor.execute("set names 'UTF8'")
        query_str = '''
        SELECT LOCATION
        FROM SDS
        WHERE SD_ID = %s
        ''' % sd_id

        cursor.execute(query_str)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any location with sd_id: %s.' % sd_id)
        else:
            location_str, = query_result
        return location_str

    @mysql_exceptions_handle
    @mysql_connect
    def get_field_terminated_by_sd_id(self, sd_id, cursor):

        cursor.execute("set names 'UTF8'")
        query_str = '''
        SELECT PARAM_VALUE
        FROM SERDE_PARAMS
        WHERE SERDE_ID = %s and PARAM_KEY = 'field.delim'
        ''' % sd_id

        cursor.execute(query_str)
        query_result = cursor.fetchone()
        if (not query_result) or len(query_result) == 0:
            raise Exception('CAN NOT find any field_terminated with sd_id: %s.' % sd_id)
        else:
            location_str, = query_result
        return location_str

if __name__ == "__main__":
    hiveMeta = HiveMetaUtil()
    # parquet 序列化的分隔符是什么？
    print hiveMeta.get_field_terminated_by_sd_id(166)
    print hiveMeta.get_location_by_sd_id(51L)
    print hiveMeta.get_table_sd_id('t_im_message_detail')