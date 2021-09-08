package com.naixue.dp.util;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunzhiwei on 2018/1/23.
 */
public class ToolUtil {
    private static Logger logger = Logger.getLogger(ToolUtil.class);

    public static boolean isValidString(String str){
        return str != null && str.length() > 0;
    }

    public static String getToday(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }

    public static boolean writeFile(String file, String data) {
        logger.info("write " + "file - " + file + " ,data - " + data);
        BufferedWriter bw = null;
        boolean flag = true;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            bw.write(data + "\n");
            bw.flush();
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
            logger.info("writeFile;" + "flag:" + flag);
        } catch (IOException e) {
            logger.error("writeFile " + e.getMessage(), e);
            flag = false;
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                flag = false;
            }
        }
        return flag;
    }
}
