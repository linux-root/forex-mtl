package domain

import forex.domain.Currency.{ SGD, USD }
import forex.domain.Rate
import forex.domain.Rate.Pair
import org.scalatest.funsuite.AnyFunSuite
import io.circe.parser.decode

/***
  * When format of Third-API One-frame service changes,
  * update new sample data to make the test failed then update the codec
  */
class RateCodecSpec extends AnyFunSuite {

  test("parse json body of One-frame service response") {
    val jsonSample = """{
             "from": "USD",
             "to": "SGD",
             "bid": 0.25615267061020375,
             "ask": 0.5946850847825655,
             "price": 0.425418877696384625,
             "time_stamp": "2023-12-05T22:07:46.924Z"
         }"""

    val rate = decode[Rate](jsonSample)
    assert(rate.exists(_.pair == Pair(USD, SGD)))
  }

}
