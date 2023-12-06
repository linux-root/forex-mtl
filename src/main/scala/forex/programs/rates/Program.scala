package forex.programs.rates

import cats.Applicative
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import forex.domain._
import forex.programs.rates.errors._
import forex.services.RatesService

class Program[F[_]: Applicative](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    if (request.isValid) {
      EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError(_)).value
    } else {
      Applicative[F].pure(
        Error.InvalidRateRequest(request).asLeft
      )
    }
  }

}

object Program {

  def apply[F[_]: Applicative](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
