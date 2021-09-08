package com.naixue.spear;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import lombok.AllArgsConstructor;

public class ShellUtils {

    public static void exec(String... commands) {
        System.out.println("##################" + Arrays.toString(commands));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);
        Process process;
        try {
            // process = new ProcessBuilder(commands).inheritIO().start();
            process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString(), e);
        }
        try {
            new Thread(new StreamGobbler(process.getInputStream())).start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
        int code = 0;
        try {
            code = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString(), e);
        }
        if (code != 0) {
            System.out.println("Failure:===>" + code);
        }
    }

    @AllArgsConstructor
    public static class StreamGobbler implements Runnable {

        InputStream in;

        @Override
        public void run() {
            try (InputStream is = in;
                 InputStreamReader inReader = new InputStreamReader(is, Charset.defaultCharset());
                 BufferedReader reader = new BufferedReader(inReader);) {
                long cursor = System.currentTimeMillis();
                String line = "";
                while (true) {
                    if (System.currentTimeMillis() - cursor > 10000) {
                        System.out.println("time out !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        break;
                    }
                    if ((line = reader.readLine()) == null) {
                        System.out.println("read line is null !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        break;
                    }
                    cursor = System.currentTimeMillis();
                    System.out.println("*******************************" + line + System.lineSeparator());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage(), e);
            }
            System.out.println("+++++++++++++++++++++++++end++++++++++++++++++++++++++++++++");
        }
    }
}
