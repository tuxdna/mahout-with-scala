package fpm

import scala.io.Source
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
import org.apache.hadoop.io.LongWritable
import org.apache.mahout.fpm.pfpgrowth.convertors.string.TopKStringPatterns
import scala.collection.JavaConversions._

object readfpmoutput {
  def main(args: Array[String]): Unit = {
    val mappingCsvFilename = "mapping.csv"

    // fetch CLI arguments
    val totalRecords = args(0).toInt
    val frequencyFilename = args(1)
    val frequentPatternsFilename = args(2)
    val minSupport = args(3).toDouble
    val minConfidence = args(4).toDouble

    // read items and their id mappings
    val itemIdMap = Source.fromFile(mappingCsvFilename)
      .getLines.map { l =>
        val parts = l.split(",").map(_.trim)
        (parts(1).toInt, parts(0))
      }.toMap

    // read item frequencies
    val conf = new Configuration
    val fs = FileSystem.get(conf)

    val freqFilePath = new Path(frequencyFilename)
    val freqReader = new SequenceFile.Reader(FileSystem.get(conf),
      freqFilePath, conf)
    val key = new Text
    val value = new LongWritable
    var freqMap = Map[Int, Long]()
    while (freqReader.next(key, value)) {
      val k = key.toString().toInt
      val v = value.get
      freqMap += (k -> v)
    }
    freqReader.close()

    // read frequent patterns now
    val topkValue = new TopKStringPatterns()
    val freqPatternsReader = new SequenceFile.Reader(fs, new Path(frequencyFilename),
      conf)

    while (freqReader.next(key, topkValue)) {
      val itemId = key.toString().toInt
      val patterns = topkValue.getPatterns()

      // first entry for the current item
      val firstEntry = patterns.head
      val itemFrequency = firstEntry.getSecond()

      // remaining entries are occurrences of this item with other items 
      for (pairs <- patterns.drop(1)) {
        val items = pairs.getFirst()
        val occurrence = pairs.getSecond()
        val support = occurrence / totalRecords.toDouble
        val confidence = occurrence / itemFrequency.toDouble
        if (support > minSupport && confidence > minConfidence) {
          val listWithoutThisItem = items.filter(_ != itemId)
          val thisItemName = itemIdMap(itemId)
          println(thisItemName, listWithoutThisItem, support, confidence)
          // compute lift and conviction of size 2 items lists, of which one is this item 
          if (listWithoutThisItem.size() == 1) {
            val oid = listWithoutThisItem.first.toInt
            val otherItemOccurrence = freqMap(oid)
            val lift = (occurrence.toDouble * totalRecords) / (itemFrequency * otherItemOccurrence)
            val conviction = (1.0 - otherItemOccurrence.toDouble / totalRecords) / (1.0 - confidence)
            println(", lift=%.3f, conviction=%.3f".format(lift, conviction))
          }
        }
      }
    }
    freqPatternsReader.close()
  }
}