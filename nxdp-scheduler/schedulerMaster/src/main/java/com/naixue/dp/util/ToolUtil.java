package com.naixue.dp.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunzhiwei on 2018/1/23.
 */
public class ToolUtil {

    public static boolean isValidString(String str){
        return str != null && str.length() > 0;
    }

    public static String getToday(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }

    public static String getDate(){
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        // 24小时制
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }

    public static String dateFormat(Date date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    public static long getTime(){
        return System.currentTimeMillis();
    }

    public static void main(String[] args){
        Date date = new Date(2018,3,1,15,4,4);
        System.out.print(ToolUtil.dateFormat(date));
    }
}
