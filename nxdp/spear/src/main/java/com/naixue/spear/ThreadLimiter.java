package com.naixue.spear;

public class ThreadLimiter {

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(new Runner());
        t.start();
        t.join();
        System.out.println("over!!!");
    }

    public static class Runner implements Runnable {

        @Override
        public void run() {
            System.out.println("go!!!");
        }
    }
}
