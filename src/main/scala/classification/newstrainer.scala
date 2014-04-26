package classification

import java.io.File
import com.google.common.collect.HashMultiset
import scala.collection.immutable.TreeMap
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder
import org.apache.mahout.vectorizer.encoders.Dictionary
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression
import com.google.common.collect.Multiset
import org.apache.mahout.classifier.sgd.L1
import java.util.Collections
import scala.util.Random
import scala.collection.JavaConversions._
import scala.io.Source
import com.google.common.collect.Iterables
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import java.io.StringReader
import java.io.Reader
import java.util.Collection
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import com.google.common.collect.ConcurrentHashMultiset
import java.io.BufferedReader
import java.io.FileReader
import org.apache.mahout.math.RandomAccessSparseVector
import scala.collection.mutable.ArrayBuffer
import org.apache.mahout.math.{ Vector => MahoutVector }
import org.apache.mahout.math.DenseVector
import com.google.common.collect.Sets
import scala.io.Codec
import java.nio.charset.CodingErrorAction
import org.apache.mahout.classifier.sgd.AdaptiveLogisticRegression
import org.apache.mahout.ep.State
import org.apache.mahout.classifier.sgd.CrossFoldLearner
import org.apache.mahout.math.Matrix
import org.apache.mahout.math.function.Functions
import org.apache.mahout.math.function.DoubleFunction
import org.apache.mahout.classifier.sgd.ModelSerializer
import com.google.common.collect.Maps
import org.apache.mahout.classifier.sgd.ModelDissector
import java.util.{ Map => JavaMap, Set => JavaSet }

object newstrainer {
  val LEAK_LABELS = Array("none", "month-year", "day-month-year")
  val analyzer = new StandardAnalyzer(Version.LUCENE_46)

  def encodeFeatureVector(
    file: File,
    encoder: ConstantValueEncoder,
    bias: StaticWordValueEncoder) = {
    val (header, count, body) = parseMail(file)
    val words: Multiset[String] = ConcurrentHashMultiset.create()
    // process header
    val headerRegex = """(From|Subject|Keywords|Summary):.*""".r
    header.foreach { l =>
      l match {
        case headerRegex(x) =>
          val w = countWords(analyzer, l)
          words.addAll(w)
        case _ =>
      }
    }

    // process body
    val w = countWords(analyzer, body.mkString("\n"))
    words.addAll(w)

    overallCounts.addAll(words)

    val vec = new RandomAccessSparseVector(FEATURES)
    bias.addToVector("", 1, vec)

    for (word <- words.elementSet()) {
      val wordCount = words.count(word)
      val score = java.lang.Math.log1p(wordCount)
      encoder.addToVector(word, score, vec)
    }
    vec
  }

  final class SGDInfo(
    var averageLL: Double,
    var averageCorrect: Double,
    var step: Double,
    var bumps: Array[Int] = Array(1, 2, 5))

  def dissect(leakType: Int,
    dictionary: Dictionary,
    learningAlgorithm: AdaptiveLogisticRegression,
    files: List[File],
    overallCounts: Multiset[String],
    encoder: StaticWordValueEncoder,
    bias: ConstantValueEncoder) = {
    val model = learningAlgorithm.getBest().getPayload().getLearner()
    model.close()

    val traceDictionary: JavaMap[String, JavaSet[Integer]] = new java.util.TreeMap[String, JavaSet[Integer]]()

    val md = new ModelDissector()
    encoder.setTraceDictionary(traceDictionary)
    bias.setTraceDictionary(traceDictionary)

    for (file <- Random.shuffle(files).take(500)) {
      val ng = file.getParentFile().getName()
      val actual = dictionary.intern(ng)

      traceDictionary.clear()

      val v = encodeFeatureVector(file, bias, encoder)
      learningAlgorithm.train(actual, v)

      md.update(v, traceDictionary, model);
    }

    val ngNames = dictionary.values
    val weights = md.summary(100)
    System.out.println("============");
    System.out.println("Model Dissection");
    for (w <- weights) {
      printf("%s\t%.1f\t%s\t%.1f\t%s\t%.1f\t%s%n",
        w.getFeature(), w.getWeight(), ngNames.get(w.getMaxImpact() + 1),
        w.getCategory(1), w.getWeight(1), w.getCategory(2), w.getWeight(2))
    }
  }

  def analyzeState(info: SGDInfo, leakType: Int,
    index: Int,
    best: State[AdaptiveLogisticRegression.Wrapper, CrossFoldLearner]) {
    val bump = info.bumps((Math.floor(info.step) % info.bumps.length).toInt)

    val scale = Math.pow(10, Math.floor(info.step / info.bumps.length)).toInt

    val (maxBeta, nonZeros, positive, norm, lambda, mu) = Option(best) match {
      case Some(best) =>
        val state = best.getPayload().getLearner();
        info.averageCorrect = state.percentCorrect()
        info.averageLL = state.logLikelihood()

        val model: OnlineLogisticRegression = state.getModels().get(0)
        // finish off pending regularization
        model.close()

        val beta = model.getBeta()
        (
          // maxBeta
          beta.aggregate(Functions.MAX, Functions.ABS),
          // nonZeros 
          beta.aggregate(Functions.PLUS, new DoubleFunction() {
            override def apply(v: Double): Double = if (Math.abs(v) > 1.0e-6) 1 else 0
          }),
          // positive 
          beta.aggregate(Functions.PLUS, new DoubleFunction() {
            override def apply(v: Double): Double = if (v > 0) 1 else 0
          }),
          // norm
          beta.aggregate(Functions.PLUS, Functions.ABS),
          // lambda
          best.getMappedParams()(0),
          // mu 
          best.getMappedParams()(1))
      case None => { (0.0, 0.0, 0.0, 0.0, 0.0, 0.0) }
    }

    if (index % (bump * scale) == 0) {
      if (best != null) {
        val modelName = "news-group-%d.model".format(index)
        val path = new File("/tmp", modelName)
        val model = best.getPayload().getLearner().getModels().get(0)
        ModelSerializer.writeBinary(path.getAbsolutePath(), model)
      }

      info.step = info.step + 0.25
      printf("%.2f\t%.2f\t%.2f\t%.2f\t%.8g\t%.8g\t", maxBeta, nonZeros, positive, norm, lambda, mu)
      printf("%d\t%.3f\t%.2f\t%s%n",
        index, info.averageLL, info.averageCorrect * 100, LEAK_LABELS(leakType % 3))
    }
  }

  val FEATURES = 10000
  val overallCounts: Multiset[String] = HashMultiset.create()
  val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.REPLACE)
  codec.onUnmappableCharacter(CodingErrorAction.REPLACE)

  def countWords(analyzer: Analyzer, in: Reader): ArrayBuffer[String] = {
    val fieldName = "text"
    val ts = analyzer.tokenStream(fieldName, in)
    ts.reset()
    var words = ArrayBuffer[String]()
    while (ts.incrementToken()) {
      val s = ts.getAttribute(classOf[CharTermAttribute]).toString()
      words += (s)
    }
    ts.close()
    words
  }

  def countWords(analyzer: Analyzer, s: String): ArrayBuffer[String] = {
    countWords(analyzer, new StringReader(s))
  }

  def parseMail(file: File) = {
    val (header, body) = Source.fromFile(file)(codec)
      .getLines.toList
      .span(x => !x.trim().equals(""))

    val h = header.filter(!_.startsWith("Lines:"))
    val ln = header.filter(_.startsWith("Lines:"))

    // identify linecount
    val lcRegex = """Lines:\s*(\d+).*""".r

    val c = ln match {
      case x :: xs => x match { case lcRegex(count) => count.toInt case _ => 0 }
      case _ => 0
    }

    (h, c, body)
  }

  def main(args: Array[String]): Unit = {
    val defaultInput = "/home/tuxdna/work/learn/external/ml-data/20news-bydate-train"
    val base = new File(if (args.length > 0) args(0) else defaultInput)
    val leakType = if (args.length > 1) args(1).toInt else 0
    val newsGroups = new Dictionary()

    val fileList = base.listFiles().flatMap { newsgroup =>
      newsGroups.intern(newsgroup.getName())
      newsgroup.listFiles()
    } toList

    val files = Random.shuffle(fileList)
    println("%d training files\n".format(files.size()))

    // feature encoder
    val bias = new ConstantValueEncoder("Intercept")
    val encoder = new StaticWordValueEncoder("body")
    encoder.setProbes(2)
    val learningAlgorithm = new AdaptiveLogisticRegression(20, FEATURES, new L1())
    learningAlgorithm.setInterval(800)
    learningAlgorithm.setAveragingWindow(500)

    val sgdInfo = new SGDInfo(0.0, 0.0, 0.0)
    for ((file, index) <- files.zipWithIndex) {
      val v = encodeFeatureVector(file, bias, encoder)
      val newsGroupName = file.getParentFile().getName()
      val newsgroupId = newsGroups.intern(newsGroupName)
      learningAlgorithm.train(newsgroupId, v)
      val best = learningAlgorithm.getBest()
      analyzeState(sgdInfo, leakType, index, best)
    }

    learningAlgorithm.close()

    dissect(leakType, newsGroups, learningAlgorithm, files, overallCounts, encoder, bias)
    println("exiting main")
    ModelSerializer.writeBinary("/tmp/news-group.model",
      learningAlgorithm.getBest().getPayload().getLearner().getModels().get(0))

    println("Word counts")
    val counts = overallCounts.elementSet().toList
      .map { word => overallCounts.count(word) }
      .sorted.reverse

    counts.take(1000).zipWithIndex
      .foreach(x => println(x._2 + "\t" + x._1))
  }
}