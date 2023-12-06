package forex.services.rates.interpreters

import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, toFlatMapOps, toFunctorOps}
import forex.config.OneFrameServiceConfig
import forex.domain.Rate.Pair
import forex.domain.{Currency, Rate}
import forex.services.RatesCache
import forex.services.rates.{Algebra, errors}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.{Headers, Request, Uri}

final class OneFrameLive[F[_]: Sync](config: OneFrameServiceConfig,
                                                 httpClient: Client[F],
                                     cache: RatesCache[F]) extends Algebra[F] {

  override def get(pair: Pair): F[errors.Error Either Rate] =
    for {
      rateOpt <- cache.get(pair.cacheKey)
      result <- rateOpt match {
                 case Some(cachedRate) =>
                   cachedRate.asRight.pure[F]
                 case None =>
                   for {
                     rates <- fetchAllRates
                     _ <- cache.set(rates.map(rate => rate.pair.cacheKey -> rate))
                   } yield
                     rates.find(_.pair == pair) match {
                       case None       => errors.Error.OneFrameLookupFailed(s"Rate for given currency pair ${pair.from}-${pair.to} not found").asLeft
                       case Some(rate) => rate.asRight
                     }
               }
    } yield result

  private def fetchAllRates: F[List[Rate]] = {
    val queryParams: String = Currency.allPairs.map(_.asHttpQueryParam).mkString("&")
    val request = Request[F](
      uri = Uri.unsafeFromString(s"${config.url.toString}?$queryParams"),
      headers = Headers("token" -> config.token)
    )
    httpClient.fetchAs[List[Rate]](request)
  }

}
