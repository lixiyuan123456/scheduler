package com.naixue.nxdp.attachment.goshawk.util;

import java.util.Arrays;

import org.apache.hadoop.util.ToolRunner;

public class MergeLittleDir {

    public static Integer mergeLittleDir(String dirType, String input, String output, String size, String fs_default_name, String jarPath, String hadoop_queuename) throws Exception {

        String[] args = new String[6];
        //args[0] = dirType;

        args[0] = dirType;
        args[1] = hadoop_queuename;
        args[2] = input;
        args[3] = output;
        args[4] = size;
        args[5] = jarPath;
        System.out.println("dirType" + "：" + dirType);
        System.out.println("hadoop_queuename" + "：" + hadoop_queuename);
        System.out.println("input" + "：" + input);
        System.out.println("output" + "：" + output);
        System.out.println("size" + "：" + size);
        System.out.println("jarPath" + "：" + jarPath);
        //Main.main(args);
		
        /*if (args.length < 4) {
            System.out.println("args is type(TEXT、LZO、PARQUET), input(not include last /), output, size(M)");
            System.out.println("hadoop jar merge-little-file-1.0-SNAPSHOT.jar " +
                    "-Dmapreduce.job.queuename=root.offline.dp TEXT /tmp/szw_lzo /tmp/szw_lzo_out 100");
            System.exit(255);
        }*/
        /*String type = dirType;
        if (type.length() == 0 || type == null){
            System.out.println("type is error, please check, you may choose TEXT, LZO, PARQUET");
        }
        String[] arg = Arrays.copyOfRange(args, 1, 3);
//        System.arraycopy
        int exitCode = 255;
        switch (type.toUpperCase()){
            case "TEXT":
                exitCode = ToolRunner.run(new TextMerge(), args);
                break;
            case "LZO":
                exitCode = ToolRunner.run(new LzoMerge(), args);
                break;
            case "PARQUET":
//                Parquet(arg[0], arg[1], 3);
                break;
            default:
                break;
        }*/

//        判断目录中是否全为文件，全为文件则合并，否则退出
        return 0;
    }
}
