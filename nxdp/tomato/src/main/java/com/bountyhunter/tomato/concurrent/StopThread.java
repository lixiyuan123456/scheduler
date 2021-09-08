package com.bountyhunter.tomato.concurrent;

public class StopThread {

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runner());
        thread.start();
        Thread.sleep(3000L);
        thread.interrupt();
    }

    public static class Runner implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("end!!!");
        }
    }
}
