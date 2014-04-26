import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;

/**
 * Code for converting CSV format to Mahout's SVD format
 * 
 * @author Danny Bickson, CMU Note: I ASSUME THE CSV FILE IS SORTED BY THE
 *         COLUMN (NAMELY THE SECOND FIELD).
 * 
 */

// java -cp $DEPENDENCIES_CLASSPATH Convert2SVD ../../netflixe.csv 17770 netflixe.seq

public class Convert2SVD {

	public static int Cardinality;

	/**
	 * 
	 * @param args
	 *            [0] - input csv file
	 * @param args
	 *            [1] - cardinality (length of vector)
	 * @param args
	 *            [2] - output file for svd
	 */
	public static void main(String[] args) {

		try {
			Cardinality = Integer.parseInt(args[1]);
			final Configuration conf = new Configuration();
			final FileSystem fs = FileSystem.get(conf);
			final SequenceFile.Writer writer = SequenceFile.createWriter(fs,
					conf, new Path(args[2]), IntWritable.class,
					VectorWritable.class, CompressionType.BLOCK);

			final IntWritable key = new IntWritable();
			final VectorWritable value = new VectorWritable();

			String thisLine;

			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			Vector vector = null;
			int from = -1, to = -1;
			int last_to = -1;
			float val = 0;
			int total = 0;
			int nnz = 0;
			int e = 0;
			int max_to = 0;
			int max_from = 0;

			while ((thisLine = br.readLine()) != null) { // while loop begins
															// here

				StringTokenizer st = new StringTokenizer(thisLine, ",");
				while (st.hasMoreTokens()) {
					from = Integer.parseInt(st.nextToken()) - 1; // convert from
																	// 1 based
																	// to zero
																	// based
					to = Integer.parseInt(st.nextToken()) - 1; // convert from 1
																// based to zero
																// basd
					val = Float.parseFloat(st.nextToken());
					if (max_from < from)
						max_from = from;
					if (max_to < to)
						max_to = to;
					if (from < 0 || to < 0 || from > Cardinality || val == 0.0)
						throw new NumberFormatException("wrong data" + from
								+ " to: " + to + " val: " + val);
				}

				// we are working on an existing column, set non-zero rows in it
				if (last_to != to && last_to != -1) {
					value.set(vector);

					writer.append(key, value); // write the older vector
					e += vector.getNumNondefaultElements();
				}
				// a new column is observed, open a new vector for it
				if (last_to != to) {
					vector = new SequentialAccessSparseVector(Cardinality);
					key.set(to); // open a new vector
					total++;
				}

				vector.set(from, val);
				nnz++;

				if (nnz % 1000000 == 0) {
					System.out.println("Col" + total + " nnz: " + nnz);
				}
				last_to = to;

			} // end while

			value.set(vector);
			writer.append(key, value);// write last row
			e += vector.getNumNondefaultElements();
			total++;

			writer.close();
			System.out.println("Wrote a total of " + total + " cols "
					+ " nnz: " + nnz);
			if (e != nnz)
				System.err.println("Bug:missing edges! we only got" + e);

			System.out.println("Highest column: " + max_to + " highest row: "
					+ max_from);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}