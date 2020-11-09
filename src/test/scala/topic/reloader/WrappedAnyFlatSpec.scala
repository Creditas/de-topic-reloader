package topic.reloader

import java.nio.file.Path
import java.nio.file.Paths
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import topic.reloader.WrappedSession.buildLocalSparkSession
import topic.reloader.lib.model.AppName
import topic.reloader.lib.model.Settings
import topic.reloader.lib.model.SubmissionID
import topic.reloader.lib.utils.FileSystem

trait WrappedAnyFlatSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  implicit lazy val spark: SparkSession = buildLocalSparkSession(AppName("EventProcessorTest"), 2)

  override def afterAll(): Unit = spark.stop()
}

object WrappedAnyFlatSpec {

  def testTmpDir: Path = Paths.get(FileSystem.tmpDir.toString, "test")

  implicit val settings: Settings = topic.reloader.lib.model.Settings(
    appName = AppName("EventProcessorTest"),
    submissionID = SubmissionID("none"),
    executionID = AppConfig.executionID
  )
}
