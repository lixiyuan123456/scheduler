package com.bountyhunter.tomato.designpattern;

public class Singleton {

    private static Singleton singleton;

    private String name;

    private Singleton() {
    /*try {
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }*/
    }

    private Singleton(String name) {
        this.name = name;
    /*try {
      Thread.sleep(2000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }*/
    }

    public static Singleton getInstance() {
        if (singleton == null) {
            synchronized (Singleton.class) {
                if (singleton == null) {
                    singleton = new Singleton("good");
                }
            }
        }
        return singleton;
    }

    public static void main(String[] args) {
        Thread thread1 = new CreativeThread();
        Thread thread2 = new CreativeThread();
        Thread thread3 = new CreativeThread();
        Thread thread4 = new CreativeThread();
        Thread thread5 = new CreativeThread();
        Thread thread6 = new CreativeThread();
        Thread thread7 = new CreativeThread();
        Thread thread8 = new CreativeThread();
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
        thread6.start();
        thread7.start();
        thread8.start();
    }

    public void hello() {
        System.out.println("hello");
        System.out.println(name);
    }

    public static class CreativeThread extends Thread {
        @Override
        public void run() {
            Singleton singleton = Singleton.getInstance();
            singleton.hello();
        }
    }
}
