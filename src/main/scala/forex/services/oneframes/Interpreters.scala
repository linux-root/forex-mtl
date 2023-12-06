package forex.services.oneframes

import cats.effect.Sync
import forex.config.OneFrameServiceConfig
import forex.services.oneframes.interpreters.OneFrameFetcher
import org.http4s.client.Client

object Interpreters {
  def live[F[_]: Sync](config: OneFrameServiceConfig, httpClient: Client[F]): Algebra[F] =
    new OneFrameFetcher[F](httpClient, config)
}
