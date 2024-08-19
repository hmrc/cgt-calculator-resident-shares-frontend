/*
 * Copyright 2024 HM Revenue & Customs
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

import org.apache.pekko.actor.ActorSystem
import assets.MessageLookup.Resident.Shares.{SellForLess => messages}
import common.KeystoreKeys.{ResidentShareKeys => keyStoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import connectors.CalculatorConnector
import controllers.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.{DisposalDateModel, SellForLessModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.gain._
import views.html.calculation.outsideTaxYear

import scala.concurrent.Future

class SellForLessActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  def setupTarget(getData: Option[SellForLessModel], knownTaxYear: Boolean = true): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]
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

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("year", knownTaxYear, "year"))))

    when(mockSessionCacheService.fetchAndGetFormData[SellForLessModel](ArgumentMatchers.eq(keyStoreKeys.sellForLess))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keyStoreKeys.disposalDate))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(DisposalDateModel(1, 1, 1))))

    when(mockSessionCacheService.saveFormData[SellForLessModel](ArgumentMatchers.any(), ArgumentMatchers.any())
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    new GainController(mockCalcConnector, mockSessionCacheService, mockMCC,
      acquisitionCostsView, acquisitionValueView, disposalCostsView, disposalDateView, disposalValueView,
      didYouInheritThemView, ownerBeforeLegislationStartView, sellForLessView, valueBeforeLegislationStartView,
      worthWhenInheritedView, worthWhenSoldForLessView, outsideTaxYearView)
  }

  "Calling .sellForLess from the resident shares GainController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.newTitle
      }
    }

    "in a known tax year" should {
      lazy val target = setupTarget(None)
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "have a back link to the disposal date page" in {
        Jsoup.parse(bodyOf(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "outside a known tax year" should {
      lazy val target = setupTarget(None, knownTaxYear = false)
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "have a back link to the outside tax years page" in {
        Jsoup.parse(bodyOf(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(SellForLessModel(true)))
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.sellForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitSellForLess from the resident shares GainCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("sellForLess", "Yes")).withMethod("POST")
      lazy val result = target.submitSellForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the worth when sold page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/worth-when-sold-for-less")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("sellForLess", "No")).withMethod("POST")
      lazy val result = target.submitSellForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the disposal value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/disposal-value")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("sellForLess", "")).withMethod("POST")
      lazy val result = target.submitSellForLess(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Sell For Less page" in {
        doc.title() shouldEqual s"Error: ${messages.newTitle}"
      }
    }
  }
}
