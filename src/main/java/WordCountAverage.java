import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCountAverage {
	public static class IntPair implements WritableComparable<IntPair> {
		private int first = 0;
		private int second = 0;

		/**
		 * Set the left and right values.
		 */
		public void set(int left, int right) {
			first = left;
			second = right;
		}

		public int getFirst() {
			return first;
		}

		public int getSecond() {
			return second;
		}

		/**
		 * Read the two integers. Encoded as: MIN_VALUE -> 0, 0 -> -MIN_VALUE,
		 * MAX_VALUE-> -1
		 */
		@Override
		public void readFields(DataInput in) throws IOException {
			first = in.readInt() + Integer.MIN_VALUE;
			second = in.readInt() + Integer.MIN_VALUE;
		}

		@Override
		public void write(DataOutput out) throws IOException {
			out.writeInt(first - Integer.MIN_VALUE);
			out.writeInt(second - Integer.MIN_VALUE);
		}

		@Override
		public int hashCode() {
			return first * 157 + second;
		}

		@Override
		public boolean equals(Object right) {
			if (right instanceof IntPair) {
				IntPair r = (IntPair) right;
				return r.first == first && r.second == second;
			} else {
				return false;
			}
		}

		/** A Comparator that compares serialized IntPair. */
		public static class Comparator extends WritableComparator {
			public Comparator() {
				super(IntPair.class);
			}

			public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2,
					int l2) {
				return compareBytes(b1, s1, l1, b2, s2, l2);
			}
		}

		static { // register this comparator
			WritableComparator.define(IntPair.class, new Comparator());
		}

		@Override
		public int compareTo(IntPair o) {
			if (first != o.first) {
				return first < o.first ? -1 : 1;
			} else if (second != o.second) {
				return second < o.second ? -1 : 1;
			} else {
				return 0;
			}
		}
	}

	// maps word to (firstAlphabet -> (lengthOfWordsSoFar, numberOfWords))
	public static class TokenizerMapper extends
			Mapper<Object, Text, Text, IntPair> {

		private Text firstCharacter = new Text();
		private final static IntPair sumAndCount = new IntPair();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {
				String token = itr.nextToken();
				firstCharacter.set(token.substring(0, 1));
				// partial: <sum, numElements>
				sumAndCount.set(token.length(), 1);
				context.write(firstCharacter, sumAndCount);
			}
		}
	}

	// combiner class
	public static class IntPairPartialSumCombiner extends
			Reducer<Text, IntPair, Text, IntPair> {
		private IntPair result = new IntPair();

		public void reduce(Text key, Iterable<IntPair> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			int total = 0;
			for (IntPair val : values) {
				sum += val.getFirst();
				total += val.getSecond();
			}
			result.set(sum, total);
			context.write(key, result);
		}
	}

	// reducer class
	public static class IntPairAverageReducer extends
			Reducer<Text, IntPair, Text, DoubleWritable> {
		private DoubleWritable result = new DoubleWritable();

		public void reduce(Text key, Iterable<IntPair> values, Context context)
				throws IOException, InterruptedException {
			double sum = 0;
			int total = 0;
			for (IntPair val : values) {
				sum += val.getFirst();
				total += val.getSecond();
			}
			double average = sum / total;
			result.set(average);
			context.write(key, result);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "word count");
		job.setJarByClass(WordCountAverage.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntPairPartialSumCombiner.class);
		job.setReducerClass(IntPairAverageReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntPair.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}
