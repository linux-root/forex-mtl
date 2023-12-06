package forex.services.rates.interpreters

import cats.effect.Sync
import cats.implicits.{ catsSyntaxApplicativeId, catsSyntaxEitherId, toFlatMapOps, toFunctorOps }
import forex.domain.Rate.Pair
import forex.domain.{ Currency, Rate }
import forex.services.rates.{ errors, Algebra }
import forex.services.{ OneFrameFetcher, RatesCache }

private[rates] final class RateQueryService[F[_]: Sync](oneFrameFetcher: OneFrameFetcher[F], cache: RatesCache[F])
    extends Algebra[F] {

  override def get(pair: Pair): F[errors.Error Either Rate] =
    for {
      rateOpt <- cache.get(pair.cacheKey)
      result <- rateOpt match {
                 case Some(cachedRate) =>
                   cachedRate.asRight.pure[F]
                 case None =>
                   for {
                     rates <- oneFrameFetcher.fetchRates(Currency.allPairs.toList)
                     _ <- cache.set(rates.map(rate => rate.pair.cacheKey -> rate))
                   } yield
                     rates.find(_.pair == pair) match {
                       case None =>
                         errors.Error
                           .OneFrameLookupFailed(s"Rate for given currency pair ${pair.from}-${pair.to} not found")
                           .asLeft
                       case Some(rate) => rate.asRight
                     }
               }
    } yield result
}
