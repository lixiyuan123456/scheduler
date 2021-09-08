# -*- coding: utf-8 -*-

import sys
import urllib2
import json
import time

wx_http_url = 'http://wxmsg.zhuaninc.com/api/message/send'
app_code = '827c4790af4ca7a5ba2db47a72099df5'
msg_type = 0  # 0 -- 报警， 1 -- 警告


def post(url, data):
    headers = {'Content-Type': 'application/json'}
    req = urllib2.Request(url, data, headers=headers)
    response = urllib2.urlopen(req)
    return response.read()


if __name__ == '__main__':
    # user_names = sys.argv[1]
    # msg = sys.argv[2]
    user_names = 'sunzhiwei'
    msg = 'flume down'
    data = dict(to_user=user_names, msg_type=msg_type, app_code=app_code, msg=msg)
    json_data = json.dumps(data)
    result = post(wx_http_url, json_data)
    result_json = json.loads(result)
    # result_json['errcode'] = 1
    count = 3
    while result_json['errcode'] != 0 and count > 0:
        print 'request again %s' % (3 - count + 1)
        result = post(wx_http_url, json_data)
        result_json = json.loads(result)
        count -= 1
