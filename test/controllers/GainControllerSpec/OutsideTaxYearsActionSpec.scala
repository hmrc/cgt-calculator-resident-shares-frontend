/*
 * Copyright 2019 HM Revenue & Customs
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
import akka.stream.{ActorMaterializer, Materializer}
import assets.MessageLookup.{OutsideTaxYears => messages}
import config.ApplicationConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.{CgtLanguageController, GainController}
import controllers.helpers.FakeRequestHelper
import models.resident.{DisposalDateModel, TaxYearModel}
import org.joda.time.DateTime
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.HttpReads
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class OutsideTaxYearsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {
  lazy val materializer = mock[Materializer]

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget(disposalDateModel: Option[DisposalDateModel], taxYearModel: Option[TaxYearModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockSessionCacheConnector = mock[SessionCacheConnector]
    val mockSessionCacheService = mock[SessionCacheService]
    implicit val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
    val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]
    val mockLangContrl = new CgtLanguageController(mockMCC, mockConfig)

    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(disposalDateModel)

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(taxYearModel)

    new GainController(mockCalcConnector, mockSessionCacheService, mockSessionCacheConnector, mockMCC, mockLangContrl)
  }

  "Calling .outsideTaxYears from the resident/shares GainCalculationController" when {

    "there is a valid session" should {
      lazy val target = setupTarget(Some(DisposalDateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", false, "2016/17")))
      lazy val result = target.outsideTaxYears(fakeRequestWithSession)

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title}" in {
        Jsoup.parse(bodyOf(result)(materializer)).title shouldBe messages.title
      }

      s"have a back link to '${controllers.routes.GainController.disposalDate().url}'" in {
        Jsoup.parse(bodyOf(result)(materializer)).getElementById("back-link").attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }

      s"have a continue link to '${controllers.routes.GainController.sellForLess().url}'" in {
        Jsoup.parse(bodyOf(result)(materializer)).getElementById("continue-button").attr("href") shouldBe controllers.routes.GainController.sellForLess().url
      }
    }

    "there is no valid session" should {
      lazy val target = setupTarget(Some(DisposalDateModel(10, 10, 2018)), Some(TaxYearModel("2018/19", false, "2016/17")))
      lazy val result = target.outsideTaxYears(fakeRequest)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }
}
