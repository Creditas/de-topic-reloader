package topic.reloader.lib.utils

import io.circe.ParsingFailure

object Exception {
  case class LoadJsonException(errors: Seq[ParsingFailure], input: Seq[String])
      extends RuntimeException(s"something wrong with json load, errors: ${errors.size}", errors.head)
}
