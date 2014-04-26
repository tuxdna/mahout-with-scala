package utils

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import java.net.URI
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.io.SequenceFile
import org.apache.hadoop.io.IOUtils
import org.apache.hadoop.io.WritableComparable
import org.apache.hadoop.io.Writable

object ReadSequenceFile {
  def readFile(uri: String) = {
    val conf = new Configuration
    val fs = FileSystem.get(URI.create(uri), conf)
    val path = new Path(uri)
    val reader = new SequenceFile.Reader(FileSystem.get(conf), path, conf)

    val keyClass = reader.getKeyClass
    val keyObj = keyClass.newInstance
    val key: WritableComparable[_] = keyObj.asInstanceOf[WritableComparable[_]]

    println(keyClass.getName())
    
    val valueClass = reader.getValueClass
    val valueObj = valueClass.newInstance
    val value: Writable = valueObj.asInstanceOf[Writable]

    println(valueClass.getName())
    
    val position = reader.getPosition()

    while (reader.next(key, value)) {
      println("Key is: " + key + "\nValue is: " + value + "\n")
    }
    IOUtils.closeStream(reader)
  }

  def main(args: Array[String]): Unit = {
    if (args.length >= 1) {
      val uri = args(0)
      ReadSequenceFile.readFile(uri)
    } else {
      println("A sequence file path is required")
    }
  }
}
