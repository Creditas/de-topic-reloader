package topic.reloader.lib

import java.nio.file.Path

package object model {

  case class AppName(value: String)
  case class ExecutionID(value: String)
  case class SubmissionID(value: String)
  case class Settings(
      appName: AppName,
      submissionID: SubmissionID,
      executionID: ExecutionID
  )
}
