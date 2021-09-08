package com.bountyhunter.tomato.concurrent;

public class LongIsNotAtomic {

    private static volatile long value = 0;

    public static void main(String[] args) {
        Thread thread1 = new Thread(new SetValue(0L));
        Thread thread2 = new Thread(new SetValue(1L));
        thread1.start();
        thread2.start();
        while (LongIsNotAtomic.value == 0L || LongIsNotAtomic.value == 1L) {
        }
        System.out.println("================" + LongIsNotAtomic.value);
    }

    public static class SetValue implements Runnable {

        private long value;

        public SetValue(long value) {
            this.value = value;
        }

        @Override
        public void run() {
            for (; ; ) {
                LongIsNotAtomic.value = value;
            }
        }
    }
}
