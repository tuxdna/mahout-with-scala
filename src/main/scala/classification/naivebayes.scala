package classification
import org.apache.mahout.math.{ Vector => MahoutVector }
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.io.Source
import org.apache.mahout.math.DenseVector
import org.apache.mahout.vectorizer.encoders.Dictionary
import scala.collection.JavaConversions._
import scala.util.Random
import java.net.URL
import org.apache.mahout.math.{ Vector => MahoutVector }

object naivebayes {

  def main(args: Array[String]) {
    val irisData = if (args.length > 0) new URL(args(0)) else getClass.getResource("/iris.csv")

    // a dictionary for encoding categories
    val dict = new Dictionary()
    val categoryMapping = mutable.Map[Int, String]()

    var features = ArrayBuffer[MahoutVector]()
    var target = ArrayBuffer[Int]()

    println("Read data from file: " + irisData)

    // Sepal.Length,Sepal.Width,Petal.Length,Petal.Width,Species
    Source.fromURL(irisData).getLines()
      .drop(1) // skip header
      .filter(_ != null) // skip empty lines
      .map(_.split(",")).filter(_.length == 5) // accept only lines with 5 elements
      .foreach { elems =>
        val v: MahoutVector = new DenseVector(5)
        v.set(0, 1) // constant term
        v.set(1, elems(0).toDouble) //Sepal.Length 
        v.set(2, elems(1).toDouble) //Sepal.Width
        v.set(3, elems(2).toDouble) //Petal.Length
        v.set(4, elems(3).toDouble) //Petal.Width
        val category = elems(4)
        val categoryCode = dict.intern(category)
        categoryMapping(categoryCode) = elems(4)
        target += categoryCode
        features += v
      }

    println("Features and target variable loaded\n")

    // categories
    println("Categories and their codes: ")
    println(dict.values().map(cat => (cat, dict.intern(cat))).toMap)
    println

    println("Building the NaiveBayes model...")
    val model = features.zip(target).groupBy(_._2) // group entries by target category
      .map { x =>
        val category = x._1
        val featureVectorCategoryPairs = x._2
        val count = featureVectorCategoryPairs.length

        // calculate stats: sum, mean, variance, sigma (or standard deviation)
        val sumVector: MahoutVector = new DenseVector(5)

        featureVectorCategoryPairs.foreach { x =>
          val featureVector = x._1
          for (i <- 0 until sumVector.size()) {
            val xk = sumVector.get(i)
            sumVector.setQuick(i, xk + featureVector.get(i))
          }
        }
        // println("Sum: " + sumVec)

        val meanVec: MahoutVector = new DenseVector(5)
        for (i <- 0 until sumVector.size()) {
          val sumVal = sumVector.get(i)
          meanVec.setQuick(i, sumVal / count)
        }
        // println("Mu: " + muVec)

        val varianceVec: MahoutVector = new DenseVector(5)
        // calculate diff squared sum
        featureVectorCategoryPairs.foreach { x =>
          val v = x._1
          for (i <- 0 until meanVec.size()) {
            val mu = meanVec.get(i)
            val xk = v.get(i)
            val diff = xk - mu
            val diffSq = diff * diff
            val variance = varianceVec.get(i) + diffSq
            varianceVec.setQuick(i, variance)
          }
        }

        // divide by N
        for (i <- 0 until varianceVec.size()) {
          val variance = varianceVec.get(i) / count
          varianceVec.setQuick(i, variance)
        }

        // println("Variance: " + varianceVec)

        val sigmaVec: MahoutVector = new DenseVector(5)
        // divide by N
        for (i <- 0 until varianceVec.size()) {
          val variance = varianceVec.get(i) / count
          sigmaVec.setQuick(i, Math.sqrt(variance))
        }

        // println("Sigma: " + sigmaVec)

        (category, count, count / features.length.toDouble, meanVec, sigmaVec)
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

    // println(g(35, 30, 12))

    println("Evaluate Naive Bayes on same dataset picked at random")
    val testData = Random.shuffle(features.zip(target)).take(5)
    testData.foreach { t =>
      val a = t._1
      val originCat = t._2
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
      }.maxBy(_._4)

      println(output, originCat)
    }
  }

  // gaussian distribution
  def g(x: Double, mu: Double, sigma: Double) = {
    val denom = Math.sqrt(2.0 * Math.Pi * sigma)
    val epart = Math.pow(Math.E, -((x - mu) * (x - mu) / (2 * sigma * sigma)))
    1.0 * epart / denom
  }

}