/*
 * Copyright 2022 HM Revenue & Customs
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
import assets.MessageLookup.Resident.Shares.{DisposalValue => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.GainController
import controllers.helpers.FakeRequestHelper
import models.resident.DisposalValueModel
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

class DisposalValueActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {
  lazy val materializer = mock[Materializer]

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget(getData: Option[DisposalValueModel]): GainController = {

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

    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalValueModel]
      (ArgumentMatchers.eq(keystoreKeys.disposalValue))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[DisposalValueModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC,
      acquisitionCostsView, acquisitionValueView, disposalCostsView, disposalDateView, disposalValueView,
      didYouInheritThemView, ownerBeforeLegislationStartView, sellForLessView, valueBeforeLegislationStartView,
      worthWhenInheritedView, worthWhenSoldForLessView, outsideTaxYearView)
  }

  "Calling .disposalValue from the GainCalculationController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.disposalValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(DisposalValueModel(100)))
      lazy val result = target.disposalValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }
  }

  "Calling .disposalValue from the GainCalculationController" should {

    lazy val target = setupTarget(None)
    lazy val result = target.disposalValue(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with title of ${messages.question}" in {
      contentType(result) shouldBe Some("text/html")
      Jsoup.parse(bodyOf(result)(materializer)).select("h1").text shouldEqual messages.question
    }
  }

  "Calling .disposalValue from the GainCalculationController with no session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.disposalValue(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }
  }

  "Calling .submitDisposalValue from the GainController" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", "100")).withMethod("POST")
    lazy val result = target.submitDisposalValue(request)

    "re-direct to the disposal Costs page when supplied with a valid form" in {
      status(result) shouldEqual 303
      redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/disposal-costs")
    }
  }

  "Calling .submitDisposalValue from the GainController" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", "")).withMethod("POST")
    lazy val result = target.submitDisposalValue(request)

    "render the disposal value page when supplied with an invalid form" in {
      status(result) shouldEqual 400
      Jsoup.parse(bodyOf(result)(materializer)).title() shouldEqual messages.question
    }
  }
}
