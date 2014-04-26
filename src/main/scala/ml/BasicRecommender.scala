package ml

object BasicRecommender {

  /**
   * Basic recommender algorithm
   * @param preferences
   * @param itemIds
   * @param userIds
   * @param similarityFunction
   * @return
   */
  def basicRecommender(
    preferences: Map[Long, Map[Int, Double]],
    itemIds: Set[Int],
    userIds: Set[Long],
    similarityFunction: ((Map[Int, Double], Map[Int, Double]) => Double),
    u: Long): List[Tuple2[Int, Double]] = {

    /*
    for every item i that u has no preference for yet
      for every other user v that has a preference for i
        compute a similarity s between u and v
        incorporate v's preference for i, weighted by s, into a running average
    return the top items, ranked by weighted average
     */
    val rs = itemIds
      .filterNot(i => preferences(u).contains(i)).map { i =>
        val rv = userIds
          .filterNot { v => v == u } // exclude u
          .filter { v => preferences(v).contains(i) } // v has a preference for i
          .map { v =>
            val prefU = preferences(u)
            val prefV = preferences(v)
            val sim = similarityFunction(prefU, prefV) // similarity between u and v
            val weightedPref = sim * preferences(v)(i) // 
            (v, weightedPref)
          }
        val sum = rv.foldLeft(0.0)((a, b) => a + b._2)
        (i, sum / rv.size)
      }.toList.sortBy(_._2).reverse

    rs
  }

}