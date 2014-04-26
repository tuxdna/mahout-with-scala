import org.apache.mahout.math.{ Vector => MahoutVector }
import org.apache.mahout.math.scalabindings._
import org.apache.mahout.math.DenseMatrix
import scala.util.Random
import org.apache.mahout.math.Matrix
import Math.min
import RLikeOps._

/* Code taken from here:
 * http://weatheringthrutechdays.blogspot.com.br/2013/07/scala-dsl-for-mahout-in-core-linear.html
 */
object apitest extends App {
  /**
   * In-core SSVD algorithm.
   *
   * @param a input matrix A
   * @param k request SSVD rank
   * @param p oversampling parameter
   * @param q number of power iterations
   * @return (U,V,s)
   */
  def ssvd(a: Matrix, k: Int, p: Int = 15, q: Int = 0) = {

    val m = a.numRows()
    val n = a.numCols()

    if (k > min(m, n))
      throw new IllegalArgumentException(
        "k cannot be greater than smaller of m,n")

    val pfxed = min(p, min(m, n) - k)

    // actual decomposition rank
    val r = k + pfxed

    // we actually fill the random matrix here
    // just like in our R prototype, although technically
    // that would not be necessary if we implemented specific random
    // matrix view. But ok, this should do for now.
    // it is actually the distributed version we are after -- we
    // certainly would try to be efficient there.

    val rnd = new Random()
    val omega = new DenseMatrix(n, r) := ((r, c, v) => rnd.nextGaussian)

    var y = a %*% omega
    var yty = y.t %*% y
    val at = a.t
    var ch = chol(yty)
    var bt = ch.solveRight(at %*% y)

    // power iterations
    for (i <- 0 until q) {
      y = a %*% bt
      yty = y.t %*% y
      ch = chol(yty)
      bt = ch.solveRight(at %*% y)
    }

    val bbt = bt.t %*% bt
    val (uhat, d) = eigen(bbt)

    val s = d.sqrt
    val u = ch.solveRight(y) %*% uhat
    val v = bt %*% (uhat %*%: diagv(1 /: s))

    (u(::, 0 until k), v(::, 0 until k), s(0 until k))

  }

  def testSSVD = {

    val a = dense((1, 2, 3), (3, 4, 5))

    val (u, v, s) = ssvd(a, 2, q = 1)

    printf("U:\n%s\n", u)
    printf("V:\n%s\n", v)
    printf("Sigma:\n%s\n", s)

    val aBar = u %*% diagv(s) %*% v.t

    val amab = a - aBar

    printf("A-USV'=\n%s\n", amab)

    assert(amab.norm < 1e-10)

  }

  val v1 = dvec(1, 0, 1.1, 1.2)
  val denseVec1: MahoutVector = (1.0, 1.1, 1.2)
  val sparseVec = svec((5 -> 1) :: (10 -> 2.0) :: Nil)
  val sparseVec2: MahoutVector = (5 -> 1.0) :: (10 -> 2.0) :: Nil
  val sparseVec3: MahoutVector = List(5 -> 1.0, 10 -> 2.0)

  val denseMat1 = dense((1, 2, 3), (3, 4, 5))
  val sparseMat1 = sparse(
    (1, 3) :: Nil,
    (0, 2) :: (1, 2.5) :: Nil)

  val sparseMat2 = sparse(
    List(1 -> 3),
    List(0 -> 2, 1 -> 2.5))

  val diagMat1 = diag(3.5, 10)

  val diagMat2 = diagv((1, 2, 3, 4, 5))

  val i10 = eye(10)

  val vec = svec(List(5 -> 1.0, 10 -> 2.0))

  val d = vec(5)

  val m = dense((1, 2, 3), (3, 4, 5, 6, 7, 8),
    (1, 2, 3, 4, 5), (3, 4, 5, 6, 7, 8), (1, 0, 1.1, 1.2), (1, 2, 3, 4, 5),
    (1, 0, 1.1, 1.2))

  m := ((r, c, x) => Math.pow(x, 2))

  val d1 = m(3, 2)

  m(3, 2) = 3.0
  val rowVec = m(3, ::)
  val colVec = m(::, 2)
  m(3, ::) := (1, 2, 3)
  m(::, 2) := (1, 2, 3)

  sparseMat2(0, 0 to 1) = (3, 5)
  sparseMat2(0, 0 to 1) := (3, 5)

  // this fails -- org.apache.mahout.math.IndexException: Index 4 is outside allowable range of [0,2)
  // val b = sparseMat2(2 to 3, 0 to 1)

  testSSVD
}
