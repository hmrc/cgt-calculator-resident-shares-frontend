/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import config.CalculatorSessionCache
import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import play.api.libs.json.{Reads, Writes}

import scala.concurrent.{ExecutionContext, Future}

class SessionCacheConnectorSpec extends UnitSpec with MockitoSugar {

  "The session cache connector" should {
    val defaultCache = mock[CacheMap]
    val defaultFetch = Some("default")
    val defaultClear = mock[HttpResponse]

    implicit val hc = mock[HeaderCarrier]

    def testCache[T](saveData: Future[CacheMap], fetchData: Future[Option[T]], clearData: Future[HttpResponse]): SessionCacheConnector = {
      val mockCache = mock[SessionCache]

      when(mockCache.cache[T](
        ArgumentMatchers.eq("key"),
        ArgumentMatchers.any[T])(
        ArgumentMatchers.any(classOf[Writes[T]]),
        ArgumentMatchers.eq(hc),
        ArgumentMatchers.any[ExecutionContext])
      ).thenReturn(saveData)

      when(mockCache.fetchAndGetEntry[T](
        ArgumentMatchers.eq("key"))(
        ArgumentMatchers.eq(hc),
        ArgumentMatchers.any(classOf[Reads[T]]),
        ArgumentMatchers.any[ExecutionContext])
      ).thenReturn(fetchData)

      when(mockCache.remove()(ArgumentMatchers.eq(hc), ArgumentMatchers.any[ExecutionContext]))
        .thenReturn(clearData)

      new SessionCacheConnector {
        override val sessionCache: SessionCache = mockCache
      }
    }

    "have the correct SessionCache instance" in {
      SessionCacheConnector.sessionCache shouldBe CalculatorSessionCache
    }

    "provide a valid interface for the cache method" when {

      "returning an exception" in {
        val result = testCache(Future.failed(new Exception("testException")), Future.successful(defaultFetch), Future.successful(defaultClear))
          .saveFormData[String]("key", "default")

        the[Exception] thrownBy await(result) should have message "testException"
      }

      "returning a valid cachemap" in {
        val result = testCache(Future.successful(defaultCache), Future.successful(defaultFetch), Future.successful(defaultClear))
          .saveFormData[String]("key", "default")

        await(result) shouldBe defaultCache
      }
    }

    "provide a valid interface for the fetch and get entry method" when {

      "returning an exception" in {
        val result = testCache(Future.successful(defaultCache), Future.failed(new Exception("testException")), Future.successful(defaultClear))
          .fetchAndGetFormData[String]("key")

        the[Exception] thrownBy await(result) should have message "testException"
      }

      "returning no data" in {
        val result = testCache(Future.successful(defaultCache), Future.successful(None), Future.successful(defaultClear))
          .fetchAndGetFormData[String]("key")

        await(result) shouldBe None
      }

      "returning valid data" in {
        val result = testCache(Future.successful(defaultCache), Future.successful(defaultFetch), Future.successful(defaultClear))
          .fetchAndGetFormData[String]("key")

        await(result) shouldBe defaultFetch
      }
    }

    "provided a valid interface for the remove method" when {

      "returning an exception" in {
        val result = testCache(Future.successful(defaultCache), Future.successful(defaultFetch), Future.failed(new Exception("testException")))
          .clearKeystore

        the[Exception] thrownBy await(result) should have message "testException"
      }

      "returning a valid response" in {
        val result = testCache(Future.successful(defaultCache), Future.successful(defaultFetch), Future.successful(defaultClear))
          .clearKeystore

        await(result) shouldBe defaultClear
      }
    }
  }
}
