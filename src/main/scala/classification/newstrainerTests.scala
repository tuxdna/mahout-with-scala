package classification

import java.io.StringReader
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder
import com.google.common.collect.Sets
import org.apache.mahout.math.RandomAccessSparseVector
import org.apache.mahout.math.{ Vector => MahoutVector }
import java.io.File
import org.apache.mahout.vectorizer.encoders.Dictionary

object newstrainerTests {
  def countWordsTest() {
    val line = """
      ABOUT THIS POSTING
------------------
This is a (still rather rough) listing of likely questions and
information about RIPEM, a program for public key mail encryption.  It
(this FAQ, not RIPEM) was written and will be maintained by Email
Man, <emial@someplace.edu>.  It will be posted to a
variety of newsgroups on a monthly basis; follow-up discussion specific
to RIPEM is redirected to the group alt.security.ripem.

This month, I have reformatted this posting in an attempt to comply
with the standards for HyperText FAQ formatting to allow easy
manipulation of this document over the World Wide Web.  Let me know
what you think.
      
      """

    val in = new StringReader(line)
    val analyzer = new StandardAnalyzer(Version.LUCENE_46)
    val words = newstrainer.countWords(analyzer, in)
    println(words)

  }

  def biasTest() {
    val traceDictionary: java.util.Map[String, java.util.Set[Integer]] = new java.util.TreeMap[String, java.util.Set[Integer]]()
    val bias = new ConstantValueEncoder("Intercept")
    bias.setTraceDictionary(traceDictionary)

    println(traceDictionary)
    val set: java.util.Set[Integer] = Sets.newHashSet(0)
    traceDictionary.put("", set)
    val FEATURES = 10000

    val v: MahoutVector = new RandomAccessSparseVector(FEATURES)
    println("Add")
    bias.addToVector("", 1, v)
  }

  def parseMailTest() {
    val fname1 = "/home/saleem/work/learn/external/ml-data/20news/20news-bydate-train/rec.sport.baseball/104596"
    val fname2 = "/home/saleem/work/learn/external/ml-data/20news/20news-bydate-train/comp.sys.mac.hardware/51916"
    val fname3 = "/home/saleem/work/learn/external/ml-data/20news/20news-bydate-train/comp.os.ms-windows.misc/9159"

    val file = new File(fname3)
    println(file)
    val (h, l, b) = newstrainer.parseMail(file)

    h foreach println
    println(" ==>" + l)
    b foreach println
  }

  def main(args: Array[String]) {
    countWordsTest()
    biasTest()
    parseMailTest()
  }

}