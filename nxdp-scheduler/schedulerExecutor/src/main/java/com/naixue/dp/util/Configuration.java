package com.naixue.dp.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Created by sunzhiwei on 2018/1/26.
 */
public class Configuration {
    private static Logger logger = Logger.getLogger(Configuration.class);

    private static Properties properties = new Properties();
    private static Configuration configuration ;

    public static Configuration getConfiguration(){
        if (configuration == null){
            synchronized (Configuration.class){
                if (configuration == null){
                    configuration = new Configuration();
                }
            }
        }
        return configuration;
    }

    public String get(String key){
        return (String)properties.get(key);
    }
    private Configuration(){
        String filePath = System.getProperty("user.dir") + "/conf/executor.properties";
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            properties.load(in);
        } catch (FileNotFoundException e) {
            logger.error(e.toString());
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
}
