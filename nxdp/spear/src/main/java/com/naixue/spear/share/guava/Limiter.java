package com.naixue.spear.share.guava;

import com.google.common.util.concurrent.RateLimiter;

public class Limiter {

    public static void main(String[] args) throws InterruptedException {
        RateLimiter limiter = RateLimiter.create(3);
        System.out.println(limiter.acquire(1));
        System.out.println(limiter.acquire(1));
        System.out.println(limiter.acquire(1));
        Thread.sleep(2000);
        System.out.println(limiter.acquire(5));
        System.out.println(limiter.acquire(1));
        System.out.println(limiter.acquire(1));
        System.out.println(limiter.acquire(1));
        System.out.println();
    }
}
