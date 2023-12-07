package forex.services.caches.interpreters

import cats.Monad
import cats.effect.Clock
import cats.effect.concurrent.Ref
import cats.implicits.{toFlatMapOps, toFunctorOps}
import forex.domain.Rate
import forex.services.caches.Algebra

import java.util.concurrent.TimeUnit

final class CachedRates[F[_]: Clock: Monad](storageRef: Ref[F, Map[String, Rate]]) extends Algebra[F, String, Rate] {
  override def set(values: List[(String, Rate)]): F[Unit] = {
    storageRef.set(values.toMap)
  }

  override def get(key: String): F[Option[Rate]] = {
    for {
      currentEpochSeconds <- Clock[F].realTime(TimeUnit.SECONDS)
      rateOpt <- storageRef.get.map{ storage =>
         storage.get(key).filterNot(_.isExpired(currentEpochSeconds))
      }
    } yield rateOpt
  }

}
