package com.naixue.spear;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class Main1111 {

    public static void main(String[] args) {
        List<String> list = Lists.newArrayList("a", "b", "c", "a", "b", "c", "a", "b", "c");
        System.out.println(Arrays.toString(list.toArray()));
        Main1111 main = new Main1111();
    /*for (int i = 0; i < list.size(); i++) {
      String key = list.get(i);
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  main.doWork(key);
                }
              })
          .start();
    }*/
        System.out.println("de700fc6-74f9-4d7c-915d-9e7b4a5be978".length());
    }

    void doWork(String key) {
        synchronized (key) {
            System.out.println(Thread.currentThread().getName() + "#" + key + " is started.");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "#" + key + " is end.");
        }
    }
}
