package com.bountyhunter.tomato.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThreadPoolService {

    @Async("nativeThreadPool")
    public void sayHello1() {
        log.debug("Native thread begin");
        System.out.println("hello1");
        log.debug("Native thread end");
    }

    @Async("threadPool")
    public void sayHello2() {
        log.debug("Spring thread begin");
        System.out.println("hello2");
        log.debug("Spring thread end");
    }
}
