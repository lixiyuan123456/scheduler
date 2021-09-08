# -*- coding: utf-8 -*-

import sys, os
import logging, logging.handlers

src_path = os.path.dirname(os.path.abspath(__file__))
etc_path = src_path + "/../etc"
sys.path.append(etc_path)

import parser_configs


# 回显类型
ECHO_TYPE = {
    "RESULT"  :0,  # 回显命令执行的结果
    "PROCESS" :1   # 回显命令执行的过程
}

# 日志等级
LOG_LEVELS = {
    "CRITICAL" : logging.CRITICAL,
    "ERROR"    : logging.ERROR,
    "WARNING"  : logging.WARNING,
    "INFO"     : logging.INFO,
    "DEBUG"    : logging.DEBUG,
    "NOTSET"   : logging.NOTSET
}

# 命令在执行过程中如果产生错误信息，基于下面的模式做正则匹配
regex_error_pattern = "Permission denied|Failed|FAILED|Error|cannot|ParseException|Cannot access|could not|does not|not defined|command not found"


# 全局使用的日志记录
def create_logger():
    logger = logging.getLogger('script_parser')
    handler = logging.handlers.TimedRotatingFileHandler(parser_configs.log_path, 'D', 1, 15)
    handler.suffix = "%Y%m%d%H%M.log"
    formatter = logging.Formatter('[%(asctime)s][%(levelname)s] %(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(LOG_LEVELS[parser_configs.log_level])
    return logger

logger = create_logger()
