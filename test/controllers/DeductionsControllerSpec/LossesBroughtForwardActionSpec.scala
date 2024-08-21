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

package controllers.DeductionsControllerSpec


import org.apache.pekko.actor.ActorSystem
import assets.MessageLookup.{LossesBroughtForward => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.DeductionsController
import controllers.helpers.FakeRequestHelper
import forms.LossesBroughtForwardValueForm
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.deductions.{lossesBroughtForward, lossesBroughtForwardValue}

import scala.concurrent.Future

class LossesBroughtForwardActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem: ActorSystem = ActorSystem()

  val gainModel = mock[GainAnswersModel]
  val summaryModel = mock[DeductionGainAnswersModel]
  val chargeableGainModel = mock[ChargeableGainResultModel]
  val mockCalcConnector = mock[CalculatorConnector]
  val mockSessionCacheService = mock[SessionCacheService]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val lossesBroughForwardValueForm = fakeApplication.injector.instanceOf[LossesBroughtForwardValueForm]
  val lossesBroughtForwardView = fakeApplication.injector.instanceOf[lossesBroughtForward]
  val lossesBroughtForwardValueView = fakeApplication.injector.instanceOf[lossesBroughtForwardValue]

  def setupTarget(lossesBroughtForwardData: Option[LossesBroughtForwardModel],
                  gainAnswers: GainAnswersModel,
                  chargeableGainAnswers: DeductionGainAnswersModel,
                  chargeableGain: ChargeableGainResultModel,
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel], maxAnnualExemptAmount: Option[BigDecimal] = Some(BigDecimal(11100))): DeductionsController = {

    when(mockSessionCacheService.fetchAndGetFormData[LossesBroughtForwardModel](ArgumentMatchers.eq(keystoreKeys.lossesBroughtForward))
      (ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(lossesBroughtForwardData))

    when(mockSessionCacheService.getShareGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainAnswers))

    when(mockSessionCacheService.getShareDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalcConnector.calculateRttShareChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(chargeableGain)))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel]
      (ArgumentMatchers.eq(keystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(taxYear)

    when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(maxAnnualExemptAmount))

    when(mockSessionCacheService.saveFormData[LossesBroughtForwardValueModel](ArgumentMatchers.any(),
      ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> "")

      )

    new DeductionsController(mockCalcConnector, mockSessionCacheService, mockMCC, lossesBroughForwardValueForm ,lossesBroughtForwardView, lossesBroughtForwardValueView)
  }

  "Calling .lossesBroughtForward from the resident DeductionsController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None, gainModel, summaryModel,
        chargeableGainModel, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015 to 2016")}" in {
        doc.title shouldEqual messages.title("2015 to 2016")
      }
    }

    "request has no session" should {

      lazy val target = setupTarget(None, gainModel, summaryModel, chargeableGainModel, None, None, None)
      lazy val result = target.lossesBroughtForward(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }
    }

    "other properties have been selected but no allowable losses" should {

      lazy val target = setupTarget(None, gainModel, summaryModel, chargeableGainModel,
        Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.lossesBroughtForward(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "have a back link to acquisition costs" in {
        doc.select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "Calling .submitLossesBroughtForward from the DeductionsController" when {

      "a valid form 'No' and chargeable gain is £1000" should {

        lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel, summaryModel,
          ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0),
            None, None, 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
        lazy val request = fakeRequestToPOSTWithSession(("option", "No")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForward(request)


        "return a 303" in {
          status(result) shouldBe 303
        }

        "redirect to the current income page" in {
          redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/current-income")
        }
      }

      "a valid form 'No' and chargeable gain is zero" should {

        lazy val target = setupTarget(Some(LossesBroughtForwardModel(false)), gainModel, summaryModel,
          ChargeableGainResultModel(1000, 0, 0, 0, 1000, BigDecimal(0), BigDecimal(0),
            None, None, 0, 0), Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
        lazy val request = fakeRequestToPOSTWithSession(("option", "No")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForward(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "redirect to the summary page" in {
          redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/review-your-answers-deduction")
        }
      }

      "a valid form 'Yes'" should {

        lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), gainModel, summaryModel,
          ChargeableGainResultModel(0, 0, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
        lazy val request = fakeRequestToPOSTWithSession(("option", "Yes")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForward(request)

        "return a 303" in {
          status(result) shouldBe 303
        }

        "redirect to the losses brought forward value page" in {
          redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/losses-brought-forward-value")
        }
      }


      "an invalid form is submitted" should {

        lazy val target = setupTarget(Some(LossesBroughtForwardModel(true)), gainModel, summaryModel, chargeableGainModel,
          Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
        lazy val request = fakeRequestToPOSTWithSession(("option", "")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForward(request)

        "return a 400" in {
          status(result) shouldBe 400
        }

        "render the brought forward losses page" in {
          Jsoup.parse(bodyOf(result)).title() shouldEqual s"Error: ${messages.title("2015 to 2016")}"
        }
      }
    }
  }
}
