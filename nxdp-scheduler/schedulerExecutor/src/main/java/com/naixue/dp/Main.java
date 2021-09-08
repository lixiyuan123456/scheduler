package com.naixue.dp;

import org.apache.log4j.Logger;

import com.naixue.dp.executor.ReadyWatcher;
import com.naixue.dp.util.CheckRM;

/** Created by sunzhiwei on 2018/1/26. */
public class Main {
  private static Logger logger = Logger.getLogger(Main.class);

  public static void main(String[] args) {
    Thread checkRMThread = new Thread(new CheckRM(), "checkRMThread");
    logger.info("executor ckeckRM thread start...");
    checkRMThread.start();
    Thread readyWatcherThread = new Thread(new ReadyWatcher(), "readyWatcherThread");
    logger.info("executor readyWatcher thread start");
    readyWatcherThread.start();
    try {
      readyWatcherThread.join();
    } catch (InterruptedException e) {
      logger.error("main ", e);
    }
  }
}
