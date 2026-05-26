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

package connectors

import assets.ModelsAsset.*
import com.typesafe.config.ConfigFactory
import common.CommonPlaySpec
import models.resident.{ChargeableGainResultModel, TaxYearModel}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import util.WireMockMethods

import java.time.LocalDate

class CalculatorConnectorSpec extends CommonPlaySpec with MockitoSugar
  with WireMockSupport with GuiceOneAppPerSuite with WireMockMethods with ScalaCheckDrivenPropertyChecks {

  private val config = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |    capital-gains-calculator {
         |      host     = $wireMockHost
         |      port     = $wireMockPort
         |    }
         |  }
         |}
         |""".stripMargin
    )
  )

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(config).build()

  private val calculatorConnector = fakeApplication().injector.instanceOf[CalculatorConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Calling .getMinimumDate" should {
    val basePath = "/capital-gains-calculator/minimum-date"
    "return a DateTime which matches the returned LocalDate" in {
      val expectedDate = LocalDate.parse("2015-06-04")
      when(GET, basePath).thenReturn(OK, expectedDate)

      await(calculatorConnector.getMinimumDate()) shouldBe expectedDate
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.getMinimumDate())
        }
    }
  }

  "Calling .getFullAEA" should {
    val basePath = "/capital-gains-calculator/tax-rates-and-bands/max-full-aea"
    val queryParam = "?taxYear=0"
    "return a value corresponding to the year if it exists" in {
      val expectedResult = Some(BigDecimal(10000))
      when(GET, basePath).thenReturn(OK, expectedResult)

      await(calculatorConnector.getFullAEA(2017)) shouldBe expectedResult
    }

    "return None when taxYear = 0 or if does not exist" in forAll(Gen.oneOf(Seq(OK, BAD_REQUEST, NOT_FOUND))) {
      status =>
        when(GET, s"$basePath$queryParam").thenReturn(status, None)
        await(calculatorConnector.getFullAEA(0)) shouldBe None
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.getFullAEA(2017))
        }
    }
  }

  "Calling .getPA" should {
    val basePath = "/capital-gains-calculator/tax-rates-and-bands/max-pa"
    val queryParam = "?taxYear=2&isEligibleBlindPersonsAllowance=true&isEligibleMarriageAllowance=true"
    "return a value corresponding to the year if it exists without blind persons allowance" in {
      val expectedResult = Some(BigDecimal(10000))
      when(GET, basePath).thenReturn(OK, expectedResult)

      await(calculatorConnector.getPA(2017)) shouldBe expectedResult
    }

    "return None when taxYear = 2 and isEligibleBlindPersonsAllowance = true or if does not exist" in forAll(Gen.oneOf(Seq(OK, BAD_REQUEST, NOT_FOUND))) {
      status =>
        when(GET, s"$basePath$queryParam").thenReturn(status, None)
        await(calculatorConnector.getPA(2,true, true)) shouldBe None
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.getPA(2017))
        }
    }
  }

  "Calling .getTaxYear" should {
    val basePath = "/capital-gains-calculator/tax-year"
    val queryParam = "?date=3"
    "return a value corresponding to the year if it exists" in {
      val expectedResult = Some(TaxYearModel("2017", isValidYear = true, "2017"))
      when(GET, basePath).thenReturn(OK, expectedResult)

      await(calculatorConnector.getTaxYear("2017")) shouldBe expectedResult
    }

    "return None when taxYear = 3 or if does not exist" in forAll(Gen.oneOf(Seq(OK, BAD_REQUEST, NOT_FOUND))) {
      status =>
        when(GET, s"$basePath$queryParam").thenReturn(status, None)
        await(calculatorConnector.getTaxYear("3")) shouldBe None
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.getTaxYear("2017"))
        }
    }
  }

  "Calling .calculateRttShareGrossGain" should {
    val basePath = "/capital-gains-calculator/shares/calculate-total-gain"
    "return a value corresponding to the result" in {
      val expectedResult = BigDecimal(10000)
      when(GET, basePath).thenReturn(OK, expectedResult)

      await(calculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles)) shouldBe expectedResult
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles))
        }
    }
  }

  "Calling .calculateRttShareChargeableGain" should {
    val basePath = "/capital-gains-calculator/shares/calculate-chargeable-gain"
    val queryParam = "?disposalValue=200000.0&disposalDate=2016-10-10&broughtForwardLosses=10000.0&acquisitionValue=100000.0&disposalCosts=10000.0&annualExemptAmount=10000.0&acquisitionCosts=10000.0"
    val chargeableGainResultModel = ChargeableGainResultModel(7000, 0, 11100, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0)

    "return a value corresponding to the result if it exists" in {
      when(GET, basePath).thenReturn(OK, chargeableGainResultModel)

      await(calculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000)) shouldBe Some(chargeableGainResultModel)
    }

    "return a None if it doesn't exist " in forAll(Gen.oneOf(Seq(OK, BAD_REQUEST, NOT_FOUND))) {
      status =>
        when(GET, s"$basePath$queryParam").thenReturn(status, None)

        await(calculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
          deductionAnswersMostPossibles, 10000)) shouldBe None
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
            deductionAnswersMostPossibles, 10000))
        }
    }
  }

  "Calling .calculateRttShareTotalGainAndTax" should {
    val basePath = "/capital-gains-calculator/shares/calculate-resident-capital-gains-tax"
    val queryParam = "?disposalValue=200000.0&disposalDate=2016-10-10&broughtForwardLosses=10000.0&acquisitionValue=100000.0&disposalCosts=10000.0&annualExemptAmount=10000.0&personalAllowance=0.0&previousIncome=0.0&acquisitionCosts=10000.0"

    "return a value corresponding to the result if it exists" in {
      when(GET, basePath)
        .thenReturn(OK, totalGainAndTaxOwedModel)

      await(calculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers)) shouldBe Some(totalGainAndTaxOwedModel)
    }

    "return a None if it doesn't exist" in forAll(Gen.oneOf(Seq(OK, BAD_REQUEST, NOT_FOUND))) {
      status =>
        when(GET, s"$basePath$queryParam").thenReturn(status, None)
        await(calculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
          deductionAnswersMostPossibles, 10000, incomeAnswers)) shouldBe None
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
            deductionAnswersMostPossibles, 10000, incomeAnswers))
        }
    }
  }

  "Calling .getSharesTotalCosts" should {
    val basePath = "/capital-gains-calculator/shares/calculate-total-costs"

    "return a value corresponding to the result" in {
      when(GET,basePath).thenReturn(OK, BigDecimal(10000))
      await(calculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles)) shouldBe BigDecimal(10000)
    }

    "Throw UpstreamErrorResponse on errors" in forAll(
      Gen.oneOf(Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY, SERVICE_UNAVAILABLE))
    ) {
      status =>
        when(GET, basePath).thenReturn(status)

        assertThrows[UpstreamErrorResponse] {
          await(calculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles))
        }
    }
  }
}
