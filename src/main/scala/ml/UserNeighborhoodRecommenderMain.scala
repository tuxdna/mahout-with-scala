package ml

import scala.io.Source
import java.io.File
import scala.collection.immutable.TreeMap

object UserNeighborhoodRecommenderMain {

  def main(args: Array[String]) {
    val inputFile = if (args.length > 0) args(0) else "intro.csv"
    val modelFile = new File(inputFile)
    if (!modelFile.exists()) {
      println("Please, specify name of file, or put file 'input.csv' into current directory!")
      System.exit(1)
    }

    val src = Source.fromFile(modelFile)
    val preferences = src
      .getLines
      .map { line => line.split(",") }
      .filter { e => e.length == 3 }
      .map { e => (e(0).toLong, e(1).toInt, e(2).toDouble) }
      .foldLeft(TreeMap[Long, TreeMap[Int, Double]]()) { (m, e) =>
        val (userId, itemId, preference) = e
        val values = m.getOrElse(userId, TreeMap[Int, Double]())
        m + (userId -> (values + (itemId -> preference)))
      }

    val userIds = preferences.keySet
    val itemIds = preferences.values.map(x => x.keySet).reduce((x, y) => x union y)
    println(userIds)
    println(itemIds)
    preferences foreach println

    val rs = UserNeighborhoodRecommender.recommender(preferences, itemIds, userIds,
      SimilarityMetrics.pearsonCorrelation, 1L)

    println("Recommendations")
    rs foreach println
  }
}