package com.bountyhunter.tomato.degrade;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Date;

public class GuavaRateLimiter {

    static RateLimiter limiter = RateLimiter.create(2);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            if (!GuavaRateLimiter.limiter.tryAcquire()) {
                continue;
            }
            new Thread(new Runner()).start();
        }
    }

    public static class Runner implements Runnable {

        @Override
        public void run() {
            System.out.println(new Date());
        }
    }
}
