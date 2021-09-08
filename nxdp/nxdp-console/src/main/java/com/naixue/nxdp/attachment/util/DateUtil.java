package com.naixue.nxdp.attachment.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 时间工具类
 */
public class DateUtil {

    public static SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    private Date datetime;

    public DateUtil() {
        datetime = new Date();
    }

    public DateUtil(Date date) {
        datetime = date;
    }

    /**
     * 根据给定的起止日期和终止日期，取得范围内的日期的字符串集合yyyyMMdd格式
     *
     * @param start
     * @param end
     * @return
     * @throws ParseException
     */
    public static Set<String> getDateRangeSet(String start, String end) throws ParseException {
        return getDateRangeSet(start, end, "yyyyMMdd", "yyyyMMdd");
    }

    /**
     * 根据给定的起止日期和终止日期，取得范围内的日期的字符串集合yyyy-MM-dd格式
     *
     * @param start
     * @param end
     * @return
     * @throws ParseException
     */
    public static Set<String> getDashDateRangeSet(String start, String end) throws ParseException {
        return getDateRangeSet(start, end, "yyyy-MM-dd", "yyyy-MM-dd");
    }

    /**
     * 根据给定的起止日期和终止日期，取得范围内的日期的字符串集合yyyyMMdd格式
     *
     * @param start
     * @param end
     * @return
     * @throws ParseException
     */
    public static Set<String> getDateRangeSet(
            String start, String end, String sourceFormatStr, String destFormatStr)
            throws ParseException {

        SimpleDateFormat sourceFormat = new SimpleDateFormat(sourceFormatStr);
        SimpleDateFormat destFormat = new SimpleDateFormat(destFormatStr);

        Set<String> dates = new TreeSet<String>();

        Calendar c = Calendar.getInstance();

        c.setTime(sourceFormat.parse(end));
        String endDay = destFormat.format(c.getTime());

        c.setTime(sourceFormat.parse(start));
        String tmpDay = destFormat.format(c.getTime());

        while (tmpDay.compareTo(endDay) <= 0) {
            dates.add(tmpDay);
            c.add(Calendar.DAY_OF_MONTH, 1);
            tmpDay = destFormat.format(c.getTime());
        }

        return dates;
    }

    /**
     * 根据给定的起止日期和终止日期，取得范围内的日期的字符串集合yyyyMMdd格式
     *
     * @param start
     * @param end
     * @return
     * @throws ParseException
     */
    public static Set<String> getDateRangeSetByFormat(
            String start, String end, String sourceFormatStr, String destFormatStr, int addType)
            throws ParseException {

        SimpleDateFormat sourceFormat = new SimpleDateFormat(sourceFormatStr);
        SimpleDateFormat destFormat = new SimpleDateFormat(destFormatStr);

        Set<String> dates = new TreeSet<String>();

        Calendar c = Calendar.getInstance();

        c.setTime(sourceFormat.parse(end));
        String endDay = destFormat.format(c.getTime());

        c.setTime(sourceFormat.parse(start));
        String tmpDay = destFormat.format(c.getTime());

        while (tmpDay.compareTo(endDay) <= 0) {
            dates.add(tmpDay);
            c.add(addType, 1);
            tmpDay = destFormat.format(c.getTime());
        }

        return dates;
    }

    public static String toString(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 取得后一天-前一天的毫秒数
     *
     * @param time1
     * @param time2
     * @return
     */
    public static String timeSpan(Date time1, Date time2) {
        long t1 = time1.getTime();
        long t2 = time2.getTime();

        return DateUtil.toTimeString(t2 - t1);
    }

    private static String toTimeString(long t) {
        String s = "";

        long md = 1000 * 60 * 60 * 24;
        long mh = 1000 * 60 * 60;
        long mm = 1000 * 60;
        long ms = 1000;

        long d = t / md;
        long c = t % md;

        if (d > 0) {
            s += d + " days ";
        }
        if (c > 0) {
            d = c / mh;
            c = c % mh;

            if (d < 10) {
                s += "0";
            }
            s += d;
            s += ":";
            if (c > 0) {
                d = c / mm;
                c = c % mm;

                if (d < 10) {
                    s += "0";
                }
                s += d;
                s += ":";
                if (c > 0) {
                    d = c / ms;

                    if (d < 10) {
                        s += "0";
                    }
                    s += d;
                } else {
                    s += "00";
                }
            } else {
                s += "00:00";
            }
        }
        return s;
    }

    /**
     * 取得此日期的星期数
     *
     * @param date
     * @return
     */
    public static String getDayOfWeek(Date date) {
        String[] weekDays = {
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        };
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) w = 0;
        return weekDays[w];
    }

    /**
     * 取得此日期的星期数中文
     *
     * @param date
     * @return
     */
    public static String getDayOfWeekChinese(Date date) {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) w = 0;
        return weekDays[w];
    }

    /**
     * 这个日期是否为当月第一天
     *
     * @param date
     * @return
     */
    public static boolean ifTheFirstDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (1 == cal.get(Calendar.DAY_OF_MONTH)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 取得过去n天的第一天和最后一天，以日期数组返回
     *
     * @param computedays
     * @param lastDay
     * @param include
     * @return
     */
    public static Date[] getStartDateAndEndDate(int computedays, Date lastDay, boolean include) {
        Date[] dates = new Date[2];
        // 拿到日历对象
        Calendar c = Calendar.getInstance();
        // 将lastDay格式化，并同步到日历对象中
        String lastDayStr = new SimpleDateFormat("yyyy-MM-dd").format(lastDay);
        c.set(
                new Integer(lastDayStr.split("-")[0]),
                new Integer(lastDayStr.split("-")[1]) - 1,
                new Integer(lastDayStr.split("-")[2]),
                0,
                0,
                0);

        // 如果不包含lastDay
        if (!include) {
            // 将日历对象日期-1
            c.add(Calendar.DAY_OF_MONTH, -1);
            // 按传入参数格式化日期为字符串，存入日期集合中
            dates[1] = c.getTime();
            // 循环天数，
            for (int i = 0; i < computedays - 1; i++) {
                // 将日历对象日期-1
                c.add(Calendar.DAY_OF_MONTH, -1);
                // 按传入参数格式化日期为字符串，存入日期集合中
                dates[0] = c.getTime();
            }
        } else {
            // 如果包含lastDay，先将最近一天格式化成字符串存入日期集合中
            dates[1] = lastDay;
            // 循环 计算天数-1（已经存入了一天了）
            for (int i = 0; i < computedays - 1; i++) {
                // 将日历对象日期-1
                c.add(Calendar.DAY_OF_MONTH, -1);
                // 按传入参数格式化日期为字符串，存入日期集合中
                dates[0] = c.getTime();
            }
        }
        return dates;
    }

    /**
     * 取得上个月有多少天
     *
     * @param date
     * @return
     */
    public static int getDaysOfLastMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 月份减一
        cal.add(Calendar.MONTH, -1);
        // 最后一天的日期数即为此月的天数
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 取得两个日期之间的所有日期
     *
     * @param computedays
     * @param lastDay
     * @param include
     * @return
     */
    public static Set<Date> getDates(Date startDay, Date endDay) {
        Set<Date> dates = new TreeSet<Date>();
        // 拿到日历对象
        Calendar tmpC = Calendar.getInstance();
        Calendar endC = Calendar.getInstance();
        tmpC.setTime(startDay);
        endC.setTime(endDay);
        {
            dates.add(endDay);
            while (tmpC.before(endC)) {
                dates.add(tmpC.getTime());
                // 将日历对象日期+1
                tmpC.add(Calendar.DAY_OF_MONTH, +1);
            }
        }
        return dates;
    }

    /**
     * 取得两个日期之间的天数
     *
     * @param computedays
     * @param lastDay
     * @param include
     * @return
     */
    public static int getDays(Date startDay, Date endDay) {
        Set<String> dates = new HashSet<String>();
        // 拿到日历对象
        Calendar tmpC = Calendar.getInstance();
        Calendar endC = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        tmpC.setTime(startDay);
        endC.setTime(endDay);
        {
            dates.add(sdf.format(endDay));
            while (tmpC.before(endC)) {
                dates.add(sdf.format(tmpC.getTime()));
                // 将日历对象日期+1
                tmpC.add(Calendar.DAY_OF_MONTH, +1);
            }
        }
        return dates.size();
    }

    /**
     * 按指定星期 分割给定的时间范围
     *
     * @param startDay
     * @param endDay
     * @param week
     * @return
     */
    public static Set<Date[]> splitDatesByWeek(Date startDay, Date endDay, String week) {
        String[] weekDays = {
                "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
        };
        for (String weekDay : weekDays) {
            if (weekDay.equalsIgnoreCase(week)) {
                return splitDates(startDay, endDay, week);
            }
        }
        return null;
    }

    /**
     * 按月分割给定的时间范围
     *
     * @param startDay
     * @param endDay
     * @return
     */
    public static Set<Date[]> splitDatesByMonth(Date startDay, Date endDay) {
        return splitDates(startDay, endDay, "month");
    }

    /**
     * 按特定类别（月，周等）分割给定的时间范围
     *
     * @param startDay
     * @param endDay
     * @param period
     * @return
     */
    public static Set<Date[]> splitDates(Date startDay, Date endDay, String period) {
        Set<Date[]> dateArrays = new HashSet<Date[]>();

        // 拿到日历对象
        Calendar tmpC = Calendar.getInstance();
        Calendar endC = Calendar.getInstance();
        tmpC.setTime(startDay);
        endC.setTime(endDay);

        Date[] dateArray = null;
        // 新建日期数组，用于存放起止日期
        dateArray = new Date[2];
        dateArray[0] = startDay;
        while (tmpC.compareTo(endC) <= 0) {
            // 如果是特定日期（月末，指定的星期数等），确定结束日期。从新建立日期数组，开始求下一个时间段
            if (ifSpecificDate(tmpC, period)) {
                if (dateArray[1] != null) {
                    dateArrays.add(dateArray);
                    dateArray = new Date[2];
                    dateArray[0] = tmpC.getTime();
                }
            } else {
                dateArray[1] = tmpC.getTime();
            }
            // 将日历对象日期+1
            tmpC.add(Calendar.DAY_OF_MONTH, +1);
        }
        // 填最后一个数组
        dateArray[1] = endDay;
        dateArrays.add(dateArray);
        return dateArrays;
    }

    /**
     * 查看此日期是否为特定日期
     */
    public static boolean ifSpecificDate(Calendar c, String period) {

        // 如果日期为本月最后一天，返回true
        if ("month".equalsIgnoreCase(period)) {
            if (c.getActualMinimum(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH)) {
                return true;
            }
        } else if ("SUNDAY".equalsIgnoreCase(period)) {
            if (Calendar.SUNDAY == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        } else if ("MONDAY".equalsIgnoreCase(period)) {
            if ((Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK))) {
                return true;
            }
        } else if ("TUESDAY".equalsIgnoreCase(period)) {
            if (Calendar.TUESDAY == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        } else if ("WEDNESDAY".equalsIgnoreCase(period)) {
            if (Calendar.WEDNESDAY == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        } else if ("THURSDAY".equalsIgnoreCase(period)) {
            if (Calendar.THURSDAY == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        } else if ("FRIDAY".equalsIgnoreCase(period)) {
            if (Calendar.FRIDAY == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        } else if ("SATURDAY".equalsIgnoreCase(period)) {
            if (Calendar.SATURDAY == c.get(Calendar.DAY_OF_WEEK)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 毫秒转化时分秒毫秒
     */
    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }
        return sb.toString();
    }

    /**
     * 毫秒转化天、时
     */
    public static String formatTimeToDayHour(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        return sb.toString();
    }

    /**
     * 获取date日期的前一天
     *
     * @param date
     * @param sf
     * @return
     */
    public static String getYesterday(Date date, String sf) {
        SimpleDateFormat sourceFormat = new SimpleDateFormat(sf);
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.add(Calendar.DAY_OF_MONTH, -1);
        return sourceFormat.format(start.getTime());
    }

    /**
     * 获取date日期的前n天
     *
     * @param date
     * @param sf
     * @param n
     * @return
     */
    public static String getNDaysAgo(Date date, String sf, int n) {
        SimpleDateFormat df = new SimpleDateFormat(sf);
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.add(Calendar.DAY_OF_MONTH, 0 - n);
        return df.format(start.getTime());
    }

    /**
     * 获取date日期的前n月
     *
     * @param date
     * @param sf
     * @param n
     * @return
     */
    public static String getNMonthsAgo(Date date, String sf, int n) {
        SimpleDateFormat df = new SimpleDateFormat(sf);
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.add(Calendar.MONTH, 0 - n);
        return df.format(start.getTime());
    }

    /**
     * 获取date日期的后n天
     *
     * @param date
     * @param sf
     * @param n
     * @return
     */
    public static String getNDaysLater(Date date, String sf, int n) {
        SimpleDateFormat df = new SimpleDateFormat(sf);
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.add(Calendar.DAY_OF_MONTH, 0 + n);
        return df.format(start.getTime());
    }

    /**
     * 转换String日期的格式
     *
     * @param date
     * @return
     */
    public static String getChangeFormat(String date, String srcFormate, String toFormate) {

        SimpleDateFormat df = new SimpleDateFormat(srcFormate);
        SimpleDateFormat toDf = new SimpleDateFormat(toFormate);
        Calendar start = Calendar.getInstance();
        try {
            start.setTime(df.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return toDf.format(start.getTime());
    }

    /*
     *把一个字符串转换为Date类型
     * @param time yyyy-MM-dd
     */
    public static Date string2date(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /*
     *把一个字符串转换为Date类型
     * @param time
     * @param sf
     */
    public static Date stringToDateByFormat(String time, String sf) {
        SimpleDateFormat format = new SimpleDateFormat(sf);
        Date date = null;
        try {
            date = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取指定日期的之前或之后几天的日期
     *
     * @param date
     * @param sf
     * @param n    n小于0时表示之前的日期 你大于0时表示之后的日期
     * @return
     */
    public static String getNDayAgoOrBeforeByDate(String date, String sf, int n) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(sf);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(date));
            calendar.add(Calendar.DATE, n);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateFormat.format(calendar.getTime());
    }

    /**
     * 把set的日期集合转成成带星期的日期集合
     *
     * @param dateSet
     * @return
     */
    public static Set<String> changeSetDateWithWeek(Set<String> dateSet) {
        Set<String> rsSet = new TreeSet<String>();
        for (String sourceDate : dateSet) {
            rsSet.add(
                    "'"
                            + sourceDate
                            + "("
                            + getDayOfWeekChinese(stringToDateByFormat(sourceDate, "yyyyMMdd"))
                            + ")'");
        }
        return rsSet;
    }

    /**
     * 把set的日期集合转成成带星期的日期集合
     *
     * @param dateSet
     * @return
     */
    public static Set<String> changeSetDateWithWeekWithoutQuote(Set<String> dateSet) {
        Set<String> rsSet = new TreeSet<String>();
        for (String sourceDate : dateSet) {
            rsSet.add(
                    sourceDate
                            + "("
                            + getDayOfWeekChinese(stringToDateByFormat(sourceDate, "yyyyMMdd"))
                            + ")");
        }
        return rsSet;
    }

    /**
     * 按照分隔时间 查询当天所有时间点
     *
     * @param splitSecond
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static Set<String> getAllDateTimesBySplit(int splitSecond) {
        Set<String> rsSet = new TreeSet<String>();
        try {

            SimpleDateFormat sourceFormatDate = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sourceFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String nowdate = sourceFormatDate.format(new Date());

            Date startDate = sourceFormatDateTime.parse(nowdate + " 00:00:00");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            long startMill = calendar.getTimeInMillis();
            for (int i = 1; i <= 24 * 60 * 60 / splitSecond; i++) {
                rsSet.add(
                        "'"
                                + TimestampUtil.TimestampToDate(
                                "yyyy-MM-dd HH:mm:ss", startMill + i * splitSecond * 1000)
                                + "'");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rsSet;
    }

    /**
     * 根据秒数区间 分隔点前多少秒
     *
     * @param startDateTime 起始时间
     * @param seconds       减少多少秒
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static Set<String> getAllTimesByAddSplit(String startDateTime, int seconds) {
        Set<String> rsSet = new TreeSet<String>();
        try {

            SimpleDateFormat sourceFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date startDate = sourceFormatDateTime.parse(startDateTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            long startMill = calendar.getTimeInMillis();
            for (int i = 0; i < seconds; i++) {
                rsSet.add(TimestampUtil.TimestampToDate("yyyy-MM-dd HH:mm:ss", startMill - i * 1000));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rsSet;
    }

    /**
     * 获取当前时间当天所有秒数
     *
     * @param offsetSeconds 按照多少秒分隔
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static Set<String> getTimesBySplitToday(int offsetSeconds) {
        Set<String> rsSet = new TreeSet<String>();
        try {

            SimpleDateFormat sourceFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sourceFormatDate = new SimpleDateFormat("yyyy-MM-dd");

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(
                    sourceFormatDateTime.parse(sourceFormatDate.format(new Date()) + " 00:00:00"));
            long startMill = calendar2.getTimeInMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            long nowMill = calendar.getTimeInMillis() - offsetSeconds * 1000;

            for (long i = startMill + 1000; i <= nowMill; i = i + 1000) {
                rsSet.add(TimestampUtil.TimestampToDate("yyyy-MM-dd HH:mm:ss", i));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rsSet;
    }

    /**
     * 获取当前时间当天所有秒数
     *
     * @param offsetSeconds 按照多少秒分隔
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static Set<String> getSafeTimes(int offsetSeconds) {
        Set<String> rsSet = new TreeSet<String>();
        try {

            SimpleDateFormat sourceFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(
                    sourceFormatDateTime.parse(getYesterday(new Date(), "yyyy-MM-dd") + " 00:00:00"));
            long startMill = calendar2.getTimeInMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            long nowMill = calendar.getTimeInMillis() - offsetSeconds * 1000;

            for (long i = startMill + 1000; i <= nowMill; i = i + 1000) {
                rsSet.add(TimestampUtil.TimestampToDate("yyyy-MM-dd HH:mm:ss", i));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rsSet;
    }

    /**
     * 获取指定时间当天所有秒数
     *
     * @param endDateTime yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Set<String> getTimesToday(String endDateTime) {
        Set<String> rsSet = new TreeSet<String>();
        try {

            SimpleDateFormat sourceFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sourceFormatDate = new SimpleDateFormat("yyyy-MM-dd");

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(
                    sourceFormatDateTime.parse(sourceFormatDate.format(new Date()) + " 00:00:00"));
            long startMill = calendar2.getTimeInMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sourceFormatDateTime.parse(endDateTime));
            long nowMill = calendar.getTimeInMillis();

            for (long i = startMill + 1000; i <= nowMill; i = i + 1000) {
                rsSet.add(TimestampUtil.TimestampToDate("yyyy-MM-dd HH:mm:ss", i));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rsSet;
    }

    /**
     * 获取当天所有秒数
     *
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static Set<String> getTimesAllToday() {
        Set<String> rsSet = new TreeSet<String>();
        try {

            SimpleDateFormat sourceFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sourceFormatDate = new SimpleDateFormat("yyyy-MM-dd");

            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(
                    sourceFormatDateTime.parse(sourceFormatDate.format(new Date()) + " 00:00:00"));
            long startMill = calendar2.getTimeInMillis();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(
                    sourceFormatDateTime.parse(getNDaysLater(new Date(), "yyyy-MM-dd", 1) + " 00:00:00"));
            long nowMill = calendar.getTimeInMillis();

            for (long i = startMill + 1000; i <= nowMill; i = i + 1000) {
                rsSet.add(TimestampUtil.TimestampToDate("yyyy-MM-dd HH:mm:ss", i));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rsSet;
    }

    /**
     * 获取今天的指定格式的日期
     *
     * @param format
     * @return
     */
    public static String getTodayDate(String format) {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat(format).format(calendar.getTime());
    }

    public static String getTodayDate(String format, int millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, millis);
        return new SimpleDateFormat(format).format(calendar.getTime());
    }

    /**
     * 比较两个日期的大小
     *
     * @param date1
     * @param date2
     * @param format
     * @return
     */
    public static int compareDate(String date1, String date2, String format) {
        if (date1.equals(date2)) {
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        Date a = null;
        Date b = null;
        try {
            a = sdf.parse(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            b = sdf.parse(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (a.before(b)) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * 从毫秒时间获取格式化时间
     *
     * @param timestamp
     * @param pattern
     * @return
     */
    public static String getDateFromMillis(long timestamp, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return dateFormat.format(calendar.getTime());
    }

    /**
     * 获取每天的分钟集合
     *
     * @param date
     * @param pattern
     * @return
     */
    public static Set<String> getMinuteListByDay(String date, String pattern) {
        Set<String> times = new LinkedHashSet<String>();

        try {
            SimpleDateFormat definedFormat = new SimpleDateFormat(pattern);
            SimpleDateFormat zeroClockDateFormat = new SimpleDateFormat("yyyyMMdd0000");
            SimpleDateFormat currentClockDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(definedFormat.parse(date));

            String startTime = zeroClockDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
            String endTime = zeroClockDateFormat.format(calendar.getTime());

            calendar.setTime(currentClockDateFormat.parse(startTime));

            while (startTime.compareTo(endTime) < 0) {
                times.add(startTime);
                calendar.add(Calendar.MINUTE, 1);
                startTime = currentClockDateFormat.format(calendar.getTime());
            }

            return times;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 判断两个时间点的时间差
     *
     * @param beginTime
     * @param endTime
     * @param format
     * @param type
     * @return
     */
    public static long getTimeDifference(
            String beginTime, String endTime, String format, String type) {
        SimpleDateFormat df = new SimpleDateFormat(format);

        long day = 1000 * 60 * 60 * 24;
        long hour = 1000 * 60 * 60;
        long minute = 1000 * 60;
        long second = 1000;

        long diff = 0;
        try {
            diff = df.parse(endTime).getTime() - df.parse(beginTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long result = 0;
        if ("day".equals(type)) {
            result = diff / day;
        } else if ("hour".equals(type)) {
            result = diff / hour;
        } else if ("minute".equals(type)) {
            result = diff / minute;
        } else if ("second".equals(type)) {
            result = diff / second;
        }

        return result;
    }

    /**
     * 获取一天每隔30s时段的时间集合
     *
     * @return
     */
    public static Set<String> getDateSetByThirtySec(
            String date, String sourcePattern, String tarPattern) {
        SimpleDateFormat sourceFormat = new SimpleDateFormat(sourcePattern);
        SimpleDateFormat tarFormat = new SimpleDateFormat(tarPattern);
        Set<String> times = new TreeSet<String>();

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sourceFormat.parse(date));
            String startTime = tarFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
            String endTime = tarFormat.format(calendar.getTime());
            calendar.setTime(sourceFormat.parse(date));
            while (startTime.compareTo(endTime) < 0) {
                times.add(startTime);
                calendar.add(Calendar.SECOND, 30);
                startTime = tarFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return times;
    }

    /**
     * 将对应时间转化为毫秒数
     *
     * @param date
     * @param pattern
     */
    public static long getMillisFromDate(String date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

        return calendar.getTimeInMillis();
    }

    /**
     * 获取一天每隔十分钟的时间集合
     *
     * @param dayAgo
     * @return
     */
    public static Set<String> getTenMinListByDay(
            String date, String datepattern, String resultpattern) {
        Set<String> times = new TreeSet<String>();
        try {
            SimpleDateFormat definedFormat = new SimpleDateFormat(datepattern);
            SimpleDateFormat zeroClockDateFormat = new SimpleDateFormat("yyyyMMdd000000");
            SimpleDateFormat currentClockDateFormat = new SimpleDateFormat(resultpattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(definedFormat.parse(date));

            String startTime = zeroClockDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
            String endTime = zeroClockDateFormat.format(calendar.getTime());

            calendar.setTime(currentClockDateFormat.parse(startTime));
            while (startTime.compareTo(endTime) < 0) {
                times.add(startTime);
                calendar.add(Calendar.MINUTE, 10);
                startTime = currentClockDateFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return times;
    }

    /**
     * 获取一天每隔指定分钟的时间集合
     *
     * @param dayAgo
     * @return
     */
    public static Set<String> getMinListByDay(
            String date, String datepattern, String resultpattern, int minutes) {
        Set<String> times = new TreeSet<String>();
        try {
            SimpleDateFormat definedFormat = new SimpleDateFormat(datepattern);
            SimpleDateFormat zeroClockDateFormat = new SimpleDateFormat(resultpattern);
            SimpleDateFormat currentClockDateFormat = new SimpleDateFormat(resultpattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(definedFormat.parse(date));

            String startTime = zeroClockDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
            String endTime = zeroClockDateFormat.format(calendar.getTime());

            calendar.setTime(currentClockDateFormat.parse(startTime));
            while (startTime.compareTo(endTime) < 0) {
                times.add(startTime);
                calendar.add(Calendar.MINUTE, minutes);
                startTime = currentClockDateFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return times;
    }

    /**
     * 查看日期是不是今天
     *
     * @param date
     * @param format
     * @return
     */
    public static boolean isToday(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        return dateFormat.format(calendar.getTime()).compareTo(date) == 0;
    }

    /**
     * 判断两个日期是不是同一天
     *
     * @param date1
     * @param formatStr1
     * @param date2
     * @param formatStr2
     * @return
     */
    public static boolean isSameDay(
            String date1, String formatStr1, String date2, String formatStr2) {
        SimpleDateFormat sameFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat format1 = new SimpleDateFormat(formatStr1);
        SimpleDateFormat format2 = new SimpleDateFormat(formatStr2);

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format1.parse(date1));
            date1 = sameFormat.format(calendar.getTime());
            calendar.setTime(format2.parse(date2));
            date2 = sameFormat.format(calendar.getTime());
            if (date1.equals(date2)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    /**
     * 获取两个时间天数差值
     *
     * @return
     */
    public static int getDaysNumBetween(String startDate, String endDate, String pattern) {
        int i = 0;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(dateFormat.parse(startDate));
            long start = calendar.getTimeInMillis();

            calendar.setTime(dateFormat.parse(endDate));
            long end = calendar.getTimeInMillis();

            i = new Long((end - start) / (24 * 60 * 60 * 1000)).intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return i;
    }

    /**
     * 获取当天特殊时刻时间
     *
     * @param dateStr 需为yyyy-MM-dd格式
     * @param type    0-开始时间，1-结束时间
     * @return
     */
    public static Date getSpecialTimeOfTheDay(String dateStr, int type) {
        Date date = null;
        dateStr = 0 == type ? dateStr + " 00:00:00" : dateStr + " 23:59:59";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 获取当天特殊时刻时间
     *
     * @param date 需为yyyy-MM-dd格式
     * @param type 0-开始时间，1-结束时间
     * @return
     */
    public static Date getSpecialTimeOfTheDay2(Date date, int type) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        return getSpecialTimeOfTheDay(format.format(date), type);
    }

    /**
     * 获取包括结束时间在内的，每一个周几的数据
     *
     * @param startDate
     * @param endDate
     * @param sourcePattern
     * @param targetPattern
     * @param weekDay       1：周末，2：周一。。。7：周六
     * @return
     */
    public static List<String> getWeekDateList(
            String startDate, String endDate, String sourcePattern, String targetPattern, int weekDay) {
        List<String> dateList = new ArrayList<String>();

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        SimpleDateFormat sourceFormat = new SimpleDateFormat(sourcePattern);
        SimpleDateFormat targetFormat = new SimpleDateFormat(targetPattern);
        try {
            dateList.add(targetFormat.format(sourceFormat.parse(endDate)));

            startCal.setTime(sourceFormat.parse(startDate));
            endCal.setTime(sourceFormat.parse(endDate));
            endCal.add(Calendar.DAY_OF_YEAR, -1);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateList;
        }

        while (!endCal.before(startCal)) {
            if (endCal.get(Calendar.DAY_OF_WEEK) > weekDay) {
                endCal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                dateList.add(targetFormat.format(endCal.getTime()));
                endCal.add(Calendar.DAY_OF_YEAR, -7);
            }
        }

        return dateList;
    }

    /**
     * 获取每月日期
     *
     * @param startDate
     * @param endDate
     * @param sourcePattern
     * @param targetPattern
     * @return
     */
    public static List<String> getMonthDateList(
            String startDate, String endDate, String sourcePattern, String targetPattern) {
        List<String> dateList = new ArrayList<String>();

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        SimpleDateFormat sourceFormat = new SimpleDateFormat(sourcePattern);
        SimpleDateFormat targetFormat = new SimpleDateFormat(targetPattern);
        try {
            startCal.setTime(sourceFormat.parse(startDate));
            endCal.setTime(sourceFormat.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
            return dateList;
        }

        while (!endCal.before(startCal)) {
            dateList.add(targetFormat.format(endCal.getTime()));
            endCal.add(Calendar.MONTH, -1);
        }
        return dateList;
    }

    /**
     * 得到本周周一
     *
     * @return yyyy-MM-dd
     */
    public static String getMondayOfThisWeek() {

        Calendar c = Calendar.getInstance();
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0) day_of_week = 7;
        c.add(Calendar.DATE, -day_of_week + 1);
        return format.format(c.getTime());
    }

    /**
     * 得到本周周日
     *
     * @return yyyy-MM-dd
     */
    public static String getSundayOfThisWeek() {
        Calendar c = Calendar.getInstance();
        int day_of_week = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (day_of_week == 0) day_of_week = 7;
        c.add(Calendar.DATE, -day_of_week + 7);
        return format.format(c.getTime());
    }

    /**
     * 枚举该天每个小时 返回格式yyyy-MM-dd-HH
     *
     * @return
     */
    public static Set<String> getRangeDashDayOfEveryHour(String date, String sourcePattern) {
        Set<String> treeSet = new TreeSet<String>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(sourcePattern);
        SimpleDateFormat intiDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat returnDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH");

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(intiDateFormat.parse(getChangeFormat(date, sourcePattern, "yyyy-MM-dd")));
            Calendar nextCalendar = Calendar.getInstance();
            nextCalendar.setTime(calendar.getTime());
            nextCalendar.add(Calendar.DAY_OF_MONTH, 1);

            String nextDay = returnDateFormat.format(nextCalendar.getTime());
            String startDate = returnDateFormat.format(calendar.getTime());

            while (startDate.compareTo(nextDay) < 0) {
                treeSet.add(startDate);
                calendar.add(Calendar.HOUR, 1);
                startDate = returnDateFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return treeSet;
    }

    /**
     * 枚举该天每个小时 返回格式yyyyMMddHH
     *
     * @return
     */
    public static Set<String> getRangeDayOfEveryHour(String date, String sourcePattern) {
        Set<String> treeSet = new TreeSet<String>();
        SimpleDateFormat intiDateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat returnDateFormat = new SimpleDateFormat("yyyyMMddHH");

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(intiDateFormat.parse(getChangeFormat(date, sourcePattern, "yyyyMMdd")));
            Calendar nextCalendar = Calendar.getInstance();
            nextCalendar.setTime(calendar.getTime());
            nextCalendar.add(Calendar.DAY_OF_MONTH, 1);

            String nextDay = returnDateFormat.format(nextCalendar.getTime());
            String startDate = returnDateFormat.format(calendar.getTime());

            while (startDate.compareTo(nextDay) < 0) {
                treeSet.add(startDate);
                calendar.add(Calendar.HOUR, 1);
                startDate = returnDateFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return treeSet;
    }

    /**
     * 获取某一天的指定时间间隔的时间列表
     *
     * @param date       日期
     * @param srcPattern 开始与结束时间的时间格式
     * @param dstPattern 返回结果时间格式
     * @param interval   时间间隔
     * @return set
     */
    public static Set<String> getNMinListByDay(
            String date, String srcPattern, String dstPattern, int interval) {
        Set<String> times = new TreeSet<String>();
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstFormat = new SimpleDateFormat(dstPattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(srcFormat.parse(date));

            String startTime = dstFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
            String endTime = dstFormat.format(calendar.getTime());

            times = getNMinListByDay(startTime, endTime, srcPattern, dstPattern, interval);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return times;
    }

    /**
     * 获取指定时间范围内的指定时间间隔的时间列表
     *
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @param srcPattern 开始与结束时间的时间格式
     * @param dstPattern 返回结果时间格式
     * @param interval   时间间隔
     * @return set
     */
    public static Set<String> getNMinListByDay(
            String startTime, String endTime, String srcPattern, String dstPattern, int interval) {
        Set<String> times = new TreeSet<String>();
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstFormat = new SimpleDateFormat(dstPattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dstFormat.parse(startTime));
            while (startTime.compareTo(endTime) < 0) {
                times.add(startTime);
                calendar.add(Calendar.MINUTE, interval);
                startTime = dstFormat.format(calendar.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return times;
    }

    /**
     * 获取n分钟之后的时间
     *
     * @param startTime  当前时间
     * @param srcPattern 当前时间格式
     * @param dstPattern 结果时间格式
     * @param interval   间隔分钟数
     * @return n分钟之后的时间
     */
    public static String getNMinLater(
            String startTime, String srcPattern, String dstPattern, int interval) {
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstFormat = new SimpleDateFormat(dstPattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(srcFormat.parse(startTime));
            calendar.add(Calendar.MINUTE, interval);
            startTime = dstFormat.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startTime;
    }

    /**
     * 获取时间范围类的分钟集合
     *
     * @param beginDate
     * @param endDate
     * @param pattern
     * @param num
     * @return
     */
    public static Set<String> getMinuteList(
            String beginDate, String endDate, String pattern, int num) {
        Set<String> times = new LinkedHashSet<String>();

        try {
            SimpleDateFormat definedFormat = new SimpleDateFormat(pattern);
            SimpleDateFormat zeroClockDateFormat = new SimpleDateFormat("yyyyMMdd0000");
            SimpleDateFormat currentClockDateFormat = new SimpleDateFormat("yyyyMMddHHmm");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(definedFormat.parse(beginDate));
            String startTime = zeroClockDateFormat.format(calendar.getTime());

            calendar.setTime(definedFormat.parse(endDate));
            calendar.add(Calendar.DATE, 1);
            String endTime = zeroClockDateFormat.format(calendar.getTime());

            calendar.setTime(currentClockDateFormat.parse(startTime));

            while (startTime.compareTo(endTime) < 0) {
                times.add(startTime);
                calendar.add(Calendar.MINUTE, num);
                startTime = currentClockDateFormat.format(calendar.getTime());
            }

            return times;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取date日期的前一天
     *
     * @param date
     * @param sf
     * @return
     */
    public static Date getYesterday(Date date) {
        Calendar start = Calendar.getInstance();
        start.setTime(date);
        start.add(Calendar.DAY_OF_MONTH, -1);
        return start.getTime();
    }

    public static Set<String> getMinuteList(String beginDate, String endDate, String pattern) {
        return getMinuteList(beginDate, endDate, pattern, 1);
    }

    public static String getNDaysAgo(String date, String srcFormat, int n, String tarFormat)
            throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(srcFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormat.parse(date));
        calendar.add(Calendar.DAY_OF_YEAR, 0 - n);
        simpleDateFormat = new SimpleDateFormat(tarFormat);
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getNDaysAgo(int n, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 0 - n);
        return simpleDateFormat.format(calendar.getTime());
    }

    /**
     * @author wangkaixuan
     * @date 2018年7月2日 @Description:比较两个日期小时差距 yyyy-MM-dd HH:mm:ss
     */
    public static double diff2DaysHour(String bigDate, String smallDate) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double h = 0.00;
        try {
            Date date1 = sf.parse(bigDate);
            Date date2 = sf.parse(smallDate);
            h = (date1.getTime() - date2.getTime()) / (1000.00 * 60 * 60);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1.00;
        }
        return h;
    }

    /**
     * @author wangkaixuan
     * @date 2018年7月2日 @Description:获取两个日志之间每分钟
     */
    public static List<String> get2DayMinute(String startTime, String endTime) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<String> resList = new ArrayList<String>();
        try {
            Date startDate = sf.parse(startTime);
            Date endDate = sf.parse(endTime);

            Long start = startDate.getTime();
            Long end = endDate.getTime();

            Long now = start;

            if (start > end) {
                return null;
            }

            do {
                resList.add(sf2.format(new Timestamp(now)));
                now += 60 * 1000;
            } while (now <= end);

            return resList;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 取得过去几天的日期，不包括今天
     *
     * @param computedays
     * @return
     */
    public Set<String> getComputeDates(int computedays, String format) {
        return getComputeDates(computedays, new Date(), false, format);
    }

    /**
     * 取得过去几天的日期，是否包含lastDay
     *
     * @param computedays
     * @param lastDay
     * @param include
     * @return
     */
    public Set<String> getComputeDates(
            int computedays, Date lastDay, boolean include, String format) {
        Set<String> dates = new TreeSet<String>();
        // 拿到日历对象
        Calendar c = Calendar.getInstance();
        // 将lastDay格式化，并同步到日历对象中
        String lastDayStr = new SimpleDateFormat("yyyy-MM-dd").format(lastDay);
        c.set(
                new Integer(lastDayStr.split("-")[0]),
                new Integer(lastDayStr.split("-")[1]) - 1,
                new Integer(lastDayStr.split("-")[2]),
                0,
                0,
                0);

        // 如果不包含lastDay
        if (!include) {
            // 循环天数，
            for (int i = 0; i < computedays; i++) {
                // 将日历对象日期-1
                c.add(Calendar.DAY_OF_MONTH, -1);
                // 按传入参数格式化日期为字符串，存入日期集合中
                dates.add(toString(format, c.getTime()));
            }
        } else {
            // 如果包含lastDay，先将最近一天格式化成字符串存入日期集合中
            dates.add(new SimpleDateFormat(format).format(lastDay));
            // 循环 计算天数-1（已经存入了一天了）
            for (int i = 0; i < computedays - 1; i++) {
                // 将日历对象日期-1
                c.add(Calendar.DAY_OF_MONTH, -1);
                // 按传入参数格式化日期为字符串，存入日期集合中
                dates.add(toString(format, c.getTime()));
            }
        }

        return dates;
    }

    /**
     * 按传入格式，将日期转换为字符串
     *
     * @param format
     * @return
     */
    public String toString(String format) {
        return new SimpleDateFormat(format).format(datetime);
    }
}
