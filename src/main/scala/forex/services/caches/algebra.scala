package forex.services.caches

trait Algebra[F[_], K, V] {
  def set(values: List[(K,V)]): F[Unit]
  def get(key: K): F[Option[V]]
}
