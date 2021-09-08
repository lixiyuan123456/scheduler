package com.bountyhunter.sharp;

import com.bountyhunter.sharp.util.ShellUtils;

public class Main {
  public static void main(String[] args) throws Exception {
    System.err.println("begin......");
    ShellUtils.execute("java -version");
    System.out.println("end......");
  }
}
