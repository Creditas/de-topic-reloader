enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

val sparkVersion   = "3.0.0"
val hadoopVersion  = "3.2.1-amzn-1"
val jacksonVersion = "2.6.7"
val circeVersion   = "0.13.0"

lazy val root = (project in file(".")).settings(
  name := "topic-reloader",
  scalaVersion := "2.12.12",
  /*We can import Hive and Hadoop components from the official AWS maven repo*/
  resolvers += "aws-emr-6.1.0-repo" at "https://s3.us-east-1.amazonaws.com/us-east-1-emr-artifacts/emr-6.1.0/repos/maven/",
  resolvers += "confluent" at "https://packages.confluent.io/maven/",
  libraryDependencies ++= Seq(
    /* execution */
    "org.apache.spark"  %% "spark-core"   % sparkVersion % Provided,
    "org.apache.spark"  %% "spark-sql"    % sparkVersion % Provided,
    "org.apache.spark"  %% "spark-sql-kafka-0-10" % sparkVersion % Provided,
    "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Provided,
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Provided,
    "org.apache.hadoop" % "hadoop-aws"    % hadoopVersion % Provided,
    /* not provided */
    "com.typesafe"      % "config"       % "1.4.0",
    "za.co.absa"       %% "abris"        % "4.0.0",
    "org.apache.spark" %% "spark-avro"   % sparkVersion,
    "io.circe"         %% "circe-core"   % circeVersion,
    "io.circe"         %% "circe-parser" % circeVersion,
    /* test */
    "org.apache.spark"  %% "spark-core"   % sparkVersion % Test,
    "org.apache.spark"  %% "spark-sql"    % sparkVersion % Test,
    "org.apache.spark"  %% "spark-sql-kafka-0-10" % sparkVersion % Test,
    "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test,
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test,
    "org.apache.hadoop" % "hadoop-aws"    % hadoopVersion % Test,
    "org.scalatest"     %% "scalatest"    % "3.2.0"       % Test
  )
)

// from: https://www.paschen.ch/2019/05/unit-testing-spark-scala-code/
fork in Test := true
parallelExecution in Test := false

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
  case PathList("javax", "ws", "rs", xs @ _*) => MergeStrategy.first
  case PathList("module-info.class") => MergeStrategy.first
  case x =>
    (assemblyMergeStrategy in assembly).value(x)
}

