# -*- coding: utf-8 -*-

import sys
import datetime
import os


def shift_date(offset):
    now = datetime.datetime.now()
    delta = datetime.timedelta(days=offset)
    date = now + delta
    return date.strftime('%Y-%m-%d')


def shell_date(dateOffset):
    offset = dateOffset[10:]
    command = 'date -d \'' + str(offset) + '  day\' +%Y-%m-%d'
    out = os.popen(command).read()
    return out


def parseDateOffsetVariable(dateOffsetStr):
    import re
    import os
    list = re.findall('\$DATE_OFFSET{[\+,-]?\d+}', dateOffsetStr)
    for obj in list:
        offset = obj.replace('$DATE_OFFSET{', '').replace('}', '')
    command = 'date -d \'' + str(offset) + '  day\' +%Y-%m-%d'
    out = os.popen(command).read().split("\n")[0]
    return out

    
def bash_time(bash_time_str):
    pattern_match_bash = '\$bash{[a-zA-Z_0-9\s+%\'\"-]*}'
    import re
    import os
    variables_bash = re.findall(pattern_match_bash, bash_time_str)
    for variable in variables_bash:
        time_str = variable
    return time_str


def is_leap_year(year):
    if year % 100 == 0 and year % 400 == 0:
        return True
    if year % 100 != 0 and year % 4 == 0:
        return True
    return False


def current_month_days(year, month):
    current_month_day = 31
    if month in [4, 6, 9, 11]:
        current_month_day = 30
    if month == 2:
        current_month_day = 28
        if is_leap_year(year):
            current_month_day = 29
    return current_month_day


def get_str_date(year, month, day):
    str_month = str(month)
    if len(str(month)) == 1:
        str_month = "0" + str(month)
    str_day = str(day)
    if len(str(day)) == 1:
        str_day = "0" + str(day)
    return str(year) + str_month + str_day

    
def calc_end_date(year, month, day, interval):
    accum_days = 0
    l_year = year
    l_month = month
    l_day = day
    current_month_day = 0
    last_interval = 0
    while interval > accum_days:
        current_month_day = current_month_days(l_year, l_month)
        last_interval = current_month_day - l_day
        accum_days += last_interval
        if l_month == 12:
            l_year += 1
        l_month = (l_month + 1) % 12
        accum_days += 1
        l_day = 1
        l_date = get_str_date(l_year, l_month, l_day)
        print accum_days, l_date

    if accum_days - interval >= l_day:
        l_month = (l_month - 1) % 12
    l_day = (current_month_day + l_day - accum_days + interval) % current_month_day
    if l_day == 0:
        l_day = current_month_day
        
    return get_str_date(l_year, l_month, l_day)

        
if __name__ == "__main__":
    #date = parseDateOffsetVariable('$DATE_OFFSET{+1}');
    #print date
    
    if len(sys.argv) != 3:
        print "Please input proper parameters: e.g. 20190912 128."
        exit(0)

    if not sys.argv[2].decode("utf8").isnumeric() or not sys.argv[1].decode("utf8").isnumeric():
        print "The parameters are numerical."
        exit(0)
    
    if len(sys.argv[1]) != 8:
        print "The date format is incorrect."
        exit(0)

    start_date = sys.argv[1]
    interval = int(sys.argv[2])
    year = int(start_date[:4])
    month = int(start_date[4:6])
    day = int(start_date[6:])
    print year, month, day

    end_date = calc_end_date(year, month, day, interval)
    print end_date

