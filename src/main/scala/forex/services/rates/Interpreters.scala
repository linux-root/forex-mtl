package forex.services.rates

import cats.Applicative
import cats.effect.Sync
import forex.services.rates.interpreters._
import forex.services.{OneFrameFetcher, RatesCache}

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_]: Sync](oneFrameFetcher: OneFrameFetcher[F],
                       ratesCache: RatesCache[F]
                      ): Algebra[F] = new RateQueryService[F](oneFrameFetcher, ratesCache)
}
