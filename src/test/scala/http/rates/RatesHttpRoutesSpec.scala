package http.rates

import cats.Applicative
import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import forex.domain.Currency.{SGD, USD}
import forex.domain.Rate.Pair
import forex.domain.{Price, Rate, Timestamp}
import forex.http.rates.RatesHttpRoutes
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol, errors}
import org.http4s._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.GET
import org.http4s.implicits.http4sLiteralsSyntax
import weaver.SimpleIOSuite

object RatesHttpRoutesSpec extends SimpleIOSuite {

  def alwaysSuccessfulRateProgram[F[_]: Applicative]: RatesProgram[F] = new RatesProgram[F] {
    override def get(request: Protocol.GetRatesRequest): F[Either[errors.Error, Rate]] =
      Rate(Pair(USD, SGD), Price(1.62), Timestamp.now).asRight.pure[F]
  }

  test("get rate successfully") {
    val req    = GET(uri"/rates?from=USD&to=SGD")
    val routes = new RatesHttpRoutes[IO](alwaysSuccessfulRateProgram).routes
    routes.run(req).value.map(response => expect(response.exists(_.status == Status.Ok)))
  }


  test("Rate not found") {
    val req = GET(uri"/rates?from=USD&to=ABC")
    val routes = new RatesHttpRoutes[IO](alwaysSuccessfulRateProgram).routes
    routes.run(req).value.map(response =>
      expect(response.isEmpty)
    )
  }
}
