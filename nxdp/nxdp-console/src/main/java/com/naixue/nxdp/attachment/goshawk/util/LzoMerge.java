package com.naixue.nxdp.attachment.goshawk.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.CombineTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by sunzhiwei on 2018/10/11.
 */
@Slf4j
public class LzoMerge extends Configured implements Tool {

    // private static Logger logger = Logger.getLogger(LzoMerge.class);
    private static final long ONE_MB = 1024 * 1024L;

    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration(getConf());
        conf.set("fs.default.name", args[3]);
//        conf.set("mapreduce.map.output.compress", "false");
//        conf.set("mapreduce.output.fileoutputformat.compress", "false");

        String inputDir = args[0];
        String outputDir = args[1];
        int maxSplitSize = Integer.valueOf(args[2]);
        log.info("inputDir--->>" + inputDir);
        log.info("outputDir--->>" + outputDir);
        log.info("maxSplitSize--->>" + maxSplitSize + "M");
        System.out.println("inputDir--->>" + inputDir);
        System.out.println("outputDir--->>" + outputDir);
        System.out.println("maxSplitSize--->>" + maxSplitSize + "M");
        Job job = Job.getInstance(conf);
        FileInputFormat.setInputPaths(job, inputDir);
        FileOutputFormat.setOutputPath(job, new Path(outputDir));
//        FileOutputFormat.setCompressOutput(job, true);
//        FileOutputFormat.setOutputCompressorClass(job, LzoCodec.class);
        job.setJarByClass(TextMerge.class);

        // 设置最大输入分片大小，与运行的map、及生产的文件数密切相关
        CombineTextInputFormat.setMaxInputSplitSize(job, ONE_MB * maxSplitSize);
        job.setInputFormatClass(CombineLzoInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setMapperClass(LzoMerge.LzoMergeMapper.class);
        job.setNumReduceTasks(0);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    static class LzoMergeMapper extends
            Mapper<LongWritable, Text, Text, NullWritable> {
        //
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            context.write(value, NullWritable.get());
        }
    }
}
