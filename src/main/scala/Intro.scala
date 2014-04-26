import java.io.File
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender
import scala.collection.JavaConversions._
import org.apache.mahout.cf.taste.model.DataModel

object Intro {

  def main(args: Array[String]) {

    val inputFile = if (args.length > 0) args(0) else "intro.csv"
    val modelFile = new File(inputFile)
    if (!modelFile.exists()) {
      println("Please, specify name of file, or put file 'input.csv' into current directory!")
      System.exit(1)
    }
    val model: DataModel = new FileDataModel(modelFile)
    val similarity = new PearsonCorrelationSimilarity(model)
    val neighborhood = new NearestNUserNeighborhood(2, similarity, model)
    val recommender = new GenericUserBasedRecommender(model, neighborhood, similarity)
    val recommendations = recommender.recommend(1, 10)
    recommendations foreach println
  }

}
