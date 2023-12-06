package forex

import cats.effect._
import forex.config._
import forex.services.{RatesCache, RatesCacheServices}
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    BlazeClientBuilder[IO](global).resource.evalMap(httpClient =>
      for {
        cache <- RatesCacheServices.live[IO]
        app <- new Application[IO].stream(executionContext, httpClient, cache).compile.drain.as(ExitCode.Success)
      } yield app
    ).use(_ => IO.never)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext, httpClient: Client[F], ratesCacheServices: RatesCache[F]): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config, httpClient, ratesCacheServices)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
