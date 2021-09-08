package com.naixue.spear.share.hystrix;

import java.util.UUID;

public class HystrixService {

    private HystrixService() {
    }

    public static HystrixService getInstance() {
        return new SingleInstance().instance;
    }

    public String getUUID() {
    /*try {
      Thread.sleep(2000);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }*/
        return UUID.randomUUID().toString();
    }

    public static class SingleInstance {
        private HystrixService instance = new HystrixService();
    }
}
