package com.bountyhunter.sharp.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ShellUtils {

  public static void exec(String... command) {
    if (command.length == 0) {
      return;
    }
    System.out.println("shell command : " + Arrays.toString(command));
    Process process;
    try {
      process = new ProcessBuilder(command).start();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    try (InputStream in = process.getInputStream();
        InputStreamReader inReader = new InputStreamReader(in, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(inReader); ) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    try (InputStream error = process.getErrorStream();
        InputStreamReader inReader = new InputStreamReader(error, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(inReader); ) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    try {
      int result = process.waitFor();
      System.out.println("shell command return : " + result);
      if (result != 0) {
        throw new RuntimeException("shell command error code : " + result);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static void execute(String command) throws Exception {
    // Process process = new ProcessBuilder("sh", "-c", command).inheritIO().start();
    Process process = new ProcessBuilder("cmd.exe", command).inheritIO().start();
    try (InputStream in = process.getInputStream();
        InputStreamReader inReader = new InputStreamReader(in, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(inReader); ) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    try (InputStream error = process.getErrorStream();
        InputStreamReader inReader = new InputStreamReader(error, Charset.defaultCharset());
        BufferedReader reader = new BufferedReader(inReader); ) {
      String line = "";
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    int result;
    try {
      result = process.waitFor();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    System.out.println("shell command return : " + result);
    if (result != 0) {
      throw new RuntimeException("shell command error code : " + result);
    }
  }
}
