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

package controllers.GainControllerSpec

import akka.actor.ActorSystem
import akka.stream.Materializer
import assets.MessageLookup.Resident.Shares.{DidYouInheritThem => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import common.KeystoreKeys.{ResidentShareKeys => keyStoreKeys}
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.shares.gain.DidYouInheritThemModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.calculation.gain.{acquisitionCosts, acquisitionValue, didYouInheritThem, disposalCosts, disposalDate, disposalValue, ownerBeforeLegislationStart, sellForLess, valueBeforeLegislationStart, worthWhenInherited, worthWhenSoldForLess}
import views.html.calculation.outsideTaxYear

import scala.concurrent.Future

class InheritedSharesActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  lazy val materializer = mock[Materializer]

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget(getData: Option[DidYouInheritThemModel]): GainController= {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockSessionCacheConnector = mock[SessionCacheConnector]
    val mockSessionCacheService = mock[SessionCacheService]
    val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    val acquisitionCostsView = fakeApplication.injector.instanceOf[acquisitionCosts]
    val acquisitionValueView = fakeApplication.injector.instanceOf[acquisitionValue]
    val disposalCostsView = fakeApplication.injector.instanceOf[disposalCosts]
    val disposalDateView = fakeApplication.injector.instanceOf[disposalDate]
    val disposalValueView = fakeApplication.injector.instanceOf[disposalValue]
    val didYouInheritThemView = fakeApplication.injector.instanceOf[didYouInheritThem]
    val ownerBeforeLegislationStartView = fakeApplication.injector.instanceOf[ownerBeforeLegislationStart]
    val sellForLessView = fakeApplication.injector.instanceOf[sellForLess]
    val valueBeforeLegislationStartView = fakeApplication.injector.instanceOf[valueBeforeLegislationStart]
    val worthWhenInheritedView = fakeApplication.injector.instanceOf[worthWhenInherited]
    val worthWhenSoldForLessView = fakeApplication.injector.instanceOf[worthWhenSoldForLess]
    val outsideTaxYearView = fakeApplication.injector.instanceOf[outsideTaxYear]

    when(mockSessionCacheConnector.fetchAndGetFormData[DidYouInheritThemModel]
      (ArgumentMatchers.eq(keyStoreKeys.didYouInheritThem))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[DidYouInheritThemModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC,
      acquisitionCostsView, acquisitionValueView, disposalCostsView, disposalDateView, disposalValueView,
      didYouInheritThemView, ownerBeforeLegislationStartView, sellForLessView, valueBeforeLegislationStartView,
      worthWhenInheritedView, worthWhenSoldForLessView, outsideTaxYearView)
  }

  "Calling .didYouInheritThem from the resident GainController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.didYouInheritThem(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.question}" in {
        Jsoup.parse(bodyOf(result)(materializer)).title shouldEqual messages.question
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(DidYouInheritThemModel(true)))
      lazy val result = target.didYouInheritThem(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }
    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.didYouInheritThem(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitInheritedShares from the resident GainCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("wereInherited", "Yes"))
      lazy val result = target.submitDidYouInheritThem(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the worth when sold page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/worth-when-inherited")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("wereInherited", "No"))
      lazy val result = target.submitDidYouInheritThem(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/acquisition-value")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("wereInherited", ""))
      lazy val result = target.submitDidYouInheritThem(request)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Inherited Shares page" in {
        doc.title() shouldEqual messages.question
      }
    }
  }

}
