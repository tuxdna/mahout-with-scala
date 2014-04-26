import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

// https://stackoverflow.com/questions/7062327/generating-a-sequencefile
public class SequenceFileDemo {

	private static final String[] DATA = { "One, two, buckle my shoe",
			"Three, four, shut the door", "Five, six, pick up sticks",
			"Seven, eight, lay them straight", "Nine, ten, a big fat hen" };

	public static void writeFile(String uri) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(URI.create(uri), conf);
		Path path = new Path(uri);
		IntWritable key = new IntWritable();
		Text value = new Text();
		SequenceFile.Writer writer = null;
		try {
			writer = SequenceFile.createWriter(fs, conf, path, key.getClass(),
					value.getClass());
			for (int i = 0; i < 100; i++) {
				key.set(100 - i);
				value.set(DATA[i % DATA.length]);
				System.out.printf("[%s]\t%s\t%s\n", writer.getLength(), key,
						value);
				writer.append(key, value);
			}
		} finally {
			IOUtils.closeStream(writer);
		}
	}

	public static void readFile(String uri) throws IOException, InstantiationException, IllegalAccessException {
		Configuration config = new Configuration();
		Path path = new Path(uri);
		SequenceFile.Reader reader = new SequenceFile.Reader(
				FileSystem.get(config), path, config);
		WritableComparable key = (WritableComparable) reader.getKeyClass()
				.newInstance();
		Writable value = (Writable) reader.getValueClass().newInstance();
		long position = reader.getPosition();

		while (reader.next(key, value)) {
			System.out.println("Key is: " + key + " value is: " + value + "\n");
		}
		IOUtils.closeStream(reader);
	}

	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
		String uri = args[0];
		writeFile(uri);
		readFile(uri);
	}
}
