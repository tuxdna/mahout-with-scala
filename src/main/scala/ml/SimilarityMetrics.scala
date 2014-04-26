package ml

object SimilarityMetrics {
  /**
   * Calculate Pearson Correlation
   * @param u
   * @param v
   * @return
   */
  def pearsonCorrelation(u: Map[Int, Double], v: Map[Int, Double]): Double = {

    val xMean = u.values.sum / u.values.size
    val yMean = v.values.sum / v.values.size

    val diffValues = (u.keys ++ v.keys).map { key =>
      // what to do with missing values?
      val x = u.getOrElse(key, 0.0)
      val y = v.getOrElse(key, 0.0)

      (x - xMean, y - yMean)
    }

    val (cov, sx, sy) = diffValues.foldLeft((0.0, 0.0, 0.0)) { (a, b) =>
      val (x, y) = b
      (a._1 + (x * y), a._2 + (x * x), a._3 + (y * y))
    }
    // pearson correlation

    cov / (Math.sqrt(sx) * Math.sqrt(sy))
  }
}