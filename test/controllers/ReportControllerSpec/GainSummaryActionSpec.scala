/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.CalculatorConnector
import controllers.ReportController
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.shares.GainAnswersModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class GainSummaryActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget
  (
    yourAnswersSummaryModel: GainAnswersModel,
    grossGain: BigDecimal,
    taxYearModel: Option[TaxYearModel]
  ): ReportController = {

    lazy val mockCalculatorConnector = mock[CalculatorConnector]
    val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

    when(mockSessionCacheService.getShareGainAnswers(ArgumentMatchers.any()))
      .thenReturn(Future.successful(yourAnswersSummaryModel))

    when(mockCalculatorConnector.calculateRttShareGrossGain(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(grossGain))

    when(mockCalculatorConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(taxYearModel))

    when(mockCalculatorConnector.getSharesTotalCosts(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(BigDecimal(1000)))

    new ReportController {
      override val calcConnector: CalculatorConnector = mockCalculatorConnector

      override def host(implicit request: RequestHeader): String = "http://localhost:9977/"

      override val sessionCacheService: SessionCacheService = mockSessionCacheService
    }
  }

  "Calling .gainSummaryReport from the ReportController" when {

    "a negative total gain is returned" should {
      lazy val yourAnswersSummaryModel = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2018),
        soldForLessThanWorth = false,
        disposalValue = Some(3000),
        worthWhenSoldForLess = None,
        disposalCosts = 10,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(5000),
        acquisitionCosts = 5
      )
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2015/2016", true, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "should return the pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "should have a filename of 'Summary.pdf'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }

    "a zero total gain is returned with an invalid tax year" should {
      lazy val yourAnswersSummaryModel = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2018),
        soldForLessThanWorth = false,
        disposalValue = Some(3000),
        worthWhenSoldForLess = None,
        disposalCosts = 10,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(5000),
        acquisitionCosts = 5
      )
      lazy val target = setupTarget(
        yourAnswersSummaryModel,
        -6000,
        taxYearModel = Some(TaxYearModel("2013/2014", false, "2015/16"))
      )
      lazy val result = target.gainSummaryReport(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return a pdf" in {
        contentType(result) shouldBe Some("application/pdf")
      }

      "should return the pdf as an attachment" in {
        header("Content-Disposition", result).get should include("attachment")
      }

      "should have a filename of 'Summary'" in {
        header("Content-Disposition", result).get should include(s"""filename="${messages.title}.pdf"""")
      }
    }
  }
}
