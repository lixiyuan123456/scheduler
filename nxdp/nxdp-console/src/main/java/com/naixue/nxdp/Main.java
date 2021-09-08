package com.naixue.nxdp;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.naixue.nxdp.thirdparty.WXHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.bj58.zhuanzhuan.zmonitor.javaclient.ZMonitor;
import com.bj58.zhuanzhuan.zzarch.common.util.SystemEnvUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableRetry(proxyTargetClass = true)
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
        log.info("zzdp instance is deployed on " + SystemEnvUtil.getIp());
        ZMonitor.init("zzdp");
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        WXHelper.asyncSendWXMsg("zzdp instance is shutdown now.", "zhaichuancheng");
                                        log.info("------>>>zzdp instance is shutdown now.");
                                    }
                                }));
    }

    @Slf4j
    @Configuration
    public static class ThreadPool {

        public static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();

        // IO密集型：2N+1
        // CPU密集型：N+1
        public static final int THREAD_POOL_CORE_POOL_SIZE = CPU_SIZE + 1;

        // 当队列存放的任务满了，线程池会创建新的线程，最大数量为设置的此值，若队列为无界队列，此参数无效
        public static final int THREAD_POOL_MAX_POOL_SIZE = THREAD_POOL_CORE_POOL_SIZE;

        public static final int THREAD_POOL_KEEP_ALIVE_SECONDS = 30;

        // 有界队列size
        public static final int THREAD_POOL_QUEUE_SIZE = 1000;

        public static final String THREAD_POOL_NAME_PREFIX = "SPRING-THREAD-POOL-";

        public static final boolean THREAD_POOL_FORCE_SHUTDOWN = false;

        public static final int THREAD_POOL_FORCE_SHUTDOWN_TIME = 60;

        @Bean(name = "taskExecutor")
        public Executor createThreadPool() {
            ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
            pool.setCorePoolSize(THREAD_POOL_CORE_POOL_SIZE);
            pool.setMaxPoolSize(THREAD_POOL_MAX_POOL_SIZE);
            pool.setQueueCapacity(THREAD_POOL_QUEUE_SIZE);
            pool.setKeepAliveSeconds(THREAD_POOL_KEEP_ALIVE_SECONDS);
            pool.setThreadNamePrefix(THREAD_POOL_NAME_PREFIX);
            pool.setWaitForTasksToCompleteOnShutdown(!THREAD_POOL_FORCE_SHUTDOWN);
            pool.setAwaitTerminationSeconds(THREAD_POOL_FORCE_SHUTDOWN_TIME);
            pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
            log.info(
                    "threadpool--->CorePoolSize={},MaxPoolSize={},QueueCapacity={},KeepAliveSeconds={}",
                    THREAD_POOL_CORE_POOL_SIZE,
                    THREAD_POOL_MAX_POOL_SIZE,
                    THREAD_POOL_QUEUE_SIZE,
                    THREAD_POOL_KEEP_ALIVE_SECONDS);
            return pool;
        }
    }

    @Configuration
    public static class JacksonWebMvcConfigurer extends WebMvcConfigurerAdapter {

        private MappingJackson2HttpMessageConverter jacksonMessageConverter() {
            Hibernate5Module module = new Hibernate5Module();
            module.configure(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION, false);
            MappingJackson2HttpMessageConverter messageConverter =
                    new MappingJackson2HttpMessageConverter();
            ObjectMapper mapper = messageConverter.getObjectMapper();
            mapper.registerModule(module);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            mapper.setTimeZone(TimeZone.getDefault());
            return messageConverter;
        }

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(jacksonMessageConverter());
            super.configureMessageConverters(converters);
        }
    }

    @Slf4j
    @ControllerAdvice
    public static class GlobalExceptionHandler {

        @ExceptionHandler
        @ResponseBody
        public Object handleException(
                HttpServletRequest request, HttpServletResponse response, Exception ex) {
            log.error(ex.toString(), ex);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            if (ex instanceof ConstraintViolationException) {
                String message = processValidationException((ConstraintViolationException) ex);
                result.put("data", message);
                result.put("message", message);
                result.put("msg", message);
            } else {
                result.put("data", ex.getMessage());
                result.put("message", ex.getMessage());
                result.put("msg", ex.getMessage());
            }
            return result;
        }

        private String processValidationException(ConstraintViolationException ex) {
            Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
            if (CollectionUtils.isEmpty(violations)) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<?> violation : violations) {
                sb.append(violation.getMessage()).append(";");
            }
            return sb.toString();
        }
    }
}
