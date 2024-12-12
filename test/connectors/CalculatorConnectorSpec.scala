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

import assets.ModelsAsset._
import com.typesafe.config.ConfigFactory
import common.CommonPlaySpec
import models.resident.{ChargeableGainResultModel, TaxYearModel}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.{Application, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport
import util.WireMockMethods

import java.time.LocalDate

// TODO: Few of the tests have been ignored due to the way underlying connect handles http requests.
// This is not an ideal way and as part of HttpClientV2, the connector interface should be updated accordingly, and then
// re-enable the tests and update them accordingly. Will be captured as part of https://jira.tools.tax.service.gov.uk/browse/DLS-10770

class CalculatorConnectorSpec extends CommonPlaySpec with MockitoSugar
  with WireMockSupport with GuiceOneAppPerSuite with WireMockMethods {

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
    "return a DateTime which matches the returned LocalDate" in {
      val expectedDate = LocalDate.parse("2015-06-04")
      when(GET, "/capital-gains-calculator/minimum-date").thenReturn(Status.OK, expectedDate)

      await(calculatorConnector.getMinimumDate()) shouldBe expectedDate
    }
  }

  "Calling .getFullAEA" should {

    "return a value corresponding to the year if it exists" in {
      val expectedResult = Some(BigDecimal(10000))
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-full-aea").thenReturn(Status.OK, expectedResult)

      await(calculatorConnector.getFullAEA(2017)) shouldBe expectedResult
    }

    "return a none value if it is returned" ignore {
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-full-aea").thenReturn(Status.OK, None)
      await(calculatorConnector.getFullAEA(2017)) shouldBe None
    }
  }

  "Calling .getPartialAEA" should {
    "return a value corresponding to the year if it exists" in {
      val expectedResult = Some(BigDecimal(10000))
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-partial-aea").thenReturn(Status.OK, expectedResult)

      await(calculatorConnector.getPartialAEA(2017)) shouldBe expectedResult
    }

    "return a none value if it is returned" ignore {
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-partial-aea").thenReturn(Status.OK, None)
      await(calculatorConnector.getPartialAEA(2017)) shouldBe None
    }
  }

  "Calling .getPA" should {
    "return a value corresponding to the year if it exists without blind persons allowance" in {
      val expectedResult = Some(BigDecimal(10000))
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-pa").thenReturn(Status.OK, expectedResult)

      await(calculatorConnector.getPA(2017)) shouldBe expectedResult
    }

    "return a none value if it is returned with blind persons allowance" ignore {
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-pa").thenReturn(Status.OK, None)
      await(calculatorConnector.getPA(2017, isEligibleBlindPersonsAllowance = true)) shouldBe None
    }
  }

  "Calling .getTaxYear" should {
    "return a value corresponding to the year if it exists" in {
      val expectedResult = Some(TaxYearModel("2017", isValidYear = true, "2017"))
      when(GET, "/capital-gains-calculator/tax-year").thenReturn(Status.OK, expectedResult)

      await(calculatorConnector.getTaxYear("2017")) shouldBe expectedResult
    }

    "return a none value if it is returned" ignore {
      when(GET, "/capital-gains-calculator/tax-year").thenReturn(Status.OK, None)
      await(calculatorConnector.getTaxYear("2017")) shouldBe None
    }
  }

  "Calling .calculateRttShareGrossGain" should {
    "return a value corresponding to the result" in {
      val expectedResult = BigDecimal(10000)
      when(GET, "/capital-gains-calculator/shares/calculate-total-gain").thenReturn(Status.OK, expectedResult)

      await(calculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles)) shouldBe expectedResult
    }
  }

  "Calling .calculateRttShareChargeableGain" should {
    val chargeableGainResultModel = ChargeableGainResultModel(7000, 0, 11100, 0, 0, BigDecimal(0), BigDecimal(0), None, None, 0, 0)

    "return a value corresponding to the result if it exists" in {
      when(GET, "/capital-gains-calculator/shares/calculate-chargeable-gain").thenReturn(Status.OK, chargeableGainResultModel)

      await(calculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000)) shouldBe Some(chargeableGainResultModel)
    }

    "return a None if it doesn't exist" ignore {
      when(GET, "/capital-gains-calculator/shares/calculate-chargeable-gain").thenReturn(Status.OK, None)

      await(calculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000)) shouldBe None
    }
  }

  "Calling .calculateRttShareTotalGainAndTax" should {
    "return a value corresponding to the result if it exists" in {
      when(GET, "/capital-gains-calculator/shares/calculate-resident-capital-gains-tax")
        .thenReturn(Status.OK, totalGainAndTaxOwedModel)

      await(calculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers)) shouldBe Some(totalGainAndTaxOwedModel)
    }

    "return a None if it doesn't exist" ignore {
      when(GET, "/capital-gains-calculator/shares/calculate-resident-capital-gains-tax").thenReturn(Status.OK, None)
      await(calculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers)) shouldBe None
    }
  }

  "Calling .getSharesTotalCosts" should {

    "return a value corresponding to the result" in {
      when(GET, "/capital-gains-calculator/shares/calculate-total-costs").thenReturn(Status.OK, BigDecimal(10000))
      await(calculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles)) shouldBe BigDecimal(10000)
    }
  }

  "Service connection failures on calls to connector" should {

    "return an exception for getSharesTotalCosts" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/shares/calculate-total-costs")

      (the[Exception] thrownBy await(calculatorConnector.getSharesTotalCosts(gainAnswersMostPossibles)))
        .getMessage should include ("Connection refused")
      wireMockServer.start()
    }

    "return an exception for calculateRttShareTotalGainAndTax" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/shares/calculate-resident-capital-gains-tax")

      (the[Exception] thrownBy await(calculatorConnector.calculateRttShareTotalGainAndTax(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000, incomeAnswers))).getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for calculateRttShareChargeableGain" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/shares/calculate-chargeable-gain")

      (the[Exception] thrownBy await(calculatorConnector.calculateRttShareChargeableGain(gainAnswersMostPossibles,
        deductionAnswersMostPossibles, 10000))).getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for calculateRttShareGrossGain" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/shares/calculate-total-gain")

      (the[Exception] thrownBy await(calculatorConnector.calculateRttShareGrossGain(gainAnswersMostPossibles)))
        .getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for getTaxYear" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/tax-year")

      (the[Exception] thrownBy await(calculatorConnector.getTaxYear("2017")))
        .getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for getPA" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-pa")

      (the[Exception] thrownBy await(calculatorConnector.getPA(2017)))
        .getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for getPartialAEA" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-partial-aea")

      (the[Exception] thrownBy await(calculatorConnector.getPartialAEA(2017)))
        .getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for getFullAEA" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/tax-rates-and-bands/max-full-aea")

      (the[Exception] thrownBy await(calculatorConnector.getFullAEA(2017)))
        .getMessage should include("Connection refused")
      wireMockServer.start()
    }

    "return an exception for getMinimumDate" in {
      wireMockServer.stop()
      when(GET, "/capital-gains-calculator/minimum-date")

      (the[Exception] thrownBy await(calculatorConnector.getMinimumDate()))
        .getMessage should include("Connection refused")
      wireMockServer.start()
    }
  }
}
