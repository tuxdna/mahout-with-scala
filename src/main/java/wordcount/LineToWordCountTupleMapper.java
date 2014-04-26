package wordcount;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class LineToWordCountTupleMapper extends
		Mapper<LongWritable, Text, Text, IntWritable> {

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		// map every line into a tuple of (word, 1), where word is a term in
		// line separated by whitespace

		String line = value.toString();
		String[] words = line.split("\\s+");
		for (String word : words) {
			String term = word.trim();
			if (term.length() > 0)
				context.write(new Text(term.toLowerCase()), new IntWritable(1));
		}
	}
}
