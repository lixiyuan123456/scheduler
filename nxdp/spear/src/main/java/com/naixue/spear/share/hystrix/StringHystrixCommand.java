package com.naixue.spear.share.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixThreadPoolProperties;

public class StringHystrixCommand extends HystrixCommand<String> {

    private HystrixService service;

    public StringHystrixCommand() {
        // super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));//最简单的初始化方式
        super(
                Setter.withGroupKey(
                        HystrixCommandGroupKey.Factory.asKey(
                                "StringHystrixCommandGroup")) // 所属的group，一个group共用线程池。默认值：getClass().getSimpleName();
                        .andCommandKey(HystrixCommandKey.Factory.asKey("UUIDGenerator")) // 默认值：当前执行方法名
                        .andCommandPropertiesDefaults(
                                HystrixCommandProperties.Setter()
                                        .withExecutionIsolationStrategy(
                                                ExecutionIsolationStrategy.THREAD) // 隔离策略，有THREAD和SEMAPHORE
                                        // THREAD - 它在单独的线程上执行，并发请求受线程池中的线程数量的限制
                                        // SEMAPHORE - 它在调用线程上执行，并发请求受到信号量计数的限制
                                        // 默认使用THREAD模式
                                        // 以下几种场景可以使用SEMAPHORE模式：
                                        // 只想控制并发度
                                        // 外部的方法已经做了线程隔离
                                        // 调用的是本地方法或者可靠度非常高、耗时特别小的方法（如medis）
                                        // .withExecutionTimeoutInMilliseconds(10000) // 超时时间
                                        .withCircuitBreakerRequestVolumeThreshold(10) // 至少有10个请求，熔断器才进行错误率的计算
                                        .withCircuitBreakerSleepWindowInMilliseconds(
                                                5000) // 熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
                                        .withCircuitBreakerErrorThresholdPercentage(50) // 错误率达到50开启熔断保护
                                        .withExecutionTimeoutEnabled(true)) // HystrixCommand.run()执行是否应该有超时。
                        .andThreadPoolPropertiesDefaults(
                                HystrixThreadPoolProperties.Setter().withCoreSize(10)));
        this.service = HystrixService.getInstance();
    }

    @Override
    protected String run() throws Exception {
        return service.getUUID();
    }

    @Override
    protected String getFallback() {
        return "no uuid is available";
    }
}
