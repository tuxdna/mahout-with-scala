mahout-with-scala
========================

This project contains some code samples in a mix of Scala and Java, which use either of Hadoop and Mahout projects.

Run Sala API code to mahout-math ( version 0.9 ) library

    $ sbt 'run-main apitest'

## Download the datasets

[MovieLens](http://grouplens.org/datasets/movielens/)

[synthetic_control.data](http://archive.ics.uci.edu/ml/databases/synthetic_control/synthetic_control.data) from [Synthetic Control Data](http://archive.ics.uci.edu/ml/databases/synthetic_control/synthetic_control.data.html)


## Compiling and building

    sbt clean compile


## Running Scala code on hadoop with some SBT magic

Use some SBT magic

    $ CP=`sbt "export  compile:dependency-classpath" | tail -1`
    $ CP_CODE=`pwd`/target/scala-2.9.3/mahout-with-scala_2.9.3-1.0.jar
    $ export HADOOP_CLASSPATH=$CP:$CP_CODE
    
Now that we have HADOOP_CLASSPATH set, we can just invoke `hadoop` and run our code on Hadoop cluster

    $ hadoop utils.MyMainClass
    Hello0

### WordCountAverage

    $ sbt package
    $ hadoop fs -copyFromLocal input.txt .
    $ hadoop WordCountAverage input.txt output
    14/03/20 20:09:49 INFO input.FileInputFormat: Total input paths to process : 1
    14/03/20 20:09:49 INFO util.NativeCodeLoader: Loaded the native-hadoop library
    14/03/20 20:09:49 WARN snappy.LoadSnappy: Snappy native library not loaded
    14/03/20 20:09:50 INFO mapred.JobClient: Running job: job_201403141309_0003
    14/03/20 20:09:51 INFO mapred.JobClient:  map 0% reduce 0%
    14/03/20 20:10:00 INFO mapred.JobClient:  map 100% reduce 0%
    14/03/20 20:10:09 INFO mapred.JobClient:  map 100% reduce 33%
    14/03/20 20:10:11 INFO mapred.JobClient:  map 100% reduce 100%
    14/03/20 20:10:12 INFO mapred.JobClient: Job complete: job_201403141309_0003
    14/03/20 20:10:12 INFO mapred.JobClient: Counters: 29
    14/03/20 20:10:12 INFO mapred.JobClient:   Job Counters 
    14/03/20 20:10:12 INFO mapred.JobClient:     Launched reduce tasks=1
    14/03/20 20:10:12 INFO mapred.JobClient:     SLOTS_MILLIS_MAPS=10526
    14/03/20 20:10:12 INFO mapred.JobClient:     Total time spent by all reduces waiting after reserving slots (ms)=0
    14/03/20 20:10:12 INFO mapred.JobClient:     Total time spent by all maps waiting after reserving slots (ms)=0
    14/03/20 20:10:12 INFO mapred.JobClient:     Rack-local map tasks=1
    14/03/20 20:10:12 INFO mapred.JobClient:     Launched map tasks=1
    14/03/20 20:10:12 INFO mapred.JobClient:     SLOTS_MILLIS_REDUCES=10189
    14/03/20 20:10:12 INFO mapred.JobClient:   File Output Format Counters 
    14/03/20 20:10:12 INFO mapred.JobClient:     Bytes Written=996
    14/03/20 20:10:12 INFO mapred.JobClient:   FileSystemCounters
    14/03/20 20:10:12 INFO mapred.JobClient:     FILE_BYTES_READ=728
    14/03/20 20:10:12 INFO mapred.JobClient:     HDFS_BYTES_READ=127075
    14/03/20 20:10:12 INFO mapred.JobClient:     FILE_BYTES_WRITTEN=49515
    14/03/20 20:10:12 INFO mapred.JobClient:     HDFS_BYTES_WRITTEN=996
    14/03/20 20:10:12 INFO mapred.JobClient:   File Input Format Counters 
    14/03/20 20:10:12 INFO mapred.JobClient:     Bytes Read=126964
    14/03/20 20:10:12 INFO mapred.JobClient:   Map-Reduce Framework
    14/03/20 20:10:12 INFO mapred.JobClient:     Map output materialized bytes=728
    14/03/20 20:10:12 INFO mapred.JobClient:     Map input records=1731
    14/03/20 20:10:12 INFO mapred.JobClient:     Reduce shuffle bytes=728
    14/03/20 20:10:12 INFO mapred.JobClient:     Spilled Records=120
    14/03/20 20:10:12 INFO mapred.JobClient:     Map output bytes=216634
    14/03/20 20:10:12 INFO mapred.JobClient:     Total committed heap usage (bytes)=176099328
    14/03/20 20:10:12 INFO mapred.JobClient:     CPU time spent (ms)=2120
    14/03/20 20:10:12 INFO mapred.JobClient:     Combine input records=21663
    14/03/20 20:10:12 INFO mapred.JobClient:     SPLIT_RAW_BYTES=111
    14/03/20 20:10:12 INFO mapred.JobClient:     Reduce input records=60
    14/03/20 20:10:12 INFO mapred.JobClient:     Reduce input groups=60
    14/03/20 20:10:12 INFO mapred.JobClient:     Combine output records=60
    14/03/20 20:10:12 INFO mapred.JobClient:     Physical memory (bytes) snapshot=241704960
    14/03/20 20:10:12 INFO mapred.JobClient:     Reduce output records=60
    14/03/20 20:10:12 INFO mapred.JobClient:     Virtual memory (bytes) snapshot=2180493312
    14/03/20 20:10:12 INFO mapred.JobClient:     Map output records=21663
    

## Running the examples

### ClusteringRecommenderExample

    sbt "run-main ClusteringRecommenderExample grouplens/ml-1m/ratings.dat"

### MovieLensEvaluator

    sbt "run-main MovieLensEvaluator movielens/input"

### PopulateDbPreferences 

Create preference database table

    CREATE TABLE taste_preferences ( 
     user_id BIGINT NOT NULL, 
     item_id BIGINT NOT NULL, 
     preference FLOAT NOT NULL, 
     PRIMARY KEY (user_id, item_id), 
     INDEX (user_id), INDEX (item_id)
    );
	
Load the data into the table. This can be done in two ways:

First method is to directly load data into the table

    mysql> load data infile '/tmp/grouplens/ml-100k/ua.base' into table taste_preferences fields terminated by '\t' lines terminated by '\n' (user_id, item_id, preference);

    -- It is also possible to load data from STDIN like this:
    
    $ cat /tmp/grouplens/ml-100k/ua.base | mysql mia01 -u root -ppassword "load data infile '/dev/stdin' into table taste_preferences fields terminated by '\t' lines terminated by '\n' (user_id, item_id, preference);"

Described [here](http://eworbit.blogspot.in/2009/07/loading-mysql-data-via-stdin.html)

You may also want to disable Apparmor in Ubuntu, read [here](http://dijks.wordpress.com/2012/07/06/how-to-disable-apparmor-on-ubuntu-12-04-precise/), or Selinux in Fedora / RHEL.

Second method is to use PopulateDbPreferences

    sbt "run-main PopulatePreferences grouplens/ml-100k/ua.base"

### EvalThresholdNeighbourHood

    sbt "run-main EvalThresholdNeighbourHood grouplens/ml-1m/ratings.dat"

    Loading /usr/share/sbt/bin/sbt-launch-lib.bash
    [info] Running EvalThresholdNeighbourHood ...-1m/ml-1m/ratings.dat
    14/03/20 19:55:04 INFO file.FileDataModel: Creating FileDataModel for file /tmp/ratings.txt
    14/03/20 19:55:04 INFO file.FileDataModel: Reading file info...
    14/03/20 19:55:04 INFO file.FileDataModel: Processed 1000000 lines
    14/03/20 19:55:04 INFO file.FileDataModel: Read lines: 1000209
    14/03/20 19:55:05 INFO model.GenericDataModel: Processed 6040 users
    14/03/20 19:55:05 INFO eval.AbstractDifferenceRecommenderEvaluator: Beginning evaluation using 0.95 of GroupLensDataModel
    14/03/20 19:55:05 INFO model.GenericDataModel: Processed 324 users
    14/03/20 19:55:05 INFO eval.AbstractDifferenceRecommenderEvaluator: Beginning evaluation of 303 users
    14/03/20 19:55:05 INFO eval.AbstractDifferenceRecommenderEvaluator: Starting timing of 303 tasks in 4 threads
    14/03/20 19:55:05 INFO eval.StatsCallable: Average time per recommendation: 31ms
    14/03/20 19:55:05 INFO eval.StatsCallable: Approximate memory used: 177MB / 1012MB
    14/03/20 19:55:05 INFO eval.StatsCallable: Unable to recommend in 92 cases
    14/03/20 19:55:06 INFO eval.AbstractDifferenceRecommenderEvaluator: Evaluation result: 0.8563161666387115
    0.8563161666387115
    [success] Total time: 4 s ...
	

### LinearInterpolationRecommenderExample

    $ sbt "run-main LinearInterpolationRecommenderExample grouplens/ml-1m/ratings.dat"
    14/03/20 19:59:28 INFO file.FileDataModel: Creating FileDataModel for file /tmp/ratings.txt
    14/03/20 19:59:29 INFO file.FileDataModel: Reading file info...
    14/03/20 19:59:29 INFO file.FileDataModel: Processed 1000000 lines
    14/03/20 19:59:29 INFO file.FileDataModel: Read lines: 1000209
    14/03/20 19:59:30 INFO model.GenericDataModel: Processed 6040 users
    14/03/20 19:59:30 INFO eval.AbstractDifferenceRecommenderEvaluator: Beginning evaluation using 0.9 of GroupLensDataModel
    14/03/20 19:59:30 INFO model.GenericDataModel: Processed 631 users
    14/03/20 19:59:30 INFO eval.AbstractDifferenceRecommenderEvaluator: Beginning evaluation of 620 users
    14/03/20 19:59:30 INFO eval.AbstractDifferenceRecommenderEvaluator: Starting timing of 620 tasks in 4 threads
    14/03/20 19:59:30 INFO eval.StatsCallable: Average time per recommendation: 76ms
    14/03/20 19:59:30 INFO eval.StatsCallable: Approximate memory used: 239MB / 1019MB
    14/03/20 19:59:30 INFO eval.StatsCallable: Unable to recommend in 17 cases
    14/03/20 19:59:33 INFO eval.AbstractDifferenceRecommenderEvaluator: Evaluation result: 0.7653784855122873
    0.7653784855122873
    [success] Total time: 8 s ...

### SlopeOneExample ( only with Mahout 0.7 and below )

This doesn't work as of now

    $ sbt "run-main SlopeOneExample grouplens/ml-1m/ratings.dat"
    14/03/20 20:02:25 INFO file.FileDataModel: Creating FileDataModel for file /tmp/ratings.txt
    14/03/20 20:02:25 INFO file.FileDataModel: Reading file info...
    14/03/20 20:02:27 INFO file.FileDataModel: Processed 1000000 lines
    14/03/20 20:02:27 INFO file.FileDataModel: Read lines: 1000209
    14/03/20 20:02:27 INFO model.GenericDataModel: Processed 6040 users
    14/03/20 20:02:27 INFO eval.AbstractDifferenceRecommenderEvaluator: Beginning evaluation using 0.9 of GroupLensDataModel
    14/03/20 20:02:27 INFO model.GenericDataModel: Processed 587 users
    14/03/20 20:02:27 INFO slopeone.MemoryDiffStorage: Building average diffs...
    14/03/20 20:02:28 INFO eval.AbstractDifferenceRecommenderEvaluator: Beginning evaluation of 580 users
    14/03/20 20:02:28 INFO eval.AbstractDifferenceRecommenderEvaluator: Starting timing of 580 tasks in 4 threads
    14/03/20 20:02:28 INFO eval.StatsCallable: Average time per recommendation: 8ms
    14/03/20 20:02:28 INFO eval.StatsCallable: Approximate memory used: 383MB / 1030MB
    14/03/20 20:02:28 INFO eval.StatsCallable: Unable to recommend in 0 cases
    14/03/20 20:02:29 INFO eval.AbstractDifferenceRecommenderEvaluator: Evaluation result: 0.726474897564684
    0.726474897564684
    [success] Total time: 6 s ...    


### RecommenderLoad

    $ sbt "run-main RecommenderLoad grouplens/ml-1m/ratings.dat"
    14/03/20 20:13:12 INFO file.FileDataModel: Creating FileDataModel for file /tmp/ratings.txt
    14/03/20 20:13:12 INFO file.FileDataModel: Reading file info...
    14/03/20 20:13:13 INFO file.FileDataModel: Processed 1000000 lines
    14/03/20 20:13:13 INFO file.FileDataModel: Read lines: 1000209
    14/03/20 20:13:13 INFO model.GenericDataModel: Processed 6040 users
    14/03/20 20:13:13 INFO eval.AbstractDifferenceRecommenderEvaluator: Starting timing of 1075 tasks in 4 threads
    14/03/20 20:13:14 INFO eval.StatsCallable: Average time per recommendation: 103ms
    14/03/20 20:13:14 INFO eval.StatsCallable: Approximate memory used: 219MB / 1017MB
    14/03/20 20:13:14 INFO eval.StatsCallable: Unable to recommend in 0 cases
    14/03/20 20:13:25 INFO eval.StatsCallable: Average time per recommendation: 45ms
    14/03/20 20:13:25 INFO eval.StatsCallable: Approximate memory used: 301MB / 1034MB
    14/03/20 20:13:25 INFO eval.StatsCallable: Unable to recommend in 0 cases
    RecommendedItem[item:28, value:5.0]
    [success] Total time: 17 s

### BooleanPrefRecommender

This should fail with:

    $ sbt "run-main BooleanPrefRecommender grouplens/ml-100k/ua.base"

    java.lang.IllegalArgumentException: DataModel doesn't have preference values
    	at com.google.common.base.Preconditions.checkArgument(Preconditions.java:88)
    	at org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity.<init>(PearsonCorrelationSimilarity.java:74)
    	at org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity.<init>(PearsonCorrelationSimilarity.java:66)
    	at BooleanPrefRecommender$$anon$1.buildRecommender(BooleanPrefRecommender.scala:30)
    	at org.apache.mahout.cf.taste.impl.eval.AbstractDifferenceRecommenderEvaluator.evaluate(AbstractDifferenceRecommenderEvaluator.java:124)
    	at BooleanPrefRecommender$.main(BooleanPrefRecommender.scala:42)
    	at BooleanPrefRecommender.main(BooleanPrefRecommender.scala)
    	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
    	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
    	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
    	at java.lang.reflect.Method.invoke(Method.java:606)

Replace PearsonCorrelationSimilarity with TanimotoCoefficientSimilarity like this:

    val similarity = new TanimotoCoefficientSimilarity(model)
	// add IR stats evaluator as well
	{
      val evaluator = new GenericRecommenderIRStatsEvaluator()
      val stats = evaluator.evaluate(
        recommenderBuilder, modelBuilder, model, null, 10,
        GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0)
      println(stats.getPrecision())
      println(stats.getRecall())
    }
	 
Now run it again, and it gives difference of 0 and precision and recall values

    $ sbt "run-main BooleanPrefRecommender grouplens/ml-100k/ua.base"
    Generic user based recommender
    Score: 0.0
    Precision/Recall: (0.2674443266171791,0.2674443266171791)
    Boolean pref user based recommender
    Precision/Recall: (0.23669141039236477,0.23669141039236477)


