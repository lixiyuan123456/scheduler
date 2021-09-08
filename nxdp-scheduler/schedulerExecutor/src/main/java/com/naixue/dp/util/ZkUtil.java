package com.naixue.dp.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Created by sunzhiwei on 2018/4/28.
 */
public class ZkUtil {
    private static Logger logger = Logger.getLogger(ZkUtil.class);

    public static boolean delZKNodeWithLock(CuratorFramework zkClinet, String path, String node){
        InterProcessMutex lock = new InterProcessMutex(zkClinet, path);
        boolean result = false;
        for (int i=0; i<5; i++) {
            if (result == true){
                break;
            }
            try {
                if (lock.acquire(15L, TimeUnit.SECONDS)) {
                    zkClinet.delete().forPath(path + "/" + node);
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                logger.error("get lock or delete fail - ", e);
            } finally {
                try {
                    lock.release();
                } catch (Exception e) {
                    logger.error("release lock fail - ", e);
                }
            }
        }
        return result;
    }
}
