package classification

import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder
import com.ibm.icu.impl.duration.impl.DataRecord
import org.apache.mahout.math.RandomAccessSparseVector

object various {

  def main(args: Array[String]): Unit = {
    val encoder = new StaticWordValueEncoder("variable-name")

    val sentence = "Life is a puzzle. Try to solve it, but dont, coz you really cant. Choice is yours"
    val v = new RandomAccessSparseVector(10000)
    for (word <- sentence.split("\\s+")) {
      // println(word)
      encoder.addToVector(word, v);
    }

    println(v)
  }
}