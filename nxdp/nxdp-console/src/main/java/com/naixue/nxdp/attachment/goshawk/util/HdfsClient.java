package com.naixue.nxdp.attachment.goshawk.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sunzhiwei on 2018/11/1.
 */
@Slf4j
public class HdfsClient {

    // private static Logger logger = Logger.getLogger(HdfsClient.class);

    private static final long ONE_MB = 1024 * 1024L;

    public static void main(String[] args) {
        HdfsClient hdfsClient = new HdfsClient();
//        boolean isFile = hdfsClient.checkFile("/szw/key");
//        System.out.println(isFile);
//        int partitionNum = (int) Math.ceil((double)(130*ONE_MB)/(128l*ONE_MB));
//        int partitionNum = hdfsClient.partitionNum("/szw/key");
//        System.out.println(partitionNum);
        //boolean mvResult = hdfsClient.deleteSmallFiles("/s1");
        //System.out.println(mvResult);
    }

    // check目录下是否都是文件
    public static boolean checkFile(String path) {
        boolean result = true;
        Configuration configuration = new Configuration();
//        configuration.set("fs.defaultFS", "hdfs://localhost");
        try (FileSystem fileSystem = FileSystem.get(configuration)) {
            Path filePath = new Path(path);
            FileStatus[] fileArray = fileSystem.listStatus(filePath);
            for (FileStatus file : fileArray) {
                if (!file.isFile()) {
                    result = false;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return result;
    }

    // 删除原来小文件
    public static boolean deleteSmallFiles(String path, FileSystem fs) {
        boolean result = false;
        //Configuration configuration = new Configuration();
//        configuration.set("fs.defaultFS", "hdfs://localhost");
        try {
            Path filePath = new Path(path);
            log.info("delete path : " + path);
            result = fs.delete(filePath, true);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return result;
    }

    // mv目标path
    public static boolean mvPath(String src, String dst, FileSystem fs) {
        boolean result = false;
        //Configuration configuration = new Configuration();
//        configuration.set("fs.defaultFS", "hdfs://localhost");
        try {
            Path srcPath = new Path(src);
            Path dstPath = new Path(dst);
            log.info("mv path: " + src + " -> " + dst);
            result = fs.rename(srcPath, dstPath);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return result;
    }

//        try {
//            FileStatus[] status = fs.listStatus(filePath);
//            for (FileStatus st : status) {
//                fs.delete(st.getPath(), false);
//            }
//            fs.delete(filePath, false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    // 合并parquet文件的分区数
    public int partitionNum(String path) {
        long total = 0;
        int partitionNum = 1;
        Configuration configuration = new Configuration();
//        configuration.set("fs.defaultFS", "hdfs://localhost");
        try (FileSystem fileSystem = FileSystem.get(configuration)) {
            Path filePath = new Path(path);
            FileStatus[] fileArray = fileSystem.listStatus(filePath);
            for (FileStatus file : fileArray) {
                total += file.getLen();
            }
            partitionNum = (int) Math.ceil((double) total / (128l * ONE_MB));
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return partitionNum;
    }

}
