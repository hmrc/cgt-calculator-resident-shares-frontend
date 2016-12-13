/*
 * Copyright 2016 HM Revenue & Customs
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

package constructors.resident.shares

import java.time.LocalDate

import common.Dates
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel, PreviousTaxableGainsModel}
import models.resident.{AllowableLossesValueModel, AnnualExemptAmountModel, _}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import uk.gov.hmrc.play.test.UnitSpec

class CalculateRequestConstructorSpec extends UnitSpec {

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
      result shouldBe s"?disposalValue=1000" +
        s"&disposalCosts=0" +
        s"&acquisitionValue=500" +
        s"&acquisitionCosts=100" +
        s"&disposalDate=2016-02-10"
    }
  }

  "chargeableGainRequestString" when {

    "supplied with no optional values" should {

      "return a valid url variable string" in {
        val answers = DeductionGainAnswersModel(Some(OtherPropertiesModel(false)),
          None,
          None,
          Some(LossesBroughtForwardModel(false)),
          None,
          None)
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&annualExemptAmount=11100"
      }
    }

    "supplied with all optional values except allowable losses" should {

      "return a valid url variable string" in {
        val answers = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(false)),
          None,
          Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))),
          Some(AnnualExemptAmountModel(BigDecimal(3000))))
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&broughtForwardLosses=2000&annualExemptAmount=3000"
      }
    }

    "supplied with all optional values including allowable losses" should {

      "return a valid url variable string" in {
        val answers = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))),
          Some(LossesBroughtForwardModel(true)),
          Some(LossesBroughtForwardValueModel(BigDecimal(2000))),
          Some(AnnualExemptAmountModel(BigDecimal(3000))))
        val result = CalculateRequestConstructor.chargeableGainRequestString(answers, BigDecimal(11100))
        result shouldBe "&allowableLosses=1000&broughtForwardLosses=2000&annualExemptAmount=11100"
      }
    }
  }

  "calling .isUsingAnnualExemptAmount" when {

    "disposed other properties and claimed non-zero allowable losses" should {

      "return a true" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(1000))))
        result shouldBe false
      }
    }

    "disposed other properties and claimed zero allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(0))))
        result shouldBe true
      }
    }

    "disposed other properties and didn't claim allowable losses" should {

      "return a true" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(true)),
          Some(AllowableLossesModel(false)),
          Some(AllowableLossesValueModel(BigDecimal(0))))
        result shouldBe true
      }
    }

    "disposed no other properties or allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(false)),
          Some(AllowableLossesModel(false)),
          Some(AllowableLossesValueModel(BigDecimal(100))))
        result shouldBe false
      }
    }

    "disposed no other properties and but claimed allowable losses" should {

      "return a false" in {
        val result = CalculateRequestConstructor.isUsingAnnualExemptAmount(Some(OtherPropertiesModel(false)),
          Some(AllowableLossesModel(true)),
          Some(AllowableLossesValueModel(BigDecimal(100))))
        result shouldBe false
      }
    }
  }

  "calling incomeAnswersRequestString" when {

    "user has no previous disposals" should {
      val deductionGainAnswersModel = DeductionGainAnswersModel(Some(OtherPropertiesModel(false)), None, None, None, None, None)
      val incomeGainAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(1000)), Some(PersonalAllowanceModel(10600)))

      "return a valid request string" in {
        val result = CalculateRequestConstructor.incomeAnswersRequestString(deductionGainAnswersModel, incomeGainAnswersModel)
        result shouldBe "&previousIncome=1000&personalAllowance=10600"
      }
    }

    "user has previous disposals but AEA left" should {
      val deductionGainAnswersModel = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(false)),
        None, None, None, Some(AnnualExemptAmountModel(1000)))
      val incomeGainAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(1000)), Some(PersonalAllowanceModel(10600)))

      "return a valid request string" in {
        val result = CalculateRequestConstructor.incomeAnswersRequestString(deductionGainAnswersModel, incomeGainAnswersModel)
        result shouldBe "&previousIncome=1000&personalAllowance=10600"
      }
    }

    "user has previous disposals but allowable losses" should {
      val deductionGainAnswersModel = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(true)),
        None, None, None, None)
      val incomeGainAnswersModel = IncomeAnswersModel(None, Some(CurrentIncomeModel(1000)), Some(PersonalAllowanceModel(10600)))

      "return a valid request string" in {
        val result = CalculateRequestConstructor.incomeAnswersRequestString(deductionGainAnswersModel, incomeGainAnswersModel)
        result shouldBe "&previousIncome=1000&personalAllowance=10600"
      }
    }

    "user has previous taxable gains" should {
      val deductionGainAnswersModel = DeductionGainAnswersModel(Some(OtherPropertiesModel(true)), Some(AllowableLossesModel(false)),
        None, None, None, Some(AnnualExemptAmountModel(0)))
      val incomeGainAnswersModel = IncomeAnswersModel(Some(PreviousTaxableGainsModel(2000)), Some(CurrentIncomeModel(1000)), Some(PersonalAllowanceModel(10600)))

      "return a valid request string" in {
        val result = CalculateRequestConstructor.incomeAnswersRequestString(deductionGainAnswersModel, incomeGainAnswersModel)
        result shouldBe "&previousTaxableGain=2000&previousIncome=1000&personalAllowance=10600"
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
