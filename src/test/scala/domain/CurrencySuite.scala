package domain

import forex.domain.Currency
import org.scalatest.funsuite.AnyFunSuite

class CurrencySuite extends AnyFunSuite {

  test("total of currencies"){
    assert(Currency.allCurrencies.size == 9)
  }

  test("total pairs of currencies"){
    assert(Currency.allPairs.size == 64)
  }

}
