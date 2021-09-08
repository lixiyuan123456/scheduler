package com.bountyhunter.tomato.concurrent;

import org.springframework.util.DigestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ThreadLocalTest {

    private String name;
    private ThreadLocalTest test = this;

    public ThreadLocalTest() {
    }

    public ThreadLocalTest(String name) {
        this.name = name;
    }

    public static void main(String[] args) throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String sql =
                "update t_job_execute_log set job_state=4,"
                        + "excute_end_time=now(),excute_kill_time=null,update_time=now(),target_server='"
                        + ip
                        + "' where id = '"
                        + 123
                        + "';";
        System.out.println(sql);
        System.out.println(DigestUtils.md5Digest("abc".getBytes()));
    }

    public static class Counter implements Runnable {

        private String name;

        private int val;

        Counter(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            val = val + 1;
            val = val + 1;
            val = val + 1;
            System.out.println(name + val);
        }
    }
}
