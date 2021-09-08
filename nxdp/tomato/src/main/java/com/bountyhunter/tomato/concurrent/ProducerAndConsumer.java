package com.bountyhunter.tomato.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ProducerAndConsumer {

    private static List<String> queue = new ArrayList<>();

    public static void main(String[] args) {
        new Thread(new Consumer(queue)).start();
        new Thread(new Producer(queue)).start();
        new Thread(new Producer(queue)).start();
    }

    public static class Producer implements Runnable {

        private List<String> queue;

        public Producer(List<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queue) {
                    while (queue.size() > 8) {
                        try {
                            System.out.println("生产者" + Thread.currentThread().getName() + "达到最大容量");
                            queue.wait();
                            System.out.println("生产者" + Thread.currentThread().getName() + "退出wait");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e.toString(), e);
                        }
                    }
                    queue.add(UUID.randomUUID().toString());
                    System.out.println("生产者" + Thread.currentThread().getName() + "生产");
                    queue.notifyAll();
                }
            }
        }
    }

    public static class Consumer implements Runnable {
        private List<String> queue;

        public Consumer(List<String> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        try {
                            System.out.println("消费者" + Thread.currentThread().getName() + "队列为空，wait");
                            queue.wait();
                            System.out.println("消费者" + Thread.currentThread().getName() + "退出wait");
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e.toString(), e);
                        }
                    }
                    Iterator<String> iterator = queue.iterator();
                    while (iterator.hasNext()) {
                        System.out.println("消费者" + Thread.currentThread().getName() + "===>" + iterator.next());
                        iterator.remove();
                    }
                    queue.notifyAll();
                }
            }
        }
    }
}
