package com.naixue.dp.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.IOException;


/**
 * Created by sunzhiwei on 2018/1/19.
 */
public class CheckRM implements Runnable {
    private static Logger logger = Logger.getLogger(CheckRM.class);
    public static volatile boolean isReady = true;

    public void run() {
        logger.info("checkRM start...");
        String hostA = Configuration.getConfiguration().get("RM_ACTIVE");
        String portA = Configuration.getConfiguration().get("RM_ACTIVE_HOST");
        String hostS = Configuration.getConfiguration().get("RM_STANDBY");
        String portS = Configuration.getConfiguration().get("RM_STANDBY_HOST");
        double percent = Double.parseDouble(Configuration.getConfiguration().get("PERCENT"));
        logger.debug("RM host : " + hostA + ", port : " + portA
                + "; hostS : " + hostS + ", portS :" + portS);
        String restResourceUrl = Configuration.getConfiguration().get("REST_RESOURCE_URL");
        boolean isActive = true;
        String restUrl = "http://" + hostA + ":" + portA + restResourceUrl;
        while (true){
            try {
                logger.debug("restUrl is " + restUrl);
                JSONObject resource = JSON.parseObject(HttpUtil.sendGet(restUrl)).getJSONObject("clusterMetrics");
                logger.debug("yarn's status is : " + resource.toString());

                long totalMem = resource.getLong("totalMB");
                long usedMem = resource.getLong("allocatedMB");

                long totalCpu = resource.getLong("totalVirtualCores");
                long usedCpu = resource.getLong("allocatedVirtualCores");

                logger.debug("totalMem is " + totalMem + ", usedMem is " + usedMem
                        + "and totalCpu is " + totalCpu + ", usedCpu is " + usedCpu + " in YARN");
                if ((totalMem * percent < usedMem) ||
                        totalCpu * percent < usedCpu){
                    logger.warn("yarn is busy, please waiting...");
                    this.isReady = false;
                }else {
                    this.isReady = true;
                }
                logger.debug("Now YARN can schedule " + this.isReady);

            } catch (IOException e) {
                logger.error(e.getMessage() + ", changed", e);
                if (isActive){
                    restUrl = "http://" + hostS + ":" + portS + restResourceUrl;
                    isActive = false;
                }else {
                    restUrl = "http://" + hostA + ":" + portA + restResourceUrl;
                    isActive = true;
                }
            } catch (Exception e) {
                logger.error("check RM error " + e.getMessage(), e);
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.error("CheckRM " + e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args){
        PropertyConfigurator.configure(System.getProperty("user.dir")
                + "/conf/log4j.properties");
        CheckRM checkRM = new CheckRM();
        Thread thread = new Thread(checkRM, "CheckRMThread");
        thread.start();
    }
}
