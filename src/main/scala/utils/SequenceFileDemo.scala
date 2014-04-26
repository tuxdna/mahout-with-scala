package utils



object SequenceFileDemo {
 


  def main(args: Array[String]): Unit = {
    if (args.length >= 1) {
      val uri = args(0)
      WriteSequenceFile.writeFile(uri)
      ReadSequenceFile.readFile(uri)
    } else {
      println("A sequence file path is required")
    }
  }
}
