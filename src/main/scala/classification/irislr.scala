package classification

import scala.io.Source
import scala.util.Random
import org.apache.mahout.math.DenseVector
import org.apache.mahout.math.{ Vector => MahoutVector }
import scala.collection.mutable.ArrayBuffer
import org.apache.mahout.vectorizer.encoders.Dictionary
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression
import org.apache.mahout.classifier.sgd.L2
import java.io.File
import java.io.PrintStream

object irislr {

  def main(args: Array[String]): Unit = {
    val irisData = if (args.length > 0) args(0)
    else "/home/saleem/work/learn/mine/mahout-0.9-in-scala/iris.csv"

    val dict = new Dictionary()
    var features = ArrayBuffer[MahoutVector]()
    var target = ArrayBuffer[Int]()
    // Read data from file
    // Sepal.Length,Sepal.Width,Petal.Length,Petal.Width,Species
    Source.fromFile(irisData).getLines().drop(1) // skip header
      .filter(_ != null).map(_.split(",")).filter(_.length == 5)
      .foreach { elems =>
        val v: MahoutVector = new DenseVector(5)
        v.set(0, 1) // constant term
        v.set(1, elems(0).toDouble) //Sepal.Length 
        v.set(2, elems(1).toDouble) //Sepal.Width
        v.set(3, elems(2).toDouble) //Petal.Length
        v.set(4, elems(3).toDouble) //Petal.Width
        val categoryCode = dict.intern(elems(4))
        target += categoryCode
        features += v
      }

    // data.zip(target) foreach println
    val dataIndices = (0 until features.length).toList
    val order = Random.shuffle(dataIndices)
    val (train, rest) = order.splitAt(100)
    val test = rest.take(50)

    // for 0 until test.size, we store number of mismatches with test data 
    val correct = Array.ofDim[Int](test.size + 1)

    val numClasses = 3
    val numFeatures = 5
    val totalRuns = 200
    val trainingPasses = 3

    for (run <- 0 until totalRuns) {
      //      println("Run " + run)

      // 30 training passes should converge to > 95% accuracy 
      // nearly always but never to 100%
      val lr = new OnlineLogisticRegression(numClasses, numFeatures, new L2(1))
      for (pass <- 0 until trainingPasses) {
        for (k <- Random.shuffle(train)) {
          lr.train(target(k), features(k))
        }
      }

      // this should reach to 100% of test.size on all correct predictions
      var numCorrect = 0

      val originalCount = Array.ofDim[Int](numClasses)
      val classifyCount = Array.ofDim[Int](numClasses)
      for (k <- test) {
        val v = features(k)
        val t = target(k)
        originalCount(t) += 1
        // classify the feature vector in test sample
        val r = lr.classifyFull(features(k)).maxValueIndex()

        classifyCount(r) += 1
        val isCorrect = r == target(k)
        numCorrect += (if (isCorrect) 1 else 0)
      }

      //      println(originalCount.toList)
      //      println(classifyCount.toList)

      correct(numCorrect) += 1
    }

    // how bad could the model behave?
    // in 100% correct scenario we will get correct(x) == 0 for all x < test.length
    // and correct(x) > 0 for x == test.length
    // 
    // in all other cases we will get correct(x) > 0 for some x < test.length
    // where correct(x) stores the number of passes
    // verify we never saw worse than 95% correct,

    val output = new File("output.dat")
    val stream = new PrintStream(output)

    val table = (0 until Math.floor(0.99 * test.length).toInt).map { i =>
      val inaccurateTrials = correct(i)
      val accuracy = 100.0 * i / test.length
      if (0 != inaccurateTrials) {
        val msg = "%d trials had unacceptable accuracy of only %.0f%%: "
          .format(inaccurateTrials, accuracy)
        println(msg, inaccurateTrials)
      }
      (accuracy, inaccurateTrials)
    }

    stream.println("accuracy\ttrials")
    table foreach { r =>
      stream.println(r._1 + "\t" + r._2)
    }
    stream.close()
    
    // $ gnuplot
    // gnuplot> plot 'output.dat' using 1:2 with lines title columnhead

    // nor perfect
    val msg = "%d trials had unrealistic accuracy of 100%%"
      .format(correct(test.length - 1))
    println(msg, (0, correct(test.length)))
  }
}