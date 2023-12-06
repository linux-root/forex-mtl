package forex.config

import org.http4s.Uri
import pureconfig.ConfigReader
import pureconfig.error.CannotConvert

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    oneFrameService: OneFrameServiceConfig
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

case class OneFrameServiceConfig(
  url: Uri,
  token: String
)

object ApplicationConfig {
  implicit val uriConfigReader: ConfigReader[Uri] = ConfigReader.fromString(s =>
    Uri.fromString(s).toTry.toEither.left.map(e => CannotConvert("URL", "Http4s URI", e.getMessage))
  )
}