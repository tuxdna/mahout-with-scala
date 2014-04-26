package clustering

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.mahout.math.Vector
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.SequenceFile
import org.apache.hadoop.io.LongWritable
import org.apache.mahout.math.VectorWritable

object ClusterHelper {
  def writePointsToFile(points: List[Vector],
    conf: Configuration,
    path: Path) = {
    val fs = FileSystem.get(path.toUri(), conf);
    val writer = new SequenceFile.Writer(fs, conf, path,
      classOf[LongWritable], classOf[VectorWritable])
    var recNum = 0L
    val vec = new VectorWritable()
    for (point <- points) {
      vec.set(point);
      writer.append(new LongWritable(recNum), vec)
      recNum += 1
    }
    writer.close()
  }
}