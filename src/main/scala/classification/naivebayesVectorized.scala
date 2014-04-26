package classification

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.io.Source
import org.apache.mahout.math.DenseVector
import org.apache.mahout.vectorizer.encoders.Dictionary
import scala.collection.JavaConversions._
import scala.util.Random
import java.net.URL
import org.apache.mahout.math.{ Vector => MahoutVector }

// Scala DSL for Mahout-Math
import org.apache.mahout.math.scalabindings._
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.Matrix
import Math.min
import RLikeOps._

object naivebayesVectorized {

  def main(args: Array[String]) {

    val irisData = if (args.length > 0) new URL(args(0)) else getClass.getResource("/iris.csv")

    // a dictionary for encoding categories
    val dict = new Dictionary()
    val categoryMapping = mutable.Map[Int, String]()

    var featuresBuf = ArrayBuffer[MahoutVector]()
    var targetBuf = ArrayBuffer[Int]()
    var rowCount = 0

    println("Read data from file: " + irisData)

    // Sepal.Length,Sepal.Width,Petal.Length,Petal.Width,Species
    Source.fromURL(irisData).getLines()
      .drop(1) // skip header
      .filter(_ != null) // skip empty lines
      .map(_.split(",")).filter(_.length == 5) // accept only lines with 5 elements
      .foreach { elems =>

        val v: MahoutVector = dvec(1,
          elems(0).toDouble, //Sepal.Length
          elems(1).toDouble, //Sepal.Width
          elems(2).toDouble, //Petal.Length
          elems(3).toDouble) //Petal.Width

        val category = elems(4)
        val categoryCode = dict.intern(category)
        categoryMapping(categoryCode) = elems(4)
        targetBuf += categoryCode
        featuresBuf += v
        rowCount += 1
      }

    val features = new DenseMatrix(rowCount, 5)
    val target = new DenseMatrix(rowCount, 1)
    (0 until rowCount) foreach { row =>
      features(row, ::) = featuresBuf(row)
      target(row, ::) = dvec(targetBuf(row))
    }

    println("Features and target variable loaded\n")

    // categories
    println("Categories and their codes: ")
    println(dict.values().map(cat => (cat, dict.intern(cat))).toMap)
    println

    println("Building the NaiveBayes model...")

    // group entries by target category
    val groups = (0 until rowCount).toList.groupBy(row => target(row, 0))

    val model = groups
      .map { x =>
        val category = x._1
        val featureRowIds = x._2
        val count = featureRowIds.length

        // calculate stats: sum, mean, variance, sigma (or standard deviation)
        val sumVector: MahoutVector = dvec(0, 0, 0, 0, 0)
        for (row <- featureRowIds) { sumVector += features(row, ::) }
        val meanVec: MahoutVector = sumVector / count

        // calculate diff squared sum
        val diffSquaredSumVec: MahoutVector = new DenseVector(5)
        featureRowIds.foreach { row =>
          val v = features(row, ::)
          val diff = v - meanVec
          val diffSq = diff * diff
          diffSquaredSumVec += diffSq
        }

        // variance
        val varianceVec = diffSquaredSumVec / count

        // standard deviation
        val sigmaVec = varianceVec.cloned
        sigmaVec := ( (c, x) => Math.sqrt(x / count) )

        // println("Sigma: " + sigmaVec)

        (category, count, count.toDouble / features.numRows, meanVec, sigmaVec)
      }.toList

    println("Generated model is:")
    model foreach println
    println

    println("Category and its probability pairs:")
    model.foreach { x =>
      val cat = x._1
      val count = x._2
      val catProb = x._3
      val mu = x._4
      val sigma = x._4
      println(cat -> catProb)
    }
    println

    println("Evaluate Naive Bayes on same dataset picked at random")
    val testData = Random.shuffle((0 until rowCount).toList).take(15)
    testData.foreach { testRow =>
      val a = features(testRow, ::)
      val originCat = target(testRow, 0)

      val output = model.map { x =>
        val cat = x._1
        val count = x._2
        val probCi = x._3
        val mu = x._4
        val sigma = x._4

        // calculate probability for this class
        var probXCi = 1.0
        for (i <- 0 until a.size()) {
          probXCi *= g(a.get(i), mu.get(i), sigma.get(i))
        }
        (cat, probCi, probXCi, probXCi * probCi)
      }.maxBy(_._4) // find the one with highest probability

      println("Classified as: %d,  Original Category: %d".format(output._1.toInt, originCat.toInt))
      println("Explanation: " + (output, originCat))
      println
    }
  }

  // gaussian distribution
  def g(x: Double, mu: Double, sigma: Double) = {
    val denom = Math.sqrt(2.0 * Math.Pi * sigma)
    val epart = Math.pow(Math.E, -((x - mu) * (x - mu) / (2 * sigma * sigma)))
    1.0 * epart / denom
  }

}
