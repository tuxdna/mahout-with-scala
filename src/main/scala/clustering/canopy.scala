package clustering

import scala.collection.mutable
import scala.util.Random

object canopy {
  type Point = Tuple2[Int, Int]

  def distance(p1: Point, p2: Point) = {
    val xdiff = p1._1 - p2._1
    val ydiff = p1._2 - p2._2
    math.sqrt(xdiff * xdiff + ydiff * ydiff)
  }

  def main(args: Array[String]) {
    var points = List(
      (1, 1), (2, 1), (1, 2), (2, 2), (3, 3),
      (8, 8), (9, 8), (8, 9), (9, 9), (5, 6))

    // T2 < T1
    val T1 = 7.0
    val T2 = 3.0
    val canopies = mutable.Map[Point, mutable.Set[Point]]()
    while (points.size > 0) {
      // new canopy
      val r = Random.shuffle(points)
      val C = r.head

      canopies foreach { x =>
        canopies(x._1).remove(C)
      }

      points = points filter (x => x != C)

      val canopy = mutable.Set[Point](C)
      canopies(C) = canopy
      for (P <- r.tail) {
        val dist = distance(C, P)
        if (dist <= T1) { canopy.add(P) }
        if (dist < T2) {
          points = points filter (x => x != P)
        }
      }
    }

    canopies foreach { x =>
      println("Cluster: %s => %s".format(x._1, x._2))
    }
  }
}