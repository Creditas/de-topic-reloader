package topic.reloader

import topic.reloader.lib.model.ExecutionID

object AppConfig {
  val executionID: ExecutionID = ExecutionID(java.util.UUID.randomUUID().toString)
}
