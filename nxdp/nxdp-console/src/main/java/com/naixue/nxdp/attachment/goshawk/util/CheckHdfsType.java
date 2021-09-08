package com.naixue.nxdp.attachment.goshawk.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by sunzhiwei on 2019/1/16.
 */
public class CheckHdfsType {

    public static void main(String[] args) {
//        System.out.println(checkHdfsFileType("/Users/hunhun/Downloads/-r-00051.gz.parquet"));
//        System.out.println(checkHdfsFileType("/Users/hunhun/Downloads/part-00000-4b9b3008-4540-4e02-af93-b47f2db67e6b.c000"));
        System.out.println(checkHdfsFileType("/Users/hunhun/Downloads/000044_0"));
        Configuration hadoopConf = new Configuration();
        hadoopConf.set("fs.defaultFS", "hdfs://10.148.15.6");
        try {
            FileSystem fs = FileSystem.get(hadoopConf);
            listFile("/home/zdp/warehouse/hdp_zhuanzhuan_dw_global/dw_log_server_action_1h/2019-01-15/12", fs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listFile(String filePath, FileSystem fileSystem) throws IOException {
        Path path = new Path(filePath);
        FileStatus[] stats = fileSystem.listStatus(path);
        for (FileStatus fileStatus : stats) {
            if (fileStatus.isFile()) {
                System.out.println(fileStatus.getPath().toString());
            } else {
                System.out.println("dir " + fileStatus.getPath().toString());
            }
        }
    }


    public static boolean checkHdfsFileType(String filePath) {

        Path file = new Path(filePath);

        try {
            Configuration hadoopConf = new Configuration();
            FileSystem fs = FileSystem.get(hadoopConf);
            FSDataInputStream in = fs.open(file);

            boolean isParquet = isParquetFile(filePath, in);
            return isParquet;

        } catch (Exception e) {
            String message = String.format("检查文件[%s]类型失败，目前支持ORC,SEQUENCE,RCFile,TEXT,CSV五种格式的文件," +
                    "请检查您文件类型和文件是否正确。", filePath);
            System.out.println(message);
        }
        return false;
    }

    // 判断file是否为Parquet file
    public static boolean isParquetFile(String fileName, FSDataInputStream in) {
        byte[] SEQ_MAGIC = new byte[]{(byte) 'P', (byte) 'A', (byte) 'R', (byte) '1'};
        byte[] magic = new byte[SEQ_MAGIC.length];
        try {
            in.seek(0);
            in.readFully(magic);
            if (Arrays.equals(magic, SEQ_MAGIC)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(String.format("检查文件类型: [%s] 不是Parquet File.", fileName));
        }
        return false;
    }

    // 判断file是否为Sequence file
    public static boolean isSequenceFile(String fileName, FSDataInputStream in) {
        byte[] SEQ_MAGIC = new byte[]{(byte) 'S', (byte) 'E', (byte) 'Q'};
        byte[] magic = new byte[SEQ_MAGIC.length];
        try {
            in.seek(0);
            in.readFully(magic);
            if (Arrays.equals(magic, SEQ_MAGIC)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(String.format("检查文件类型: [%s] 不是Sequence File.", fileName));
        }
        return false;
    }

    public static boolean isLzoFile(String fileName, FSDataInputStream in) {
        if (fileName.endsWith(".lzo")) {
            return true;
        }
        return false;
    }

}
