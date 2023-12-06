package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.config.OneFrameServiceConfig
import forex.services.RatesCache
import forex.services.rates.interpreters._
import org.http4s.client.Client

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Sync](config: OneFrameServiceConfig,
                       httpClient: Client[F],
                       ratesCache: RatesCache[F]
                      ): Algebra[F] = new OneFrameLive[F](config, httpClient, ratesCache)
}
