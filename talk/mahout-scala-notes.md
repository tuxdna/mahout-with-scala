

## Talk proposal

Apache Mahout is a popular technology in the Machine Learning domain.

Objective: Writing Machine Learning tasks in Scala using Apache Mahout.

Abstract:

Apache Mahout is an Apache Foundation project for Scalable Machine Learning and Data Mining. It covers only scalable machine learning algorithms, which generally run on top of Apache Hadoop. However, Mahout is not limited to only map-reduce based algorithms. Mahout consists of High Performance Java collections which form a foundation for different clustering, classification, and collaborative filtering algorithms. To a beginner, merely creating a development setup is a daunting task. In this talk I will present a working demo of Mahout.

While Machine Learning is an inter-disciplinary field, it has applications in many domains. Mahout provides ready to use implementations with many use cases:
 * Product Recommendation ( understanding / inferring what your customers are looking for )
 * Topic Modeling ( identifying topics from documents )
 * Frequent Patterns Mining ( knowing which entities occur together very often )
 * Clustering ( grouping similar items or grouping very similar documents, which are perhaps talking about the same subject )
 * Regression and Classification ( predicting house prices / identifying a class of a document of an item )


Scala is a Functional Programming Language.

Apache Mahout is a library which provides tools and APIs to write scalable machine learning algorithms. Machine Learning algorithms are inherently based on mathematical concepts such as probability and statistics.

The combination of Scala and Apache Mahout makes perfect sense in both experimental and real world scenarios.

In this talk I will discuss ways to combine the power of Scala and Apache Mahout, to make sense out of data:

 * Using Classification, Clustering and Recommendation algorithms in Scala
 * Using Scala primitives provided by Apache Mahout
 * and a demo

Requirements:

 * A basic understanding of Scala Programming Language
 * Basic mathematics: Probability and Statistics 

Duration: 1 hour

## Posted at


[Machine Learning with Apache Mahout and Scala](http://osdconf.in/funnel/osdconf14/8-machine-learning-with-apache-mahout-and-scala)


## Some more notes


Similarity / Distance metric
Vector and Matrices

* MAHOUT-1446 Create an intro for matrix factorization
* MAHOUT-1485 Clean up Recommender Overview page
* MAHOUT-1495 Create a website describing the distributed item-based recommender

Another very important issue would be to make sure the Hadoop-based recommenders run on Hadoop 2

http://blogs.technet.com/b/oliviaklose/archive/2014/04/15/mahout-for-dummies-1.aspx

http://blogs.technet.com/b/oliviaklose/archive/2014/04/14/mahout-for-dummies-2-step-by-step-mahout-and-hdinsight-interactive-style.aspx


Which version to choose from?
 
Mahout 0.7
 * SlopeOne Recommender
 * Dirichlet clustering

Mahout 0.8

SlopeOne Recommender removed
   - removed from Mahout 0.8 onwards
   https://issues.apache.org/jira/browse/MAHOUT-1250

Mahout 0.9 ( latest )
Mahout 1.0 ( upcoming )

 - Basic Mahout Concepts (Algorithms available and their classification)

 - Data Structures implemented in Mahout for various Algorithms (vectors, matrices, various other optimized collections)
 
 Fast data structures: FastIDSet, FastByIDMap
 Distance/Similarity Metrics: PearsonCorrelation, EuclideanDistance, Jaccard Distance, LogLikelihood, Spearman Correlation etc.
 Vectors:

 - Taste Framework and Standalone Recommenders
 - Implement a distributed Item Based Recommender on Hadoop with Movielens Data


 CF types: User based, Item based, Content based
 Basic Algorithm and neighbourhood algorithm
 Neighbourhood metrics: fixed size / threshold value

 SlopeOne Recommender
   - only present uptil Mahout 0.7
   - removed from Mahout 0.8 onwards
   https://issues.apache.org/jira/browse/MAHOUT-1250
   
 - Various Clustering Algorithms in Mahout (Canopy, K-means, Mean shift, Dirichlet, etc)
 - Implement and Run a Clustering Algorithm on the Movies/Users of the MovieLens data on Hadoop


Converting text files to sequence files, and then to sparse vectors for running clustering algorithms.

Mahout0.7 - Experimented with KMeans, FKmeans, Dirichlet

Mahout0.9 - Experimented with KMeans, FKmeans, cvb

Different Approaches: hierarchical, top-down, bottom up, generative, discriminative


 -  Various Classification Algorithms with Mahout (Bayes, etc)
 -  Implement and Run a Classification Algorithm on the Movies/Users of the MovieLens data on Hadoop

Some terms: record, features, predictor variable, target variable, model

Stochastic Gradiend Descent Algorithm

OnlineLogisticRegression

AdaptiveLogisticRegression

CrossFoldLearner

examples/bin/classify-20newsgroups.sh

Fix ./classify-20newsgroups.sh
 * Assumes that `HADOOP_HOME` is set although `HADOOP_HOME` is now deprecated.
 
