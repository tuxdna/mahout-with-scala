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

object WriteSequenceFile {
  val DATA = Array(
    "One, two, buckle my shoe",
    "Three, four, shut the door",
    "Five, six, pick up sticks",
    "Seven, eight, lay them straight",
    "Nine, ten, a big fat hen")

  def writeFile(uri: String) = {
    val conf = new Configuration
    val fs = FileSystem.get(URI.create(uri), conf)
    val path = new Path(uri)
    val key = new IntWritable
    val value = new Text
    val UP = 100
    var writer: SequenceFile.Writer = null
    try {
      writer = SequenceFile.createWriter(fs, conf, path, key.getClass, value.getClass)
      for (i <- 0 until UP) {
        key.set(UP - i)
        value.set(DATA(i % DATA.length))
        println("[%s]\t%s\t%s".format(writer.getLength(), key, value))
        writer.append(key, value)
      }
    } finally {
      IOUtils.closeStream(writer)
    }
  }

  def main(args: Array[String]): Unit = {
    if (args.length >= 1) {
      val uri = args(0)
      WriteSequenceFile.writeFile(uri)
    } else {
      println("A sequence file path is required")
    }

  }
}