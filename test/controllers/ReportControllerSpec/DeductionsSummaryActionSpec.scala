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

package controllers.ReportControllerSpec

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.{CgtLanguageController, ReportController}
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.{MessagesControllerComponents, RequestHeader}
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DeductionsSummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget
  (
    yourAnswersSummaryModel: GainAnswersModel,
    grossGain: BigDecimal,
    chargeableGainAnswers: DeductionGainAnswersModel,
    chargeableGainResultModel: Option[ChargeableGainResultModel] = None,
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]
    val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
    implicit val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
    implicit val mockLang = mock[Lang]
    val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]

    when(mockSessionCacheService.getShareGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttShareGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockSessionCacheService.getShareDeductionAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(chargeableGainAnswers))

    when(mockCalculatorConnector.calculateRttShareChargeableGain
    (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(chargeableGainResultModel)

    when(mockCalculatorConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getFullAEA(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(BigDecimal(11100))))

    when(mockCalculatorConnector.getSharesTotalCosts(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(1000)))

    new ReportController(mockCalculatorConnector, mockSessionCacheService, mockMCC) {
      override def host(implicit request: RequestHeader): String = "http://localhost:9977/"
    }
  }

  "Calling .deductionReport from the ReportController" when {

    "a 0 gain is returned" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2018),
        soldForLessThanWorth = false,
        disposalValue = Some(200000),
        worthWhenSoldForLess = None,
        disposalCosts = 10000,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(100000),
        acquisitionCosts = 10000
      )
      lazy val deductionAnswers = DeductionGainAnswersModel(
        Some(LossesBroughtForwardModel(true)),
        Some(LossesBroughtForwardValueModel(10000)))
      lazy val results = ChargeableGainResultModel(BigDecimal(50000),
        BigDecimal(-11000),
        BigDecimal(0),
        BigDecimal(11000),
        BigDecimal(71000),
        BigDecimal(0),
        BigDecimal(0),
        None,
        None,
        0,
        0
      )

      lazy val target = setupTarget(
        gainAnswers,
        0,
        deductionAnswers,
        Some(results),
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.deductionsReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "a carried forward loss is returned with an invalid tax year" should {
      lazy val yourAnswersSummaryModel = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2018),
        soldForLessThanWorth = false,
        disposalValue = Some(30000),
        worthWhenSoldForLess = None,
        disposalCosts = 0,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(10000),
        acquisitionCosts = 0
      )
      lazy val chargeableGainAnswers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)), None)
      lazy val chargeableGainResultModel = ChargeableGainResultModel(20000, 20000, 11100, 0, 11100, BigDecimal(0), BigDecimal(0), None, None, 0, 0)
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -10000,
        chargeableGainAnswers,
        Some(chargeableGainResultModel),
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.deductionsReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "return a pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "return the pdf with a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }
  }
}
