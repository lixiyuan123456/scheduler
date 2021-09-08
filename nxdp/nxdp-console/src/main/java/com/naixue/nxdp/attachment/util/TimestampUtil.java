package com.naixue.nxdp.attachment.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间戳工具类
 *
 * @author zhangyecheng
 */
public class TimestampUtil {

    /**
     * 把int类型的时间戳转换成对应sf格式的字符串
     *
     * @param sf
     * @param i
     * @return
     */
    public static String TimestampToDate(String sf, int i) {
        SimpleDateFormat sdf = new SimpleDateFormat(sf);
        String date = sdf.format(new Date(i * 1000L));
        return date;
    }

    /**
     * 把long类型的时间戳转换成对应sf格式的字符串
     *
     * @param sf
     * @param i
     * @return
     */
    public static String TimestampToDate(String sf, long i) {
        SimpleDateFormat sdf = new SimpleDateFormat(sf);
        String date = sdf.format(new Date(i));
        return date;
    }

    /**
     * 把指定sf格式的日期转换成long的时间戳
     *
     * @param sf
     * @param date
     * @return
     */
    public static long DateToTimestamp(String sf, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat(sf);
        long timeStemp = 0;
        try {
            timeStemp = sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeStemp;
    }

    public static void main(String[] args) {
        System.out.println(TimestampUtil.DateToTimestamp("yyyy-MM-dd HH:mm:ss", "2016-07-26 00:00:00"));
        System.out.println(TimestampUtil.TimestampToDate("yyyy-MM-dd HH:mm:ss", 1469461426833L));
        System.out.println(Integer.parseInt(TimestampUtil.TimestampToDate("HH", System.currentTimeMillis())) > 6);
    }
}
