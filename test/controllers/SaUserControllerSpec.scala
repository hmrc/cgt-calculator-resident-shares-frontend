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

package controllers

import assets.{MessageLookup, ModelsAsset}
import com.codahale.metrics.SharedMetricRegistries
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import models.resident.shares.GainAnswersModel
import models.resident.{ChargeableGainResultModel, TotalGainAndTaxOwedModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import views.html.calculation.whatNext.saUser

import scala.concurrent.Future

class SaUserControllerSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionId")))
  val mockConnector: CalculatorConnector = mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  implicit val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockApplication: Application = fakeApplication
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val saUserView: saUser = fakeApplication.injector.instanceOf[saUser]

  def setupController(gainAnswersModel: GainAnswersModel, chargeableGain: BigDecimal, totalGain: BigDecimal,
                      taxOwed: BigDecimal): SaUserController = {
    SharedMetricRegistries.clear()

    when(mockSessionCacheService.getShareGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswersModel))

    when(mockSessionCacheService.getShareDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(ModelsAsset.deductionAnswersLeastPossibles))

    when(mockSessionCacheService.getShareIncomeAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(ModelsAsset.incomeAnswers))

    when(mockConnector.calculateRttShareGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(totalGain))

    when(mockConnector.calculateRttShareChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(ChargeableGainResultModel(totalGain, chargeableGain, 0, 0, 0, 0, 0, None, None, 0, 0))))

    when(mockConnector.calculateRttShareTotalGainAndTax(ArgumentMatchers.any(), ArgumentMatchers.any(),
      ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(TotalGainAndTaxOwedModel(totalGain, chargeableGain, 11000, 0, 5000, 10000, 5, None, None, None, None, 0, 0))))

    when(mockConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11000))))

    when(mockConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(ModelsAsset.taxYearModel)))

    new SaUserController(mockConnector, mockSessionCacheService, mockMCC, saUserView)
  }

  "Calling .saUser" when {

    "no session is provided" should {
      lazy val controller = setupController(ModelsAsset.gainLargeDisposalValue, 0, -10000, 0)
      lazy val result = controller.submitSaUser(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the missing session page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "a session is provided" should {
      lazy val controller = setupController(ModelsAsset.gainLargeDisposalValue, 0, -10000, 0)
      lazy val result = controller.submitSaUser(fakeRequestWithSession)

      "return a status of 400 when there is no form body in the request" in {
        status(result) shouldBe 400
      }

      "return a status of 200 when form body is correct" in {
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(("isInSa", "Yes")).withMethod("POST"))

        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/what-next-sa-no-gain-over-limit")
      }

      "load the saUser page with an error" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe MessageLookup.SaUser.errorTitle
      }
    }
  }

  "Calling .submitSaUser" when {

    "no session is provided" should {
      lazy val controller = setupController(ModelsAsset.gainLargeDisposalValue, 0, -10000, 0)
      lazy val result = controller.submitSaUser(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the missing session page" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "a non-sa user is submitted" when {
      val form = "isInSa" -> "No"

      "there is no tax liability" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 0, -10000, 0)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa loss what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextNonSaController.whatNextNonSaLoss.url)
        }
      }

      "there is a tax liability" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 10000, 5000, 2000)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa gain what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextNonSaController.whatNextNonSaGain.url)
        }
      }
    }

    "a sa user is submitted" when {
      val form = "isInSa" -> "Yes"

      "there is a tax liability" should {
        lazy val controller = setupController(ModelsAsset.gainAnswersMostPossibles, 10000, 5000, 2000)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa gain what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextSAController.whatNextSAGain.url)
        }
      }

      "there is no tax liability and a disposal value less than 4*AEA" should {
        lazy val controller = setupController(ModelsAsset.gainLowDisposalValue, 0, -10000, 0)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa loss what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextSAController.whatNextSANoGain.url)
        }
      }

      "there is no tax liability and a disposal value greater than 4*AEA" should {
        lazy val controller = setupController(ModelsAsset.gainLargeDisposalValue, 0, -10000, 0)
        lazy val result = controller.submitSaUser(fakeRequestToPOSTWithSession(form).withMethod("POST"))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the nonSa loss with value greater than what next page" in {
          redirectLocation(result) shouldBe Some(controllers.routes.WhatNextSAController.whatNextSAOverFourTimesAEA.url)
        }
      }
    }
  }
}
