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

import assets.MessageLookup
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.apache.pekko.util.Timeout
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.calculation.checkYourAnswers.checkYourAnswers

import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class ReviewAnswersControllerSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  val mockSessionCacheService = mock[SessionCacheService]
  val mockConnector = mock[CalculatorConnector]
  implicit val appConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val appApplication: Application = fakeApplication
  val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  val date: LocalDate = LocalDate.of(2016, 5, 8)
  val totalLossModel: GainAnswersModel = GainAnswersModel(date, soldForLessThanWorth = false, Some(100000), None, 1000,
    ownerBeforeLegislationStart = false, None, Some(false), None, Some(150000), 1500)
  val totalGainModel: GainAnswersModel = GainAnswersModel(date, soldForLessThanWorth = false, Some(100000), None, 1000,
    ownerBeforeLegislationStart = false, None, Some(false), None, Some(50000), 1500)
  val totalGainWithinAEAModel: GainAnswersModel = GainAnswersModel(date, soldForLessThanWorth = false, Some(100000), None, 1000,
    ownerBeforeLegislationStart = false, None, Some(false), None, Some(90000), 1500)
  val allDeductionsModel: DeductionGainAnswersModel = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(true)),
    Some(LossesBroughtForwardValueModel(50000)))
  val noDeductionsModel: DeductionGainAnswersModel = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)), None)
  val incomeAnswersModel: IncomeAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(25000)), Some(PersonalAllowanceModel(11000)))
  implicit val timeout: Timeout = Timeout.apply(Duration.create(20, "seconds"))
  implicit val hc: HeaderCarrier = new HeaderCarrier()
  val checkYourAnswersView = fakeApplication.injector.instanceOf[checkYourAnswers]

  def setupController(gainResponse: GainAnswersModel,
                      deductionsResponse: DeductionGainAnswersModel,
                      taxYearModel: Option[TaxYearModel] = None): ReviewAnswersController = {

    when(mockSessionCacheService.getShareGainAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(gainResponse))

    when(mockSessionCacheService.getShareDeductionAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(deductionsResponse))

    when(mockConnector.getTaxYear(ArgumentMatchers.any())(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockSessionCacheService.getShareIncomeAnswers(using ArgumentMatchers.any()))
      .thenReturn(Future.successful(incomeAnswersModel))

    new ReviewAnswersController(mockConnector, mockSessionCacheService, mockMCC, checkYourAnswersView)
  }

  "Calling .reviewGainAnswers" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(totalLossModel, allDeductionsModel)
      lazy val result = controller.reviewGainAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(totalLossModel, allDeductionsModel)
      lazy val result = controller.reviewGainAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(contentAsString(result)).title() shouldBe MessageLookup.Resident.Shares.ReviewAnswers.title
      }

      "have a back link to the acquisition costs page" in {
        Jsoup.parse(contentAsString(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .reviewDeductionsAnswers" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(
        totalGainModel,
        allDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewDeductionsAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session and brought forward losses" should {
      lazy val controller = setupController(
        totalLossModel,
        allDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewDeductionsAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(contentAsString(result)).title() shouldBe MessageLookup.Resident.Shares.ReviewAnswers.title
      }

      "have a back link to the brought forward losses value page" in {
        Jsoup.parse(contentAsString(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }

    "provided with a valid session and no brought forward losses" should {
      lazy val controller = setupController(
        totalGainWithinAEAModel,
        noDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewDeductionsAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(contentAsString(result)).title() shouldBe MessageLookup.Resident.Shares.ReviewAnswers.title
      }

      "have a back link to the brought forward losses page" in {
        Jsoup.parse(contentAsString(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .reviewFinalAnswers" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(
        totalGainModel,
        noDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewFinalAnswers(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(
        totalLossModel,
        noDeductionsModel,
        taxYearModel = Some(TaxYearModel("2016/17", isValidYear = true, "2016/17")))
      lazy val result = controller.reviewFinalAnswers(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the Review Answers page" in {
        Jsoup.parse(contentAsString(result)).title() shouldBe MessageLookup.Resident.Shares.ReviewAnswers.title
      }

      "have a back link to the personal allowance page" in {
        Jsoup.parse(contentAsString(result)).select(".govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }
}
