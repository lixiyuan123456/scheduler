package com.naixue.dp.executor;

import com.naixue.dp.util.Configuration;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by sunzhiwei on 2018/1/31.
 */
public class LogCollection extends Thread{
    private static Logger logger = Logger.getLogger(LogCollection.class);

    private InputStream inputStream;
    private String type;
    private String queryName;
    private String errors;

    public LogCollection(InputStream inputStream, String type, String executorId) {
        this.inputStream = inputStream;
        this.type = type;
        this.queryName = executorId;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public void run() {
        logger.info("start collection log of " + this.queryName + " with " + this.type);
//        StringBuilder stringBuilder = new StringBuilder();
        String logFile = Configuration.getConfiguration().get("SCRIPT_LOG_PATH") + "_" + this.queryName + this.type;
        BufferedWriter bufferedWriter = null;
//        StringBuffer stringBuffer = new StringBuffer();
        InputStreamReader inputStreamReader = new InputStreamReader(this.inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 1024);
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true)));
            String line = null;
            try {
                while ((line = bufferedReader.readLine()) != null){
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
//                if (this.type.equalsIgnoreCase("_PROCESS_INFO")){
//                    bufferedWriter.write("queryName - " + this.queryName + " is complete.");
//                    bufferedWriter.newLine();
//                    bufferedWriter.flush();
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (this.type.equalsIgnoreCase("_PROCESS_INFO")) {
                    bufferedWriter.write("queryName - " + this.queryName + " is complete.");
                    bufferedWriter.flush();
                }
                bufferedWriter.close();
                bufferedReader.close();
                inputStreamReader.close();
                this.inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
