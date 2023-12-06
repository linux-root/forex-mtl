package forex.services.oneframes

import forex.domain.Rate
import forex.domain.Rate.Pair

trait Algebra[F[_]] {
  def fetchRates(pairs: List[Pair]): F[List[Rate]]
}
