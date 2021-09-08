# -*- coding: utf-8 -*-

import sys, os
import constant_util

'''
下表是dp任务中所支持的变量替换（部分以`2014-04-03`为例）：

系统自动调度按当前执行时间作为关联时间去计算任务中用到的变量；
用户手动重跑，按选择的重跑时间作为关联时间去计算任务中用到的变量；
$bash{***}的变量默认就是当前系统时间作为关联时间，如果需要按用户选择重跑时间去动态改变，可以这样编写$bash{date -d'${dateSuffix}' +%Y%m%d}；
'''

# value是function
CONSTANT = {
    # /tmp/dw_tmp_file/临时目录
    "${tempCatalog}": constant_util.tmp_path,
    # 2014-04-03，如果手动选择，则为选择日期-1天	导出文件名的后缀，昨天日期
    "${outFileSuffix}": constant_util.yesterday_with,
    # '2014-04-03'	昨天日期，如果手动选择，则为选择日期-1天
    "${startDate}": constant_util.yesterday_with,
    # 20140403	默认任务执行的前一天，如果手动选择，则为选择日期-1天
    "${dateSuffix}": constant_util.yesterday,
    # 2015061117	当前日期小时后缀
    "${dateHourSuffix}": constant_util.date_hour,
    # 2015061116	当前日期前一小时后缀
    "${dateBeforeOneHourSuffix}": constant_util.date_before_one_hour,
    # 201404	月份后缀
    "${monthSuffix}": constant_util.month,
    # 201403	上个月份后缀
    "${lastMonthSuffix}": constant_util.last_month,
    # '2015-08-24'	当日，如果手动选择，则为选择日期
    "${today}": constant_util.today_with,
    # yyyy-MM-dd HH:mm:ss	当前日期时间，如果手动选择，则为选择日期时间
    "${todayDateTime}": constant_util.today_date_time,
    # 20150824	当日，如果手动选择则为选择日期
    "${todaySuffix}": constant_util.today,
    # '2014-04-03'	昨日，如果手动选择，则为选择日期-1天
    "${dealDate}": constant_util.yesterday_with,
    # '2014M04'	昨日的月，如果手动选择，则为选择日期-1天的月份
    "${monthId}": constant_util.month_id,
    # '2014-04-01'	昨日的月初，如果手动选择，则为选择日期-1天的月初
    "${monthBegin}": constant_util.month_begin_with,
    # '2014-04-30'	昨日的月末，如果手动选择，则为选择日期-1天的月末
    "${monthEnd}": constant_util.month_end_with,
    # 20140401	昨日的月初，如果手动选择，则为选择日期-1天的月初
    "${monthBeginSuffix}": constant_util.month_begin,
    # 20140430	昨日的月末，如果手动选择，则为选择日期-1天的月末
    "${monthEndSuffix}": constant_util.month_end,
    # '2014W15'	昨日周WeekId，如果手动选择，则为选择日期-1天的周WeekId
    "${weekId}": constant_util.week_id,
    # '2014-04-06'(周日)	跨年了，该值就变成20**-01-01，如果手动选择，则为选择日期-1天的周日
    "${weekBegin}": constant_util.week_begin_with,
    # '2014-04-12'(周六)	跨年了，该值变成20**-12-31
    "${weekEnd}": constant_util.week_end_with,
    # 20140406(周日)	跨年了，该值就变成20**0101
    "${weekBeginSuffix}": constant_util.week_begin,
    # 20140412(周六)	跨年了，该值变成20**1231
    "${weekEndSuffix}": constant_util.week_end,
    # '2015-08-24'(周一)	昨日相对的周一，如果手动选择，则为选择日期-1天的周一
    "${weekBeginCn}": constant_util.week_begin_cn_with,
    # '2015-08-30'(周日)	昨日相对的周日，如果手动选择，则为选择日期-1天的周日
    "${weekEndCn}": constant_util.week_end_cn_with,
    # 20150824(周一)
    "${weekBeginCnSuffix}": constant_util.week_begin_cn,
    # 20150830(周日)
    "${weekEndCnSuffix}": constant_util.week_end_cn,
    # date_sub(?,interval 7 day)	取七天前日期
    "${sevenDaysBefore}": constant_util.seven_days_before_with,
    # 日期格式是 yyyyMMdd	取7天前日期
    "${sevenDaysBeforeSuffix}": constant_util.seven_days_before_suffix,
    # 日期格式是 yyyyMMdd	取30天前日期
    "${thirtyDaysBeforeSuffix}": constant_util.thirty_days_before_suffix,
    # 日期格式是 yyyyMMdd	取60天前日期
    "${sixtyDaysBeforeSuffix}": constant_util.sixty_days_before_suffix,
    # monthOfYear	日期的月份(06)
    "${monthOnlySuffix}": constant_util.month_only_suffix,
    # {}中的bash日期表达式用户自定义	按表达式解析为准
    # $bash{date -d '${todaySuffix} -8 day' +%Y-%m-%d}
    "$bash{": constant_util.bash_time
    # 按一定规则生成，含module_name	数据文件路径
    # "${dataFile}"
}

