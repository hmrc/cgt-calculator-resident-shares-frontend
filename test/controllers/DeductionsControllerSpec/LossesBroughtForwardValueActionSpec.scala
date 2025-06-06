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

import assets.MessageLookup.{LossesBroughtForwardValue => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.DeductionsController
import controllers.helpers.FakeRequestHelper
import forms.LossesBroughtForwardValueForm
import models.resident._
import models.resident.shares._
import org.apache.pekko.actor.ActorSystem
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.deductions.{lossesBroughtForward, lossesBroughtForwardValue}

import scala.concurrent.Future

class LossesBroughtForwardValueActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar{

  implicit lazy val actorSystem: ActorSystem = ActorSystem()
  val mockCalcConnector = mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val gainModel = mock[GainAnswersModel]
  val summaryModel = mock[DeductionGainAnswersModel]
  val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val lossesBroughtForwardValueForm = fakeApplication.injector.instanceOf[LossesBroughtForwardValueForm]
  val lossesBroughtForwardView = fakeApplication.injector.instanceOf[lossesBroughtForward]
  val lossesBroughtForwardValueView = fakeApplication.injector.instanceOf[lossesBroughtForwardValue]

  "Calling .lossesBroughtForwardValue from the resident DeductionsController" when {


    def setGetTarget(getData: Option[LossesBroughtForwardValueModel],
                     disposalDateModel: DisposalDateModel,
                     taxYearModel: TaxYearModel): DeductionsController = {

      when(mockSessionCacheService.fetchAndGetFormData[LossesBroughtForwardValueModel](ArgumentMatchers.eq(keystoreKeys.lossesBroughtForwardValue))
        (using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(getData))

      when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel](ArgumentMatchers.eq(keystoreKeys.disposalDate))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(disposalDateModel)))

      when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(taxYearModel)))

      new DeductionsController(mockCalcConnector, mockSessionCacheService, mockMCC, lossesBroughtForwardValueForm, lossesBroughtForwardView, lossesBroughtForwardValueView)
    }

    "request has a valid session with no keystore data" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setGetTarget(None, disposalDateModel, taxYearModel)
      lazy val result = target.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015 to 2016")}" in {
        Jsoup.parse(contentAsString(result)).title shouldEqual messages.title("2015 to 2016")
      }

      s"have a back link to '${controllers.routes.DeductionsController.lossesBroughtForward.url}'" in {
        Jsoup.parse(contentAsString(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "request has a valid session with some keystore data" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2014)
      lazy val taxYearModel = TaxYearModel("2014/15", false, "2015/16")
      lazy val target = setGetTarget(Some(LossesBroughtForwardValueModel(BigDecimal(1000))), disposalDateModel, taxYearModel)
      lazy val result = target.lossesBroughtForwardValue(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with " in {
        contentType(result) shouldBe Some("text/html")
      }

      s"return a title of ${messages.title("2015 to 2015")}" in {
        Jsoup.parse(contentAsString(result)).title shouldEqual messages.title("2014 to 2015")
      }
    }

    "request has an invalid session" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2014)
      lazy val taxYearModel = TaxYearModel("2014/15", false, "2015/16")
      lazy val target = setGetTarget(Some(LossesBroughtForwardValueModel(BigDecimal(1000))), disposalDateModel, taxYearModel)
      lazy val result = target.lossesBroughtForwardValue(fakeRequest)
      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitLossesBroughtForwardValue from the resident DeductionsController" when {

    def setPostTarget(gainAnswers: GainAnswersModel,
                      chargeableGainAnswers: DeductionGainAnswersModel,
                      chargeableGain: ChargeableGainResultModel,
                      disposalDateModel: DisposalDateModel,
                      taxYearModel: TaxYearModel,
                      maxAnnualExemptAmount: Option[BigDecimal] = Some(BigDecimal(11100))
                     ): DeductionsController = {

      when(mockSessionCacheService.getShareGainAnswers(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(gainAnswers))

      when(mockSessionCacheService.getShareDeductionAnswers(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(chargeableGainAnswers))

      when(mockCalcConnector.calculateRttShareChargeableGain(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(chargeableGain)))

      when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel]
        (ArgumentMatchers.eq(keystoreKeys.disposalDate))(using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(disposalDateModel)))

      when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(Some(taxYearModel)))

      when(mockCalcConnector.getFullAEA(ArgumentMatchers.any())(using ArgumentMatchers.any()))
        .thenReturn(Future.successful(maxAnnualExemptAmount))

      when(mockSessionCacheService.saveFormData[LossesBroughtForwardValueModel](ArgumentMatchers.any(),
        ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful("" -> "")

        )

      new DeductionsController(mockCalcConnector, mockSessionCacheService, mockMCC, lossesBroughtForwardValueForm ,lossesBroughtForwardView, lossesBroughtForwardValueView)
    }

    "given a valid form" when {

      "the user has zero chargeable gain" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(gainModel, summaryModel,
          ChargeableGainResultModel(2000, 0, 0, 0, 2000, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.routes.ReviewAnswersController.reviewDeductionsAnswers.toString}'" in {
          redirectLocation(result).get shouldBe controllers.routes.ReviewAnswersController.reviewDeductionsAnswers.toString
        }
      }

      "the user has negative chargeable gain" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(gainModel, summaryModel,
          ChargeableGainResultModel(2000, -1000, 0, 0, 3000, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.routes.ReviewAnswersController.reviewDeductionsAnswers.toString}'" in {
          redirectLocation(result).get shouldBe controllers.routes.ReviewAnswersController.reviewDeductionsAnswers.toString
        }
      }

      "the user has positive chargeable gain of £1,000" should {
        lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val target = setPostTarget(gainModel, summaryModel,
          ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
          disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
        lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
        lazy val result = target.submitLossesBroughtForwardValue(request)

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        s"redirect to '${controllers.routes.SummaryController.summary.toString}'" in {
          redirectLocation(result).get shouldBe controllers.routes.IncomeController.currentIncome.toString
        }
      }
    }

    "given an invalid form" should {
      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setPostTarget(gainModel, summaryModel,
        ChargeableGainResultModel(1000, 1000, 0, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0),
        disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "")).withMethod("POST")
      lazy val result = target.submitLossesBroughtForwardValue(request)

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      s"return a title of ${messages.title("2015/16")}" in {
        Jsoup.parse(contentAsString(result)).title shouldEqual s"Error: ${messages.title("2015 to 2016")}"
      }
    }
  }
}
