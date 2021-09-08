package com.naixue;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

  private ThreadPool() {}

  private static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();

  // IO密集型：2N+1
  // CPU密集型：N+1
  private static final int THREAD_POOL_CORE_POOL_SIZE = 2 * CPU_SIZE + 1;

  // 当队列存放的任务满了，线程池会创建新的线程，最大数量为设置的此值，若队列为无界队列，此参数无效
  private static final int THREAD_POOL_MAX_POOL_SIZE = THREAD_POOL_CORE_POOL_SIZE + 10;

  private static final int THREAD_POOL_KEEP_ALIVE_SECONDS = 30;

  // 有界队列size
  private static final int THREAD_POOL_QUEUE_SIZE = 1000;

  private static class Singleton {
    private static final ThreadPoolExecutor threadPool =
        new ThreadPoolExecutor(
            THREAD_POOL_CORE_POOL_SIZE,
            THREAD_POOL_MAX_POOL_SIZE,
            THREAD_POOL_KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(THREAD_POOL_QUEUE_SIZE),
            new ThreadPoolExecutor.CallerRunsPolicy());
  }

  public static final ThreadPoolExecutor getThreadPool() {
    return Singleton.threadPool;
  }
}
