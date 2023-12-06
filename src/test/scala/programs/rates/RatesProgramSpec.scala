package programs.rates

import cats.Applicative
import cats.effect.IO
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import forex.domain.Currency.{SGD, USD}
import forex.domain.Rate.Pair
import forex.domain.{Price, Rate, Timestamp}
import forex.programs.RatesProgram
import forex.programs.rates.Protocol.GetRatesRequest
import forex.services.RatesService
import weaver.SimpleIOSuite

object RatesProgramSpec extends SimpleIOSuite{
  def alwaysSuccessfulRateQueryService[F[_]: Applicative](returnedRate: Rate): RatesService[F] =
    (_: Pair) => returnedRate.asRight.pure[F]

  test("successful rate query"){
    val request = GetRatesRequest(USD, SGD)
    val mockingRateValue = Rate(Pair(request.from, request.to), Price(1.13), Timestamp.now)
    RatesProgram[IO](alwaysSuccessfulRateQueryService(mockingRateValue)).get(request).map{ result =>
      expect(result.isRight)
    }
  }

  test("Return error in program when GetRateRequest has invalid pair such as USD-USD"){
    val request = GetRatesRequest(USD, USD)
    val mockingRateValue = Rate(Pair(request.from, request.to), Price(1.13), Timestamp.now)
    RatesProgram[IO](alwaysSuccessfulRateQueryService(mockingRateValue)).get(request).map { result =>
      expect(result.isLeft)
    }
  }

}
