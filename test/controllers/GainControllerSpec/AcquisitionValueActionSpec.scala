/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.GainControllerSpec

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.MessageLookup.{SharesAcquisitionValue => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import config.ApplicationConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.{CgtLanguageController, GainController}
import controllers.helpers.FakeRequestHelper
import models.resident.AcquisitionValueModel
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AcquisitionValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  lazy val materializer = mock[Materializer]

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget(getData: Option[AcquisitionValueModel]): GainController = {

    implicit val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
    implicit val mockApplication = fakeApplication
    val mockCalcConnector = mock[CalculatorConnector]
    val mockSessionCacheConnector = mock[SessionCacheConnector]
    val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
    val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]

    when(mockSessionCacheConnector.fetchAndGetFormData[AcquisitionValueModel]
      (ArgumentMatchers.eq(keystoreKeys.acquisitionValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[AcquisitionValueModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC)
  }

  "Calling .acquisitionValue from the GainCalculationController with session" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.acquisitionValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Value view" in {
        Jsoup.parse(bodyOf(result)(materializer)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(AcquisitionValueModel(1000)))
      lazy val result = target.acquisitionValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Acquisition Value view" in {
        Jsoup.parse(bodyOf(result)(materializer)).title shouldBe messages.title
      }
    }
  }

  "Calling .acquisitionValue from the GainCalculationController with no session" should {
    lazy val target = setupTarget(None)
    lazy val result = target.acquisitionValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }

  "Calling .submitAcquisitionValue from the GainCalculationController" when {

    "a valid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAcquisitionValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition costs page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/acquisition-costs")
      }
    }

    "an invalid form is submitted" should {
      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitAcquisitionValue(request)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the acquisition value page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
