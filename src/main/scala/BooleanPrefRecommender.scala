import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator
import java.io.File
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender
import org.apache.mahout.cf.taste.eval.RecommenderBuilder
import org.apache.mahout.cf.taste.model.DataModel
import org.apache.mahout.cf.taste.recommender.Recommender
import org.apache.mahout.cf.taste.eval.DataModelBuilder
import org.apache.mahout.cf.taste.impl.common.FastByIDMap
import org.apache.mahout.cf.taste.model.PreferenceArray
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity

object BooleanPrefRecommender {

  def main(args: Array[String]) {

    val inputFile = if (args.length > 0) args(0) else "ua.base"
    val modelFile = new File(inputFile)
    if (!modelFile.exists()) {
      println("Please, specify name of file, or put file 'input.csv' into current directory!")
      System.exit(1)
    }

    val model = new GenericBooleanPrefDataModel(GenericBooleanPrefDataModel.toDataMap(new FileDataModel(modelFile)));

    val modelBuilder = new DataModelBuilder {
      override def buildDataModel(trainingData: FastByIDMap[PreferenceArray]): DataModel = {
        return new GenericBooleanPrefDataModel(GenericBooleanPrefDataModel.toDataMap(trainingData))
      }
    }

    val recommenderBuilder = new RecommenderBuilder {
      override def buildRecommender(model: DataModel): Recommender = {
        // val similarity = new PearsonCorrelationSimilarity(model)
        val similarity = new TanimotoCoefficientSimilarity(model)
        val neighborhood = new NearestNUserNeighborhood(10, similarity, model)
        new GenericUserBasedRecommender(model, neighborhood, similarity);
      }
    }

    println("Generic user based recommender")

    // average diff evaluator
    {
      val evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator()
      val score = evaluator.evaluate(recommenderBuilder, modelBuilder, model, 0.9, 1.0)
      println("Score: " + score)
    }

    // IR stats evaluator
    {
      val evaluator = new GenericRecommenderIRStatsEvaluator()
      val stats = evaluator.evaluate(
        recommenderBuilder, modelBuilder, model, null, 10,
        GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0)

      println("Precision/Recall: " + (stats.getPrecision(), stats.getRecall()))
    }

    println("Boolean pref user based recommender")

    // boolean user based recommender
    {
      val recommenderBuilder = new RecommenderBuilder {
        override def buildRecommender(model: DataModel): Recommender = {
          // val similarity = new PearsonCorrelationSimilarity(model)
          //  val similarity = new TanimotoCoefficientSimilarity(model)
          val similarity = new LogLikelihoodSimilarity(model)
          val neighborhood = new NearestNUserNeighborhood(10, similarity, model)
          new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity);
        }
      }

      val evaluator = new GenericRecommenderIRStatsEvaluator()
      val stats = evaluator.evaluate(
        recommenderBuilder, modelBuilder, model, null, 10,
        GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0)

      println("Precision/Recall: " + (stats.getPrecision(), stats.getRecall()))
    }

  }

}
