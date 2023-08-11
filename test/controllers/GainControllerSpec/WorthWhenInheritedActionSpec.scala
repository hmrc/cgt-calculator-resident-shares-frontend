/*
 * Copyright 2023 HM Revenue & Customs
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
import assets.MessageLookup.Resident.Shares.{WorthWhenInherited => Messages}
import common.KeystoreKeys.{ResidentShareKeys => keyStoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.WorthWhenInheritedModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.cache.client.CacheMap
import views.html.calculation.gain._
import views.html.calculation.outsideTaxYear

import scala.concurrent.Future

class WorthWhenInheritedActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget(getData: Option[WorthWhenInheritedModel]): GainController= {

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

    when(mockSessionCacheConnector.fetchAndGetFormData[WorthWhenInheritedModel]
      (ArgumentMatchers.eq(keyStoreKeys.worthWhenInherited))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[WorthWhenInheritedModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC,
      acquisitionCostsView, acquisitionValueView, disposalCostsView, disposalDateView, disposalValueView,
      didYouInheritThemView, ownerBeforeLegislationStartView, sellForLessView, valueBeforeLegislationStartView,
      worthWhenInheritedView, worthWhenSoldForLessView, outsideTaxYearView)
  }

  "Calling .worthWhenInherited action" when {

    "request has a valid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenInherited(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${Messages.title}" in {
        doc.title shouldEqual Messages.title
      }

      "have a back link to how-became-owner" in {
        doc.body().select("a#back-link").attr("href") shouldBe "/calculate-your-capital-gains/resident/shares/did-you-inherit-them"
      }

      "have a home link to 'homeLink'" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldBe "/calculate-your-capital-gains/resident/shares/disposal-date"
      }

      "have a method to POST" in {
        doc.select("form").attr("method") shouldBe "POST"
      }

      "have an action to worth-when-inherited" in {
        doc.select("form").attr("action") shouldBe "/calculate-your-capital-gains/resident/shares/worth-when-inherited"
      }
    }

    "request has a valid session with existing data" should {
      lazy val target = setupTarget(Some(WorthWhenInheritedModel(100)))
      lazy val result = target.worthWhenInherited(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${Messages.title}" in {
        Jsoup.parse(bodyOf(result)).title shouldEqual Messages.title
      }
    }

    "request has an invalid session" should {
      lazy val target = setupTarget(None)
      lazy val result = target.worthWhenInherited(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitSellOrGiveAway action" when {

    "a valid form with the answer '100' is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitWorthWhenInherited(fakeRequestToPOSTWithSession(("amount", "100")).withMethod("POST"))

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the acquisition-costs page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/acquisition-costs")
      }
    }

    "an invalid form with no answer is submitted" should {
      lazy val target = setupTarget(None)
      lazy val result = target.submitWorthWhenInherited(fakeRequestToPOSTWithSession(("amount", "")))
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "return to the page" in {
        doc.title shouldEqual s"Error: ${Messages.title}"
      }

      "raise an error on the page" in {
        doc.body.select("#amount-error").size shouldBe 1
      }
    }
  }

}
