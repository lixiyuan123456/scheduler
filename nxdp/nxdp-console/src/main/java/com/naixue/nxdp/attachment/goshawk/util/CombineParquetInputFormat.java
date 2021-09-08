package com.naixue.nxdp.attachment.goshawk.util;

import java.io.IOException;

/**
 * Created by sunzhiwei on 2018/10/11.
 */

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReaderWrapper;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.parquet.hadoop.ParquetInputFormat;

/**
 * Input format that is a <code>CombineFileInputFormat</code>-equivalent for
 * <code>TextInputFormat</code>.
 *
 * @see CombineFileInputFormat
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class CombineParquetInputFormat
        extends CombineFileInputFormat<LongWritable, Text> {
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit split,
                                                               TaskAttemptContext context) throws IOException {
        return new CombineFileRecordReader<LongWritable, Text>(
                (CombineFileSplit) split, context, ParquetRecordReaderWrapper.class);
    }

    /**
     * A record reader that may be passed to <code>CombineFileRecordReader</code>
     * so that it can be used in a <code>CombineFileInputFormat</code>-equivalent
     * for <code>TextInputFormat</code>.
     *
     * @see CombineFileRecordReader
     * @see CombineFileInputFormat
     * @see TextInputFormat
     */
    private static class ParquetRecordReaderWrapper
            extends CombineFileRecordReaderWrapper<LongWritable, Text> {
        // this constructor signature is required by CombineFileRecordReader
        public ParquetRecordReaderWrapper(CombineFileSplit split,
                                          TaskAttemptContext context, Integer idx)
                throws IOException, InterruptedException {
            super(new ParquetInputFormat(), split, context, idx);
        }
    }
}