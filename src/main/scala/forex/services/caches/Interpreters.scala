package forex.services.caches

import cats.effect.concurrent.Ref
import cats.effect.{Clock, Sync}
import cats.implicits.toFunctorOps
import forex.domain.Rate
import forex.services.caches.interpreters.CachedRates

object Interpreters {
  def live[F[_]: Sync: Clock]: F[Algebra[F, String, Rate]] = {
    Ref.of[F, Map[String, Rate]](Map.empty[String, Rate]).map{storage =>
      new CachedRates[F](storage)
    }
  }
}
