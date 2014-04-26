package wordcount;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;

public class WordCountDriver {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {

		if (args.length != 2) {
			System.err.println("Usage: " + WordCountDriver.class.getName()
					+ " <input path> <output path>");
			System.exit(-1);
		}

		Job job = new Job();
		job.setJarByClass(WordCountDriver.class);
		job.setJobName("Word Count");

		String inputPath = args[0];
		String outputPath = args[1];

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));

		job.setMapperClass(LineToWordCountTupleMapper.class);
		job.setCombinerClass(WordCountTupleReducer.class);
		job.setReducerClass(WordCountTupleReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
