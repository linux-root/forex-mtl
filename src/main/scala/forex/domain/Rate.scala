package forex.domain

import cats.implicits.toShow
import io.circe.{Decoder, DecodingFailure, HCursor}

import java.time.Instant

case class Rate(
    pair: Rate.Pair,
    price: Price,
    timestamp: Timestamp
) {
  def isExpired(currentEpochSeconds: Long): Boolean = {
    val timeToLiveSeconds = 300L // 5 Minutes
    val expiringMoment = timestamp.value.toInstant.plusSeconds(timeToLiveSeconds)
    expiringMoment.isBefore(Instant.ofEpochSecond(currentEpochSeconds))
  }
}

object Rate {

  implicit val rateDecoder: Decoder[Rate] = (c: HCursor) => {
    for {
      from <- c.downField("from").as[String].flatMap(x => Currency.fromString(x))
      to <- c.downField("to").as[String].flatMap(x => Currency.fromString(x))
      price <- c.downField("price").as[BigDecimal].map(Price(_))
      time <- c.downField("time_stamp").as[String].flatMap(t => Timestamp.fromString(t).left.map(msg => DecodingFailure(msg, Nil)))
    } yield {
      new Rate(Pair(from, to), price, time)
    }
  }
  final case class Pair(
      from: Currency,
      to: Currency
  ) {
    def asHttpQueryParam: String = s"pair=${from.show}${to.show}"

    def cacheKey=s"${from.show}${to.show}"

  }
}
