package com.naixue.dp.common.util;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShellUtils {

    public static String exec(String... commands) {
    /*log.info("shell command is {}", Arrays.toString(commands));
    ProcessBuilder processBuilder = new ProcessBuilder(commands);
    processBuilder.redirectErrorStream(true);
    Process process;
    try {
      // process = new ProcessBuilder(commands).inheritIO().start();
      process = processBuilder.start();
    } catch (Exception e) {
      log.error(e.toString(), e);
      throw new RuntimeException(e.toString(), e);
    }
    StringBuilder sb = new StringBuilder();
    try (InputStream in = process.getInputStream();
        InputStreamReader inReader = new InputStreamReader(in, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(inReader); ) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        sb.append(line).append(System.lineSeparator());
      }
    } catch (Exception e) {
      log.error(e.toString(), e);
      throw new RuntimeException(e.getMessage(), e);
    }*/
    /*try (InputStream error = process.getErrorStream();
        InputStreamReader inReader = new InputStreamReader(error, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(inReader); ) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        sb.append(line).append(System.lineSeparator());
      }
    } catch (Exception e) {
      log.error(e.toString(), e);
      throw new RuntimeException(e.getMessage(), e);
    }*/
        StreamGobbler streamGobbler =
                new StreamGobbler(System.currentTimeMillis() + "", new StringBuilder()) {
                    @Override
                    public void handle(String data) {
                        ((StringBuilder) this.getHandler()).append(data).append(System.lineSeparator());
                    }
                };
        exec(streamGobbler, commands);
        return streamGobbler.getHandler().toString();
    }

    public static void exec(StreamGobbler streamGobbler, String... commands) {
        log.info("shell command is {}", Arrays.toString(commands));
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);
        Process process;
        try {
            // process = new ProcessBuilder(commands).inheritIO().start();
            process = processBuilder.start();
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        }
        streamGobbler.setProcess(process);
        streamGobbler.start();
        try {
            process.waitFor(5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error(e.toString(), e);
            throw new RuntimeException(e.toString(), e);
        } finally {
            streamGobbler.interrupt();
        }
    }

    public static String exec(String command) {
        Preconditions.checkNotNull(command);
        return exec("sh", "-c", command);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        StreamGobbler streamGobbler =
                new StreamGobbler(System.currentTimeMillis() + "", new StringBuilder()) {
                    @Override
                    public void handle(String data) {
                        ((StringBuilder) this.getHandler()).append(data).append(System.lineSeparator());
                    }
                };
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (System.currentTimeMillis() - start > 20000L) {
                                System.out.println("------------------------" + streamGobbler.isAlive());
                                streamGobbler.interrupt();
                                break;
                            }
                        }
                    }
                })
                .start();
        ShellUtils.exec(streamGobbler, "ping", "www.baidu.com");
        // ShellUtils.exec(streamGobbler, "ping", "www.baidu.com", "-t");
        System.out.println(streamGobbler.getHandler().toString());
    }

    public abstract static class StreamGobbler extends Thread {

        private String name;

        @Getter
        private Object handler;

        @Setter
        private Process process;

        // @Getter private StringBuilder StringBuilder;
        // @Getter private String stringUnit;

        public StreamGobbler(final String name, final Object realHandler) {
            this.name = name;
            this.handler = realHandler;
            // this.StringBuilder = new StringBuilder();
        }

        public abstract void handle(final String data);

        @Override
        public void run() {
            try (InputStream in = process.getInputStream();
                 InputStreamReader inReader = new InputStreamReader(in, Charset.defaultCharset());
                 BufferedReader reader = new BufferedReader(inReader);) {
                long cursor = System.currentTimeMillis();
                String line = "";
                while (true) {
                    if (System.currentTimeMillis() - cursor > 10000) {
                        log.info(
                                "StreamGobbler thread[name={}] is vain run over 10 seconds !!!!!!!!!!!!!!!!!!!!!!!!!!!!",
                                this.name);
                        break;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        log.info(
                                "StreamGobbler thread[name={}] is interrupted !!!!!!!!!!!!!!!!!!!!!!!!!!!!",
                                this.name);
                        break;
                    }
                    if ((line = reader.readLine()) == null) {
                        log.info(
                                "StreamGobbler thread[name={}] reads a null line !!!!!!!!!!!!!!!!!!!!!!!!!!!!",
                                this.name);
                        break;
                    }
                    cursor = System.currentTimeMillis();
                    // this.StringBuilder.append(line).append(System.lineSeparator());
                    // this.stringUnit = line;
                    handle(line);
                }
                log.info("StreamGobbler thread[name={}] is end !!!!!!!!!!!!!!!!!!!!!!!!!!!!", this.name);
            } catch (Exception e) {
                log.error(
                        "StreamGobbler thread[name={}] is error,error is {}.", this.name, e.toString(), e);
                throw new RuntimeException(e.getMessage(), e);
            } finally {
                process.destroy();
            }
        }
    }
}
