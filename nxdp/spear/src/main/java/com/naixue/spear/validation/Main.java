package com.naixue.spear.validation;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.naixue.spear.core.ThreadPool;
import com.naixue.spear.dao.SQLMapper;
import com.naixue.spear.util.SqlSessionTemplate;

public class Main {

    public static void main(String[] args) {
        ThreadPool.getThreadPool().submit(new Worker());
    }

    public static class Worker implements Runnable {

        @Override
        public void run() {
            while (true) {
                ThreadPool.getThreadPool().submit(new SlowWorker());
                try {
                    Thread.sleep(1000L);
                } catch (Exception e) {
                }
            }
        }
    }

    public static class SlowWorker implements Runnable {

        @Override
        public void run() {
            try {
                List<Map<String, Object>> list =
                        SqlSessionTemplate.getInstance().getMapper(SQLMapper.class).findByStatus(1);
                System.out.println(JSON.toJSONString(list));
                Thread.sleep(2000L);
            } catch (Exception e) {
            }
        }
    }
}
