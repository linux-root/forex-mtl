package services.rates.interpreters

import cats.Applicative
import cats.effect.{IO, Resource, Sync}
import cats.implicits.catsSyntaxApplicativeId
import forex.config.OneFrameServiceConfig
import forex.domain.Currency.{SGD, USD}
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.{OneFrameFetcherServices, RatesCache, RatesService, RatesServices}
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import util.OneFrameServiceContainer
import weaver.IOSuite

import scala.concurrent.ExecutionContext.global

object RateQueryServiceSpec extends IOSuite {
  override type Res = (OneFrameServiceConfig, Client[IO])
  override def sharedResource: Resource[IO, (OneFrameServiceConfig, Client[IO])] = {
    for {
      config <- OneFrameServiceContainer.resource.map(port =>
        OneFrameServiceConfig(
          url = Uri.unsafeFromString(s"http://localhost:${port.value}/rates"),
          token = "10dc303535874aeccc86a8251e6992f5"
        ))
      client <- BlazeClientBuilder[IO](global).resource
    } yield (config, client)
  }

  def dummyCache[F[_]: Applicative]: RatesCache[F] = new RatesCache[F]{
    override def set(values: List[(String, Rate)]): F[Unit] = Applicative[F].unit

    override def get(key: String): F[Option[Rate]] = Option.empty[Rate].pure[F]
  }
  def ratesService[F[_]: Sync](config: OneFrameServiceConfig, httpClient: Client[F]): RatesService[F] ={
    RatesServices.live[F](
      OneFrameFetcherServices.live[F](config, httpClient),
      dummyCache[F]
    )
  }

  test("Get the rate of the pair USD-SGD"){ case (config, client) =>
    val pair = Pair(USD, SGD)
    ratesService[IO](config, client).get(pair).map { result =>
      expect(result.isRight)
    }
  }

  test("Get the rate of invalid pair USD-ABC"){ case (config, client) =>
    val pair = Pair(USD, USD)
    ratesService[IO](config, client).get(pair).map {
      case Left(OneFrameLookupFailed(msg)) =>
        expect(msg.startsWith("Rate for given currency pair"))
      case _ => failure("Expected Left")
    }
  }

  test("Error when cannot handle one frame service's response") { case (config, client) =>
    val pair = Pair(USD, SGD)
    val configWithMalformedToken = config.copy(token = "badtoken")
    ratesService[IO](configWithMalformedToken, client).get(pair).attempt.map{result =>
      expect(result.isLeft)
    }
  }
}
