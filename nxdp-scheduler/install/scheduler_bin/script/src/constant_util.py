# -*- coding: utf-8 -*-

from datetime import timedelta, date, datetime
import calendar

def tmp_path(path=None):
    if path:
        return path
    return "/tmp/dw_tmp_file/"

def choose_time_to_date(choose_time):
    b_date = datetime.strptime(choose_time, "%Y-%m-%d %H:%M:%S")
    return b_date

def choose_time_to_namespce(choose_time):
    if choose_time:
        b_date = choose_time.split(" ")[0].split("-")
        b_date = [int(num) for num in b_date]
        b_day = date(b_date[0], b_date[1], b_date[2])
        return b_day

def today_with(choose_time=None):
    if choose_time:
        return choose_time_to_namespce(choose_time).strftime("%Y-%m-%d")
    return str(date.today())

def today(choose_time=None):
    if choose_time:
        dateSuffix = choose_time_to_namespce(choose_time)
    else:
        dateSuffix = date.today()
    return dateSuffix.strftime("%Y%m%d")

def today_date_time(choose_time=None):
    if choose_time:
        return choose_time
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")

def yesterday_with(choose_time=None):
    if choose_time:
        b_day = choose_time_to_namespce(choose_time)
        return str(b_day - timedelta(days=1))
    return str(date.today() - timedelta(days=1))

def yesterday(choose_time=None):
    if choose_time:
        b_day = choose_time_to_namespce(choose_time)
        dateSuffix = b_day - timedelta(days=1)
    else:
        dateSuffix = date.today() - timedelta(days=1)
    return dateSuffix.strftime("%Y%m%d")

def date_hour(choose_time=None):
    if choose_time:
        b_day = choose_time_to_date(choose_time)
        dateSuffix = b_day
    else:
        dateSuffix = datetime.now()
    return dateSuffix.strftime("%Y%m%d%H")
    # return datetime.now().strftime("%Y%m%d%H")

def date_before_one_hour(choose_time=None):
    if choose_time:
        b_day = choose_time_to_date(choose_time)
        dateSuffix = b_day - timedelta(hours=1)
    else:
        dateSuffix = datetime.now()
    return dateSuffix.strftime("%Y%m%d%H")
    # return (datetime.now() - timedelta(hours=1)).strftime("%Y%m%d%H")

def month(choose_time=None):
    if choose_time:
        return choose_time_to_namespce(choose_time).strftime("%Y%m")
    return datetime.now().strftime("%Y%m")

def last_month(choose_time=None):
    if choose_time:
        b_date = choose_time_to_date(choose_time)
        return (date(b_date.today().year, b_date.today().month, 1) - timedelta(days=1)).strftime("%Y%m")
    return (date(date.today().year, date.today().month, 1) - timedelta(days=1)).strftime("%Y%m")

def month_id(choose_time=None):
    if choose_time:
        b_day = choose_time_to_namespce(choose_time)
        return (b_day - timedelta(days=1)).strftime("%Y%m")
    return (date.today() - timedelta(days=1)).strftime("%Y%m")

def month_begin_with(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return date(y_day.year, y_day.month, 1).strftime("%Y-%m-%d")

def month_end_with(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    _, days = calendar.monthrange(y_day.year, y_day.month)
    return date(y_day.year, y_day.month, days).strftime("%Y-%m-%d")

def month_begin(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return date(y_day.year, y_day.month, 1).strftime("%Y%m%d")

def month_end(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    _, days = calendar.monthrange(y_day.year, y_day.month)
    return date(y_day.year, y_day.month, days).strftime("%Y%m%d")

def week_id(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return y_day.strftime("%W")

def week_begin_with(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    sunday = y_day + timedelta(6 - y_day.weekday())
    if sunday.year != y_day.year:
        return date(sunday.year, 1, 1).strftime("%Y-%m-%d")
    return sunday.strftime("%Y-%m-%d")

def week_end_with(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    saturday = y_day + timedelta(6 - y_day.weekday()) + timedelta(6)
    if saturday.year != y_day.year:
        return date(y_day.year, 12, 31).strftime("%Y-%m-%d")
    return saturday.strftime("%Y-%m-%d")

def week_begin(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    sunday = y_day + timedelta(6 - y_day.weekday())
    if sunday.year != y_day.year:
        return date(sunday.year, 1, 1).strftime("%Y%m%d")
    return sunday.strftime("%Y%m%d")

def week_end(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    saturday = y_day + timedelta(6 - y_day.weekday()) + timedelta(6)
    if saturday.year != y_day.year:
        return date(y_day.year, 12, 31).strftime("%Y%m%d")
    return saturday.strftime("%Y%m%d")

def week_begin_cn_with(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return (y_day - timedelta(y_day.weekday())).strftime("%Y-%m-%d")

def week_end_cn_with(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return (y_day + timedelta(6 - y_day.weekday())).strftime("%Y-%m-%d")

def week_begin_cn(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return (y_day - timedelta(y_day.weekday())).strftime("%Y%m%d")

def week_end_cn(choose_time=None):
    y_day = datetime.strptime(yesterday_with(choose_time), "%Y-%m-%d")
    return (y_day + timedelta(6 - y_day.weekday())).strftime("%Y%m%d")

def seven_days_before_with(choose_time=None):
    t_day = datetime.strptime(today_with(choose_time), "%Y-%m-%d")
    return (t_day - timedelta(7)).strftime("%Y-%m-%d")

def seven_days_before_suffix(choose_time=None):
    return num_days_before_suffix(7,choose_time)

def thirty_days_before_suffix(choose_time=None):
    return num_days_before_suffix(30,choose_time)

def sixty_days_before_suffix(choose_time=None):
    return num_days_before_suffix(60,choose_time)

def num_days_before_suffix(days, choose_time=None):
    t_day = datetime.strptime(today_with(choose_time), "%Y-%m-%d")
    return (t_day - timedelta(days)).strftime("%Y%m%d")

def month_only_suffix(choose_time=None):
    t_day = datetime.strptime(today_with(choose_time), "%Y-%m-%d")
    return t_day.month

# 重跑，命令不支持
def bash_time(bash_time_str):
    pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+%\'\"-]*}'
    import re
    import os
    variables_bash = re.findall(pattern_match_bash, bash_time_str)
    for variable in variables_bash:
        time_str = os.popen(variable[6:-1]).read().split("\n")[0]
    return time_str

def bash():
    # s = '''$bash{date -d "2018-03-01" -8 day +%Y-%m-%d}kjl}fdgd'''
    s = '''sdfsf${monthOnlySuffix}dsfsafsa$b$bash{  date -d "${todaySuffix} -8 day" +%Y-%m-%d}dssdfsf${monthOnlySuffix}dsfs'''
# print s
    ss = 'sdfsf${monthOnlySuffix}dsfs'
    pattern_match = '\${[a-zA-Z_]\w*}'
    # $bash{date -d '2018-03-01 -8 day' +%Y-%m-%d}
    pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+%\'\"-]*}'
    # pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+]*}'
    import re
    variables = re.findall(pattern_match, s)
    for v in variables:
        s = s.replace(v, '2018-03-01')
    vs = re.findall(pattern_match_bash, s)
    import os
    for vv in vs:
        print vv[6:-1]
        # p = os.system(vv[6: -1])
        print os.popen('date -d "2018-03-01 -8 day" +%Y-%m-%d').read().split("\n")[0]
        # print p
    # print s


if __name__ == '__main__':
    # print yesterday('2018-03-01 11:00:00')
    # print month_only_suffix()
    # bash()
    # s = "        s dss dfs "
    # print s.replace(" ", ""), s
    print last_month('2018-06-23 00:30:00')