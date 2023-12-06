package util

import cats.effect.{ Resource, Sync }
import cats.implicits.toFunctorOps
import com.dimafeng.testcontainers.GenericContainer

object OneFrameServiceContainer {
  case class Port(value: Int)
  def resource[F[_]: Sync]: Resource[F, Port] = {
    val servicePort = 8080
    Resource
      .make {
        val container = GenericContainer(
          "paidyinc/one-frame",
          exposedPorts = List(servicePort),
        )
        Sync[F].pure(container.start()).as(container -> Port(container.container.getMappedPort(servicePort)))
      } { case (container, _) => Sync[F].delay(container.stop()) }
      .map { case (_, port) => port }
  }
}
