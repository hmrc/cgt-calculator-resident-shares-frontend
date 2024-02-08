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

package constructors

import java.time.LocalDate

import common.{CommonPlaySpec, Dates}
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}

class CalculateRequestConstructorSpec extends CommonPlaySpec {

  "totalGainRequestString" should {

    "return a valid url variable string" in {
      val answers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 2, 2016),
        soldForLessThanWorth = false,
        disposalValue = Some(1000),
        worthWhenSoldForLess = None,
        disposalCosts = 0,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(500),
        acquisitionCosts = 100
      )
      val result = CalculateRequestConstructor.totalGainRequestString(answers)
      result shouldBe s"?disposalValue=1000.0" +
        s"&disposalCosts=0.0" +
        s"&acquisitionValue=500.0" +
        s"&acquisitionCosts=100.0" +
        s"&disposalDate=2016-02-10"
    }
  }

  "chargeableGainRequestString" when {

    "supplied with no optional values" should {

      "return a valid url variable string" in {
        val answers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)),
          None)
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&annualExemptAmount=11100.0"
      }
    }

    "supplied with all optional values" should {

      "return a valid url variable string" in {
        val answers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))))
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&broughtForwardLosses=2000.0&annualExemptAmount=11100.0"
      }
    }
  }

  "calling incomeAnswersRequestString" when {

    "supplied with personal allowance and previous income" should {
      val deductionGainAnswersModel = DeductionGainAnswersModel(None, None)
      val incomeGainAnswersModel = IncomeAnswersModel(Some(CurrentIncomeModel(1000)), Some(PersonalAllowanceModel(10600)))

      "return a valid request string" in {
        val result = CalculateRequestConstructor.incomeAnswersRequestString(deductionGainAnswersModel, incomeGainAnswersModel)
        result shouldBe "&previousIncome=1000.0&personalAllowance=10600.0"
      }
    }
  }

  "Calling determineDisposalValueToUse" should {

    "return a value from the disposal value if not sold for less" in {
      val answers = GainAnswersModel(LocalDate.parse("2015-05-05"),
        soldForLessThanWorth = false, Some(1500), Some(2500), 0, ownerBeforeLegislationStart = false, None, Some(false), None, None, 0)
      val result = CalculateRequestConstructor.determineDisposalValueToUse(answers)

      result shouldBe 1500
    }

    "return a value from the worth when sold for less value if sold for less" in {
      val answers = GainAnswersModel(LocalDate.parse("2015-05-05"),
        soldForLessThanWorth = true, Some(1500), Some(2500), 0, ownerBeforeLegislationStart = false, None, Some(false), None, None, 0)
      val result = CalculateRequestConstructor.determineDisposalValueToUse(answers)

      result shouldBe 2500
    }
  }

  "Calling determineAcquisitionValueToUse" should {

    "return a value from the worth on value if owned before tax start" in {
      val answers = GainAnswersModel(LocalDate.parse("2015-05-05"),
        true, None, None, 0, true, Some(1500), None, Some(2500), Some(3500), 0)
      val result = CalculateRequestConstructor.determineAcquisitionValueToUse(answers)

      result shouldBe 1500
    }

    "return a value from the worth when inherited value if inherited" in {
      val answers = GainAnswersModel(LocalDate.parse("2015-05-05"),
        true, None, None, 0, false, Some(1500), Some(true), Some(2500), Some(3500), 0)
      val result = CalculateRequestConstructor.determineAcquisitionValueToUse(answers)

      result shouldBe 2500
    }

    "return a value from the acquisition value if not inherited and owned after tax start" in {
      val answers = GainAnswersModel(LocalDate.parse("2015-05-05"),
        true, None, None, 0, false, Some(1500), Some(false), Some(2500), Some(3500), 0)
      val result = CalculateRequestConstructor.determineAcquisitionValueToUse(answers)

      result shouldBe 3500
    }
  }
}
