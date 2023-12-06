package forex.services.oneframes.interpreters

import cats.effect.Sync
import forex.config.OneFrameServiceConfig
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.oneframes.Algebra
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.{ Headers, Request, Uri }
private[oneframes] final class OneFrameFetcher[F[_]: Sync](httpClient: Client[F], config: OneFrameServiceConfig)
    extends Algebra[F] {
  override def fetchRates(pairs: List[Pair]): F[List[Rate]] = {
    val queryParams: String = pairs.map(_.asHttpQueryParam).mkString("&")
    val request = Request[F](
      uri = Uri.unsafeFromString(s"${config.url.toString}?$queryParams"),
      headers = Headers("token" -> config.token)
    )
    httpClient.fetchAs[List[Rate]](request)
  }

}
