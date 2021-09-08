package com.bountyhunter.tomato.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@org.springframework.context.annotation.Configuration
@EnableAsync
public class Configuration {

    public static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();

    public static final int THREAD_POOL_CORE_SIZE = 2;

    // IO密集型：2N+1
    // CPU密集型：N+1
    public static final int THREAD_POOL_MAX_SIZE = 2 * CPU_SIZE + 1;

    public static final int THREAD_POOL_KEEP_ALIVE_TIME = 30;

    public static final TimeUnit THREAD_POOL_KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    public static final int THREAD_POOL_QUEUE_SIZE = 1000;

    public static final String SPRING_THREAD_POOL_PREFIX = "spring-thread-pool-";

    @Bean(name = "nativeThreadPool")
    public Executor createNativeThreadPool() {
        ThreadPoolExecutor pool =
                new ThreadPoolExecutor(
                        THREAD_POOL_CORE_SIZE,
                        THREAD_POOL_MAX_SIZE,
                        THREAD_POOL_KEEP_ALIVE_TIME,
                        THREAD_POOL_KEEP_ALIVE_TIME_UNIT,
                        new LinkedBlockingQueue<Runnable>(THREAD_POOL_QUEUE_SIZE),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
        return pool;
    }

    @Bean(name = "threadPool")
    public Executor createThreadPoolExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(THREAD_POOL_CORE_SIZE);
        pool.setMaxPoolSize(THREAD_POOL_MAX_SIZE);
        pool.setThreadNamePrefix(SPRING_THREAD_POOL_PREFIX);
        return pool;
    }
}
