name := "mahout-with-scala"

scalaVersion :="2.9.3"

version :="1.0"

libraryDependencies += "org.apache.hadoop" % "hadoop-core" % "1.2.1"

// libraryDependencies += "org.apache.hadoop" % "hadoop-test" % "1.2.1"

// libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"

libraryDependencies += "org.apache.mahout" % "mahout-core" % "0.9"

libraryDependencies += "org.apache.mahout" % "mahout-math" % "0.9"

libraryDependencies += "org.apache.mahout" % "mahout-examples" % "0.9"

libraryDependencies += "org.apache.mahout" % "mahout-math-scala" % "0.9"

libraryDependencies += "org.scala-lang" % "scala-library" % "2.9.3"

// required for google guava collections
// http://stackoverflow.com/questions/10007994/why-do-i-need-jsr305-to-use-guava-in-scala
// libraryDependencies += "net.sourceforge.findbugs" % "jsr305" % "1.3.7"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "2.0.3"
