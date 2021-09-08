package com.naixue.spear.share.hystrix;

public class HystrixMain {

    public static void main(String[] args) {
        for (int i = 0; i < 200; i++) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String uuid = new StringHystrixCommand().execute();
                            System.out.println(uuid);
                        }
                    })
                    .start();
        }
    }
}
