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
import org.apache.hadoop.util.Tool;
import org.apache.log4j.Logger;
import org.apache.parquet.hadoop.ParquetOutputFormat;

/**
 * Created by sunzhiwei on 2018/10/16.
 */
public class ParquetMerge extends Configured implements Tool {

    private static final long ONE_MB = 1024 * 1024L;
    private static Logger logger = Logger.getLogger(ParquetMerge.class);

    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration(getConf());
//        conf.set("mapreduce.map.output.compress", "false");
//        conf.set("mapreduce.output.fileoutputformat.compress", "false");

        String inputDir = args[0];
        String outputDir = args[1];
        int maxSplitSize = Integer.valueOf(args[2]);
        logger.info("inputDir--->>" + inputDir);
        logger.info("outputDir--->>" + outputDir);
        logger.info("maxSplitSize--->>" + maxSplitSize + "M");

        Job job = Job.getInstance(conf);
        FileInputFormat.setInputPaths(job, inputDir);
        FileOutputFormat.setOutputPath(job, new Path(outputDir));
//        FileOutputFormat.setCompressOutput(job, true);
//        FileOutputFormat.setOutputCompressorClass(job, LzoCodec.class);
        job.setJarByClass(TextMerge.class);

        // 设置最大输入分片大小，与运行的map、及生产的文件数密切相关
        CombineTextInputFormat.setMaxInputSplitSize(job, ONE_MB * maxSplitSize);
        job.setInputFormatClass(CombineParquetInputFormat.class);
        job.setOutputFormatClass(ParquetOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setMapperClass(ParquetMerge.ParquetMergeMapper.class);
        job.setNumReduceTasks(0);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    static class ParquetMergeMapper extends
            Mapper<LongWritable, Text, Text, NullWritable> {
        //
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            context.write(value, NullWritable.get());
        }
    }
}
