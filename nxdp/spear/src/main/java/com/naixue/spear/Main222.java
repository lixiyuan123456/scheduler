package com.naixue.spear;

import java.sql.ResultSet;

import com.naixue.spear.util.JdbcUtils;
import com.naixue.spear.util.JdbcUtils.ResultSetProcessor;

public class Main222 {

    public static void main(String[] args) {
        for (int i = 0; i < 200; i++) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            JdbcUtils.builder()
                                    .url("jdbc:mysql://10.48.186.32:3306/zzdp")
                                    .username("root")
                                    .password("UDP@mj505")
                                    .build()
                                    .executeQuery(
                                            new ResultSetProcessor() {
                                                @Override
                                                public void processResultSet(ResultSet resultSet) throws Exception {
                                                }
                                            },
                                            "select 1");
                        }
                    })
                    .start();
        }
        while (true) {
        }
    }
}
