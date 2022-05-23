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
import assets.MessageLookup.Resident.Shares.{ValueBeforeLegislationStart => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import config.ApplicationConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.{GainController, routes}
import controllers.helpers.FakeRequestHelper
import models.resident.shares.gain.ValueBeforeLegislationStartModel
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

class ValueBeforeLegislationStartActionSpec extends CommonPlaySpec with WithCommonFakeApplication
  with FakeRequestHelper with MockitoSugar {
  lazy val materializer = mock[Materializer]

  implicit lazy val actorSystem = ActorSystem()
  val mockCalcConnector = mock[CalculatorConnector]
  val mockSessionCacheConnector = mock[SessionCacheConnector]
  val mockSessionCacheService = mock[SessionCacheService]
  implicit val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockApplication = fakeApplication
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

  def setupTarget(getData: Option[ValueBeforeLegislationStartModel]): GainController = {

    when(mockSessionCacheConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel]
      (ArgumentMatchers.eq(keystoreKeys.valueBeforeLegislationStart))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[ValueBeforeLegislationStartModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC,
      acquisitionCostsView, acquisitionValueView, disposalCostsView, disposalDateView, disposalValueView,
      didYouInheritThemView, ownerBeforeLegislationStartView, sellForLessView, valueBeforeLegislationStartView,
      worthWhenInheritedView, worthWhenSoldForLessView, outsideTaxYearView)
  }

  "Calling .valueBeforeLegislationStart from the GainCalculationController" when {
    "there is no keystore data" should {
      lazy val target = setupTarget(None)
      lazy val result = target.valueBeforeLegislationStart(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }

    "there is keystore data" should {
      lazy val target = setupTarget(Some(ValueBeforeLegislationStartModel(100)))
      lazy val result = target.valueBeforeLegislationStart(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldEqual 200
      }
    }
  }

  "Calling .valueBeforeLegislationStart from the GainCalculationController" should {

    lazy val target = setupTarget(None)
    lazy val result = target.valueBeforeLegislationStart(fakeRequestWithSession)

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    s"return some html with title of ${messages.question}" in {
      contentType(result) shouldBe Some("text/html")
      Jsoup.parse(bodyOf(result)(materializer)).select("h1").text shouldEqual messages.question
    }
  }

  "Calling .valueBeforeLegislationStart from the GainCalculationController with no session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.valueBeforeLegislationStart(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }
  }

  "Calling .submitValueBeforeLegislationStart with a valid request" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
    lazy val result = target.submitValueBeforeLegislationStart(request)

    "return a status of 303" in {
      status(result) shouldEqual 303
    }

    "re-direct to the acquisition Costs page when supplied with a valid form" in {
      redirectLocation(result) shouldBe Some(routes.GainController.acquisitionCosts().url)
    }
  }

  "Calling .submitValueBeforeLegislationStart with an invalid request" should {
    lazy val target = setupTarget(None)
    lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
    lazy val result = target.submitValueBeforeLegislationStart(request)

    "render with a status of 400" in {
      status(result) shouldEqual 400
    }

    "render the valueBeforeLegislationStart view" in {
      Jsoup.parse(bodyOf(result)(materializer)).title() shouldEqual messages.question
    }
  }
}
