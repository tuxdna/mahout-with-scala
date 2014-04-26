package ml

import scala.io.Source
import java.io.File
import scala.collection.immutable.TreeMap

object UserNeighborhoodRecommender {

  def recommender(
    preferences: Map[Long, Map[Int, Double]],
    itemIds: Set[Int],
    userIds: Set[Long],
    similarityFunction: ((Map[Int, Double], Map[Int, Double]) => Double),
    u: Long): List[Tuple2[Int, Double]] = {
    /*

for every other user w
  compute a similarity s between u and w
  retain the top users, ranked by similarity, as a neighbourhood n

for item i in neighbourhood except the ones rated by u
  for user v in neighbourhood who has a preference for i
    compute a similarity s between u and v
    incorporate v's preference for i, weighted by s, into a running average

return the top items, ranked by weighted average
 
     */

    val topN = 2

    val neighbours = userIds.filterNot(_ == u).toList.map { w =>
      val sim = similarityFunction(preferences(u), preferences(w))
      (w, sim)
    }.sortBy(_._2).reverse.map(_._1).take(topN)

    // find items rated by neighbour and not by u
    val itemsInNeighbourhood = neighbours.flatMap { neighbour =>
      preferences(neighbour).keys.filterNot(item => preferences(u).contains(item))
    }

    val weightedPreferences = itemsInNeighbourhood.flatMap { i =>
      val ratersOfItem = neighbours.filter(v => preferences(v).contains(i))
      ratersOfItem.map { v =>
        val sim = similarityFunction(preferences(u), preferences(v))
        val pref = preferences(v)(i)
        val weightedPref = sim * pref
        (i, weightedPref)
      }
    }

    val redommendedItems = weightedPreferences.groupBy(_._1).map { x =>
      val (item, weightedPrefs) = x
      val sum = weightedPrefs.foldLeft(0.0)((a, b) => a + b._2)
      (item, sum / weightedPrefs.size)
    }.toList.sortBy(_._2).reverse

    redommendedItems
  }

}
