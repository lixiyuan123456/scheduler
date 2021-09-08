package com.bountyhunter.tomato.concurrent;

import java.util.concurrent.BrokenBarrierException;

public class ClassAndObjectLocker {

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
        ClassAndObjectLocker locker = new ClassAndObjectLocker();
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        locker.test2();
                    }
                })
                .start();
        new Thread(
                new Runnable() {

                    @Override
                    public void run() {
                        locker.test1();
                    }
                })
                .start();
    }

    public void test1() {
        synchronized (this) {
            System.out.println("test1........................");
        }
    }

    public void test2() {
        synchronized (this) {
            System.out.println("test2........................");
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
            }
            System.out.println("test2........................$$$$$$$$$$");
        }
    }
}
