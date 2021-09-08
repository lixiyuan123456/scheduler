package com.bountyhunter.tomato.concurrent;

public class VolatileTest {

    public static void main(String[] args) {
        Runner runner = new Runner();
        new Thread(runner).start();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.toString(), e);
        }
        runner.cancel();
    }

    public static class Runner implements Runnable {

        // 如果不加volatile程序不会终止
        // private boolean cancel = false;

        private volatile boolean cancel = false;

        @Override
        public void run() {
            while (!cancel) {
                if (doBusiness()) {
                    break;
                }
            }
            if (cancel) {
                System.out.println("cancel");
            } else {
                System.out.println("done");
            }
        }

        private boolean doBusiness() {
            boolean done = false;
            // System.out.println("执行中..........");
            return done;
        }

        public void cancel() {
            cancel = true;
            System.out.println(this + "canceled");
        }
    }
}
