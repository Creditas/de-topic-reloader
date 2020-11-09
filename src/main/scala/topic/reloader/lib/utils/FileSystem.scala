package topic.reloader.lib.utils

import java.io.File
import java.nio.file.{Files, Path, Paths}

import io.circe.Json
import io.circe.parser.parse
import topic.reloader.lib.model.Settings
import topic.reloader.lib.utils.Exception.LoadJsonException

import scala.util.Try

object FileSystem {

  def tmpDir(implicit settings: Settings): Path =
    Paths.get(
      System.getProperty("java.io.tmpdir", "/tmp"),
      settings.appName.value,
      settings.executionID.value
    )

  def ensureDir(base: Path, path: String*): Either[Throwable, Path] =
    Try {
      val dirPath = Paths.get(base.toString, path: _*)
      Files.createDirectories(dirPath)
      dirPath
    }.toEither

  def loadResourceAsString(resourceName: String): Either[Throwable, String] =
    Try {
      scala.io.Source.fromResource(resourceName).mkString
    }.toEither

  def copyResourceTo(resourcePath: String, baseDir: Path, subDirs: String*): Either[Throwable, Long] =
    Try {
      val resourceStream = getClass.getResourceAsStream(File.separator + resourcePath)
      val basename = resourcePath.split(File.separator).last
      val destDir = Files.createDirectories(Paths.get(baseDir.toString, subDirs.toList: _*))
      val destFile = Paths.get(destDir.toString, basename)
      Files.copy(resourceStream, destFile)
    }.toEither

  def loadJsonMapFrom(key: String, dir: Path): Either[Throwable, Map[String, Json]] =
    Try {
      val file = dir.toFile
      val rawDataList = file
        .listFiles()
        .toList
        .filter(_.isFile)
        .filter(_.toString.endsWith(".json"))
        .map(scala.io.Source.fromFile(_))
        .map(_.mkString)
        .map(_.trim)

      val eitherList = rawDataList.map(parse)

      val lefts = eitherList.collect { case Left(error) => error }
      val rights =
        if (lefts.nonEmpty) throw LoadJsonException(lefts, rawDataList)
        else eitherList.collect { case Right(json) => json }

      rights.foldLeft(Map.empty[String, Json]) {
        case (acc, json) =>
          json.hcursor.downField(key).as[String] match {
            case Left(e) => throw e
            case Right(name) => acc + (name -> json)
          }
      }
    }.toEither
}
