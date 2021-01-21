/*
 * Copyright 2021 HM Revenue & Customs
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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.logging.SessionId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.Future

class SessionCacheConnectorSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar {

  val defaultCache = mock[CacheMap]
  val defaultConnector = mock[CalculatorConnector]
  val defaultHttpClient = mock[DefaultHttpClient]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  implicit val hc = HeaderCarrier(sessionId = Some(SessionId("SessionID")))

  class Setup {
    val connector = new SessionCacheConnector(
      defaultHttpClient,
      defaultConnector,
      mockConfig
    )
  }

  "The session cache connector" should {
    "provide a valid interface for the cache method" when {
      "returning an exception" in new Setup {
        when(defaultHttpClient.PUT(any(), any(), any())
        (any(), any(), any(), any()))
          .thenReturn(Future.failed(new RuntimeException("testException")))

        val result = connector.saveFormData[String]("key", "default")

        intercept[RuntimeException](await(result) shouldBe "testException")
      }
      "returning a valid cachemap" in new Setup {
        when(defaultHttpClient.PUT[String, CacheMap](any(), any(), any())
          (any(), any(), any(), any()))
          .thenReturn(Future.successful(defaultCache))

        val result = connector.saveFormData[String]("key", "default")

        await(result) shouldBe defaultCache
      }
    }

    "provide a valid interface for the fetch and get entry method" when {
      "returning an exception" in new Setup {
        when(defaultHttpClient.GET(any())(any(), any(), any()))
          .thenReturn(Future.failed(new RuntimeException("testException")))

        val result = connector.fetchAndGetFormData[String]("key")

        intercept[RuntimeException](await(result))
      }
      "returning no data" in new Setup {
        when(defaultHttpClient.GET[CacheMap](any())(any(), any(), any()))
          .thenReturn(Future.failed(new NotFoundException("404")))

        val result = connector.fetchAndGetFormData[String]("key")

        await(result) shouldBe None
      }
      "returning valid data" in new Setup {
        val validCacheMap = CacheMap("SessionID", Map("key" -> Json.toJson("default")))

        when(defaultHttpClient.GET[CacheMap](any())
          (any(), any(), any()))
          .thenReturn(Future.successful(validCacheMap))

        val result = connector.fetchAndGetFormData[String]("key")

        await(result) shouldBe Some("default")
      }
    }

    "provided a valid interface for the remove method" when {
      "returning an exception" in new Setup {
        when(defaultHttpClient.DELETE[HttpResponse](any(), any())
          (any(), any(), any()))
          .thenReturn(Future.failed(new RuntimeException("testException")))

        val result = connector.clearKeystore

        intercept[RuntimeException](await(result))
      }

      "returning a valid response" in new Setup {
        val expected = HttpResponse(200)

        when(defaultHttpClient.DELETE[HttpResponse](any(), any())
          (any(), any(), any()))
          .thenReturn(Future.successful(expected))

        val result = connector.clearKeystore

        await(result) shouldBe expected
      }
    }
  }
}
