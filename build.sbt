name := "Veracious"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.1"

val sparkVersion = "1.3.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

fork := true

fork in run := true

// add a JVM option to use when forking a JVM for 'run'
javaOptions ++= Seq("-Xmx2G")

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

//  depenedencies for Akka
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9"
)

// All the apache spark dependencies
libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % sparkVersion excludeAll ExclusionRule(organization = "org.slf4j"),
  "org.apache.spark" % "spark-sql_2.11" % sparkVersion,
  "org.apache.spark" % "spark-streaming_2.11" % sparkVersion,
  "org.apache.spark" % "spark-mllib_2.11" % sparkVersion
)

// Webjar dependencies
libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "angularjs" % "1.3.15",
  "org.webjars" % "bootstrap" % "3.3.4"
)


// Scala-csv library dependency
// Libary found at http://github.com/tototoshi/scala-csv
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.1.2"

// ScalaTest library dependency for testing
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

// All the Apache Spark resolvers
resolvers ++= Seq(
  "Apache repo" at "https://repository.apache.org/content/repositories/releases",
  "Local Repo" at Path.userHome.asFile.toURI.toURL + "/.m2/repository", // Added local repository
  Resolver.mavenLocal)

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)


//Compiler plugins


// linter: static analysis for scala
resolvers += "Linter Repository" at "https://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1.8")


/// console

// define the statements initially evaluated when entering 'console', 'consoleQuick', or 'consoleProject'
// but still keep the console settings in the sbt-spark-package plugin

// If you want to use yarn-client for spark cluster mode, override the environment variable
// SPARK_MODE=yarn-client <cmd>
val sparkMode = sys.env.getOrElse("SPARK_MODE", "local[2]")


//initialCommands in console :=
//  s"""
//     |import org.apache.spark.SparkConf
//     |import org.apache.spark.SparkContext
//     |import org.apache.spark.SparkContext._
//     |
//     |@transient val sc = new SparkContext(
//     | new SparkConf()
//     |   .setMaster("$sparkMode")
//                                 |   .setAppName("Console test"))
//                                 |implicit def sparkContext = sc
//                                 |import sc._
//                                 |
//                                 |@transient val sqlc = new org.apache.spark.sql.SQLContext(sc)
//                                 |implicit def sqlContext = sqlc
//                                 |import sqlc._
//                                 |
//                                 |def time[T](f: => T): T = {
//                                 | import System.{currentTimeMillis => now}
//                                 | val start = now
//                                 | try { f } finally { println("Elapsed: " + (now - start)/1000.0 + " s") }
//                                 |}
//                                 |
//                                 |""".stripMargin


/// scaladoc
scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits",
  // NOTE: remember to change the JVM path that works on your system.
  // Current setting should work for JDK7 on OSX and Linux (Ubuntu)
  "-doc-external-doc:/Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/lib/rt.jar#http://docs.oracle.com/javase/7/docs/api",
  "-doc-external-doc:/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/rt.jar#http://docs.oracle.com/javase/7/docs/api"
)

autoAPIMappings := true
