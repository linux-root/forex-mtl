package forex.domain

import cats.Show
import forex.domain.Rate.Pair
import io.circe.DecodingFailure

import scala.util.Try

sealed trait Currency

object Currency {
  private[domain] def generatePairs(currencies: Set[Currency]): Set[Pair] = {
    if (currencies.size < 2) {
      Set.empty
    } else {
      for {
        a <- currencies
        b <- currencies.tail if a != b
      } yield Pair(a, b)
    }
  }


  val allCurrencies: Set[Currency] = Set(AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD)

  val allPairs: Set[Pair] = generatePairs(allCurrencies)
  case object AUD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object EUR extends Currency
  case object GBP extends Currency
  case object NZD extends Currency
  case object JPY extends Currency
  case object SGD extends Currency
  case object USD extends Currency

  implicit val show: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
  }

  def unsafeFromString(s: String): Currency = s.toUpperCase match {
    case "AUD" => AUD
    case "CAD" => CAD
    case "CHF" => CHF
    case "EUR" => EUR
    case "GBP" => GBP
    case "NZD" => NZD
    case "JPY" => JPY
    case "SGD" => SGD
    case "USD" => USD
  }

  def fromString(s: String): Either[DecodingFailure, Currency] = Try{
    Right(unsafeFromString(s))
  }.getOrElse(Left(DecodingFailure(s"$s is not a valid Currency", Nil)))

}
