package fpm

import scala.io.Source
import java.io.FileWriter
import java.io.FileOutputStream
import java.io.File
import java.io.PrintStream

object Utils {

  def main(args: Array[String]) {
    if (args.length != 1) {
      println("Usage: " + Utils.getClass().getName() + "  [path/to/marketbasket.csv]")
      System.exit(0)
    }

    val lines = Source.fromFile(args(0)).getLines()
    val headerLine = lines.take(1).next

    val headers = headerLine.split(",").map(_.trim).zipWithIndex

    // mapping of columns and their relative position in header starting with 0
    println("writing mapping.csv")
    val ps = new PrintStream(new FileOutputStream(new File("mapping.csv")))
    for (h <- headers) {
      ps.print("%s,%s\n".format(h._1, h._2))
    }
    ps.close()

    // only the column ids of those items which were purchased for every transaction
    println("writing output.dat")
    val datPs = new PrintStream(new FileOutputStream(new File("output.dat")))
    for (line <- lines.filter(l => l != null && !l.isEmpty())) {
      val tokens = line.split(",").map(_.trim).zipWithIndex
      val (first, remaining) = tokens.splitAt(1)
      val entry = remaining.filter(_._1 == "true").map { r => r._2 }.toList.mkString(",")
      datPs.println(entry)
    }
    datPs.close()
  }
  
}
