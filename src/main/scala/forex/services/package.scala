package forex

import forex.domain.Rate

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type RatesCache[F[_]] = caches.Algebra[F, String, Rate]
  final val RatesCacheServices = caches.Interpreters

  type OneFrameFetcher[F[_]] = oneframes.Algebra[F]
  final val OneFrameFetcherServices = oneframes.Interpreters
}
