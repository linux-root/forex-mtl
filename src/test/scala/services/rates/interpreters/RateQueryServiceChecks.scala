package services.rates.interpreters

import cats.effect.concurrent.Ref
import cats.effect.{Clock, IO, Sync}
import cats.implicits.{catsStdShowForList, catsSyntaxApplicativeId, catsSyntaxApply, toFlatMapOps, toFunctorOps, toTraverseOps}
import cats.{MonadThrow, Show}
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services._
import org.scalacheck.Gen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

import java.time.{Instant, OffsetDateTime, ZoneId}
import java.util.concurrent.TimeUnit
import scala.util.Random

object RateQueryServiceChecks extends SimpleIOSuite with Checkers {
  private val REQUEST_LIMIT = 1000
  private val _24_HOURS_IN_SECONDS = 3600 * 24
  private val TODAY = 1701795600L
  case class QueryAction(pair: Pair, secondsToTheNextAction: Long)

  object TestClock {
    def create[G[_]: MonadThrow](epochTimestampRef: Ref[G, Long]): Clock[G] = {
      new Clock[G] {
        override def realTime(unit: TimeUnit): G[Long] = {
          if (unit == TimeUnit.SECONDS) {
           epochTimestampRef.get
          } else {
            MonadThrow[G].raiseError(new NotImplementedError(s"time unit $unit is not implemented"))
          }
        }

        override def monotonic(unit: TimeUnit): G[Long] = {
          MonadThrow[G].raiseError(new NotImplementedError("Not support this method"))
        }
      }
    }

    def adjust[G[_]](epochTimestampRef: Ref[G, Long])(increasedSeconds: Long): G[Unit] = {
      epochTimestampRef.update { now =>
        now + increasedSeconds
      }
    }

  }

  case class Counter(remainingRequests: Int, day: Int){
    def resetCount: Counter = this.copy(remainingRequests = REQUEST_LIMIT, day = this.day + 1)

    def takeRequest: Counter = this.copy(remainingRequests = this.remainingRequests - 1)
  }
  def oneFrameFetcher[G[_] : MonadThrow: Clock](counterRef: Ref[G, Counter]): OneFrameFetcher[G] = new OneFrameFetcher[G] {
    override def fetchRates(pairs: List[Rate.Pair]): G[List[Rate]] = {
      for {
        now <- Clock[G].realTime(TimeUnit.SECONDS)
        counter <- counterRef.updateAndGet{counter  =>
          val resetCount = now - TODAY >= (counter.day + 1) *_24_HOURS_IN_SECONDS
          if (resetCount) counter.resetCount else counter
        }
        randomRates <- if (counter.remainingRequests == 0){
          MonadThrow[G].raiseError(new IllegalStateException("out of requests for a day"))
        } else {
          counterRef.updateAndGet(_.takeRequest).flatMap {updatedCounter =>
            println(s"requests count=${updatedCounter.remainingRequests}").pure[G] *> Clock[G].realTime(TimeUnit.SECONDS).map(now =>
              randomRates(pairs, now)
            )
          }
        }
      } yield randomRates
    }
  }

  def randomRates(pairs: List[Pair], now: Long): List[Rate] = {
      println("request complete")
      pairs.map(pair => Rate(pair, Price(Random.nextDouble()), Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochSecond(now), ZoneId.systemDefault()))))
  }

  def rateQueryService[G[_]](counterRef: Ref[G, Counter], ratesCaches: RatesCache[G])(implicit C: Clock[G], S: Sync[G]): RatesService[G] = {
    RatesServices.live(oneFrameFetcher(counterRef), ratesCaches)
  }

  def rateCacheService[G[_]](implicit C: Clock[G], S: Sync[G]): G[RatesCache[G]] = RatesCacheServices.live[G]

  private val pairGen: Gen[Pair] = Gen.choose(0, Currency.allPairs.size - 1).map {index => Currency.allPairs.toList(index)}

  private val queryActionGen: Gen[QueryAction] = for {
    pair <- pairGen
    secondsToTheNextAction <- Gen.chooseNum[Long](1, 301)
  } yield QueryAction(pair, secondsToTheNextAction)

  private implicit val qaShow: Show[QueryAction] = Show.show(_.pair.cacheKey)

  test("10000 requests per day") {
    forall(Gen.buildableOfN[List[QueryAction], QueryAction](10000, queryActionGen)) { actions =>
      for {
        counterRef <- Ref[IO].of(Counter(REQUEST_LIMIT, 0))
        epochTimestampRef <- Ref[IO].of(TODAY)
        testClock = TestClock.create[IO](epochTimestampRef)
        rateCaches <- rateCacheService[IO](testClock, Sync[IO])
        service = rateQueryService(counterRef, rateCaches)(testClock, Sync[IO])
        _ <- actions.traverse(action => service.get(action.pair) *> TestClock.adjust[IO](epochTimestampRef)(action.secondsToTheNextAction))
      } yield expect(true)
    }
  }
}
