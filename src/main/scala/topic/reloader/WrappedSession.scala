package topic.reloader

import topic.reloader.lib.model.AppName
import org.apache.spark.sql.SparkSession

object WrappedSession {

  def buildSparkSession(appName: AppName): SparkSession =
    SparkSession
      .builder()
      .appName(appName.value)
      .getOrCreate()

  def buildLocalSparkSession(appName: AppName = AppName("local execution"), count: Int = 2): SparkSession = {

    val spark = SparkSession
      .builder()
      .appName(appName.value)
      .master(s"local[$count]")
      .getOrCreate()

    spark.sparkContext.hadoopConfiguration
      .set("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider")

    spark
  }
}
