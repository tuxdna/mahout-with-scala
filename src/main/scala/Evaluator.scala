import org.apache.mahout.common.RandomUtils
import java.io.File
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator
import org.apache.mahout.cf.taste.eval.RecommenderBuilder
import org.apache.mahout.cf.taste.model.DataModel
import org.apache.mahout.cf.taste.recommender.Recommender
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator

object Evaluator {

  def main(args: Array[String]) {
    RandomUtils.useTestSeed()
    val inputFile = if (args.length > 0) args(0) else "intro.csv"
    val modelFile = new File(inputFile)
    if (!modelFile.exists()) {
      println("Please, specify name of file, or put file 'input.csv' into current directory!")
      System.exit(1)
    }

    val model = new FileDataModel(modelFile)
    val rmsEval = new RMSRecommenderEvaluator
    val avgEval = new AverageAbsoluteDifferenceRecommenderEvaluator
    val evaluator = avgEval

    val recommenderBuilder = new RecommenderBuilder {
      override def buildRecommender(model: DataModel): Recommender = {
        val similarity = new PearsonCorrelationSimilarity(model)
        val neighborhood = new NearestNUserNeighborhood(2, similarity, model)
        new GenericUserBasedRecommender(model, neighborhood, similarity)
      }
    }

    val score = evaluator.evaluate(recommenderBuilder, null, model, 0.7, 1.0)
    println(score)
  }
}