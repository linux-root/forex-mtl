package forex

import forex.domain.Rate

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type RatesCache[F[_]] = caches.Algebra[F, String, Rate]
  final val RatesCache = caches.Interpreters
}
