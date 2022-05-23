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

package connectors

import java.time.LocalDate
import java.util.UUID

import assets.ModelsAsset._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import models.resident.{ChargeableGainResultModel, TaxYearModel, TotalGainAndTaxOwedModel}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, SessionId}
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import scala.concurrent.{ExecutionContext, Future}

class CalculatorConnectorSpec extends CommonPlaySpec with WithCommonFakeApplication with MockitoSugar {

  val mockHttp = mock[DefaultHttpClient]
  val mockSessionCacheConnector = mock[SessionCacheConnector]
  val sessionId = UUID.randomUUID.toString
  val homeLink = controllers.routes.GainController.disposalDate().url
  val mockConfig = mock[ApplicationConfig]

  object TargetCalculatorConnector extends CalculatorConnector(mockHttp, mockConfig) {
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "Calling .getMinimumDate" should {
    def mockDate(result: Future[DateTime]): OngoingStubbing[Future[DateTime]] =
      when(mockHttp.GET[DateTime](ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(result)

    "return a DateTime which matches the returned LocalDate" in {
      mockDate(Future.successful(DateTime.parse("2015-06-04")))
      await(TargetCalculatorConnector.getMinimumDate()) shouldBe LocalDate.parse("2015-06-04")
    }

    "return a failure if one occurs" in {
      mockDate(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getMinimumDate()) should have message "error message"
    }
  }

  "Calling .getFullAEA" should {
    def mockAEA(result: Future[Option[BigDecimal]]): OngoingStubbing[Future[Option[BigDecimal]]] = {
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[Option[BigDecimal]]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the year if it exists" in {
      mockAEA(Future.successful(Some(BigDecimal(10000))))
      await(TargetCalculatorConnector.getFullAEA(2017)) shouldBe Some(BigDecimal(10000))
    }

    "return a none value if it is returned" in {
      mockAEA(Future.successful(None))
      await(TargetCalculatorConnector.getFullAEA(2017)) shouldBe None
    }

    "return an exception if one occurs" in {
      mockAEA(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getFullAEA(2017)) should have message "error message"
    }
  }

  "Calling .getPartialAEA" should {
    def mockAEA(result: Future[Option[BigDecimal]]): OngoingStubbing[Future[Option[BigDecimal]]] = {
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[Option[BigDecimal]]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the year if it exists" in {
      mockAEA(Future.successful(Some(BigDecimal(10000))))
      await(TargetCalculatorConnector.getPartialAEA(2017)) shouldBe Some(BigDecimal(10000))
    }

    "return a none value if it is returned" in {
      mockAEA(Future.successful(None))
      await(TargetCalculatorConnector.getPartialAEA(2017)) shouldBe None
    }

    "return an exception if one occurs" in {
      mockAEA(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getPartialAEA(2017)) should have message "error message"
    }
  }

  "Calling .getPA" should {
    def mockPA(result: Future[Option[BigDecimal]]): OngoingStubbing[Future[Option[BigDecimal]]] = {
      when(mockHttp.GET[Option[BigDecimal]](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[Option[BigDecimal]]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the year if it exists without blind persons allowance" in {
      mockPA(Future.successful(Some(BigDecimal(10000))))
      await(TargetCalculatorConnector.getPA(2017, isEligibleBlindPersonsAllowance = false)) shouldBe Some(BigDecimal(10000))
    }

    "return a none value if it is returned with blind persons allowance" in {
      mockPA(Future.successful(None))
      await(TargetCalculatorConnector.getPA(2017, isEligibleBlindPersonsAllowance = true)) shouldBe None
    }

    "return an exception if one occurs" in {
      mockPA(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getPA(2017)) should have message "error message"
    }
  }

  "Calling .getTaxYear" should {
    def mockTaxYear(result: Future[Option[TaxYearModel]]): OngoingStubbing[Future[Option[TaxYearModel]]] = {
      when(mockHttp.GET[Option[TaxYearModel]](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[Option[TaxYearModel]]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the year if it exists" in {
      mockTaxYear(Future.successful(Some(TaxYearModel("2017", isValidYear = true, "2017"))))
      await(TargetCalculatorConnector.getTaxYear("2017")) shouldBe Some(TaxYearModel("2017", isValidYear = true, "2017"))
    }

    "return a none value if it is returned" in {
      mockTaxYear(Future.successful(None))
      await(TargetCalculatorConnector.getTaxYear("2017")) shouldBe None
    }

    "return an exception if one occurs" in {
      mockTaxYear(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getTaxYear("2017")) should have message "error message"
    }
  }

  "Calling .calculateRttShareGrossGain" should {
    def mockCalculateRttShareGrossGain(result: Future[BigDecimal]): OngoingStubbing[Future[BigDecimal]] = {
      when(mockHttp.GET[BigDecimal](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[BigDecimal]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the result" in {
      mockCalculateRttShareGrossGain(Future.successful(BigDecimal(10000)))
      await(TargetCalculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles)) shouldBe BigDecimal(10000)
    }

    "return an exception if one occurs" in {
      mockCalculateRttShareGrossGain(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles)) should have message "error message"
    }

    "return an ApplicationException if a NoSuchElementException is returned" in {
      mockCalculateRttShareGrossGain(Future.failed(new NoSuchElementException("error message")))
      val result = TargetCalculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles)
      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }

  "Calling .calculateRttShareChargeableGain" should {
    val mockChargeableGainResultModel = mock[ChargeableGainResultModel]
    def mockCalculateRttShareChargeableGain(result: Future[Option[ChargeableGainResultModel]]): OngoingStubbing[Future[Option[ChargeableGainResultModel]]] = {
      when(mockHttp.GET[Option[ChargeableGainResultModel]](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[Option[ChargeableGainResultModel]]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the result if it exists" in {
      mockCalculateRttShareChargeableGain(Future.successful(Some(mockChargeableGainResultModel)))
      await(TargetCalculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000)) shouldBe Some(mockChargeableGainResultModel)
    }

    "return a None if it doesn't exist" in {
      mockCalculateRttShareChargeableGain(Future.successful(None))
      await(TargetCalculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000)) shouldBe None
    }

    "return an exception if one occurs" in {
      mockCalculateRttShareChargeableGain(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000)) should have message "error message"
    }

    "return an ApplicationException if a NoSuchElementException is returned" in {
      mockCalculateRttShareChargeableGain(Future.failed(new NoSuchElementException("error message")))
      val result = TargetCalculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles, deductionAnswersMostPossibles, 10000)
      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }

  "Calling .calculateRttShareTotalGainAndTax" should {
    val mockTotalGainAndTaxOwedModel = mock[TotalGainAndTaxOwedModel]
    def mockCalculateRttShareTotalGainAndTax(result: Future[Option[TotalGainAndTaxOwedModel]]): OngoingStubbing[Future[Option[TotalGainAndTaxOwedModel]]] = {
      when(mockHttp.GET[Option[TotalGainAndTaxOwedModel]](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[Option[TotalGainAndTaxOwedModel]]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the result if it exists" in {
      mockCalculateRttShareTotalGainAndTax(Future.successful(Some(mockTotalGainAndTaxOwedModel)))
      await(TargetCalculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers)) shouldBe Some(mockTotalGainAndTaxOwedModel)
    }

    "return a None if it doesn't exist" in {
      mockCalculateRttShareTotalGainAndTax(Future.successful(None))
      await(TargetCalculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers)) shouldBe None
    }

    "return an exception if one occurs" in {
      mockCalculateRttShareTotalGainAndTax(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers)) should have message "error message"
    }

    "return an ApplicationException if a NoSuchElementException is returned" in {
      mockCalculateRttShareTotalGainAndTax(Future.failed(new NoSuchElementException("error message")))
      val result = TargetCalculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles, deductionAnswersMostPossibles, 10000, incomeAnswers)
      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }

  "Calling .getSharesTotalCosts" should {
    def mockGetSharesTotalCosts(result: Future[BigDecimal]): OngoingStubbing[Future[BigDecimal]] = {
      when(mockHttp.GET[BigDecimal](ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(classOf[HttpReads[BigDecimal]]),
        ArgumentMatchers.any(classOf[HeaderCarrier]), ArgumentMatchers.any(classOf[ExecutionContext])))
        .thenReturn(result)
    }

    "return a value corresponding to the result" in {
      mockGetSharesTotalCosts(Future.successful(BigDecimal(10000)))
      await(TargetCalculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles)) shouldBe BigDecimal(10000)
    }

    "return an exception if one occurs" in {
      mockGetSharesTotalCosts(Future.failed(new Exception("error message")))
      the[Exception] thrownBy await(TargetCalculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles)) should have message "error message"
    }

    "return an ApplicationException if a NoSuchElementException is returned" in {
      mockGetSharesTotalCosts(Future.failed(new NoSuchElementException("error message")))
      val result = TargetCalculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles)
      the[ApplicationException] thrownBy await(result) shouldBe ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "error message")
    }
  }
}
