package com.naixue.nxdp.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String TIME_FORMAT = "HH:mm:ss";

    public static Date getBeginningOfDay(String date) {
        Date d;
        try {
            d = new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return getBeginningOfDay(d);
    }

    public static Date getBeginningOfDay(Date date) {
        Objects.requireNonNull(date, "请求参数date不允许为空");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LocalDateTime beginning =
                LocalDateTime.of(
                        LocalDate.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.DAY_OF_MONTH)),
                        LocalTime.MIN);
        return Date.from(beginning.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getEndingOfDay(String date) {
        Date d;
        try {
            d = new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return getEndingOfDay(d);
    }

    public static Date getEndingOfDay(Date date) {
        Objects.requireNonNull(date, "请求参数date不允许为空");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        LocalDateTime end =
                LocalDateTime.of(
                        LocalDate.of(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH) + 1, // 0-11
                                calendar.get(Calendar.DAY_OF_MONTH)),
                        LocalTime.MAX);
        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date getDate(Date source, int amount, TimeUnit unit) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(source);
        if (unit == TimeUnit.MILLISECONDS) {
            calendar.add(Calendar.MILLISECOND, amount);
        } else if (unit == TimeUnit.SECONDS) {
            calendar.add(Calendar.SECOND, amount);
        } else if (unit == TimeUnit.MINUTES) {
            calendar.add(Calendar.MINUTE, amount);
        } else if (unit == TimeUnit.HOURS) {
            calendar.add(Calendar.HOUR_OF_DAY, amount);
        } else if (unit == TimeUnit.DAYS) {
            calendar.add(Calendar.DAY_OF_MONTH, amount);
        } else {
            throw new RuntimeException("不支持的时间格式");
        }
        return calendar.getTime();
    }

    public static Date getDate(int amount, TimeUnit unit) {
        return getDate(new Date(), amount, unit);
    }
}
