package forex.domain

import java.time.{ Instant, OffsetDateTime, ZoneId }
import scala.util.Try

case class Timestamp(value: OffsetDateTime) extends AnyVal

object Timestamp {
  def now: Timestamp =
    Timestamp(OffsetDateTime.now)

  def fromString(iso8601: String): Either[String, Timestamp] =
    Try {
      Right(Timestamp(Instant.parse(iso8601).atZone(ZoneId.systemDefault()).toOffsetDateTime))
    }.getOrElse(Left("Cannot parse ISO 8601 datetime format"))
}
