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

package common

import common.Validation._
import play.api.data.validation.{Invalid, Valid}

class ValidationSpec extends CommonPlaySpec {
  //############# Tests for isPositive function ##########################################
  "calling common.Validation.isPositive(amount) " should {
    "with a positive numeric supplied isPositive(1) return true" in {
      isPositive(1) shouldBe true
    }

    "with Zero supplied return true" in {
      isPositive(0) shouldBe true
    }

    "with Negative supplied return false" in {
      isPositive(-1) shouldBe false
    }
  }

  //############# Tests for decimalPlacesCheck ##########################################
  "calling common.Validation.decimalPlacesCheck(amount) " should {
    "with no decimals supplied decimalPlacesCheck(1) return true" in {
      decimalPlacesCheck(1) shouldBe true
    }

    "with one decimal place supplied decimalPlacesCheck(1.1) return true" in {
      decimalPlacesCheck(1.1) shouldBe true
    }

    "with two decimal places supplied decimalPlacesCheck(1.11) return true" in {
      decimalPlacesCheck(1.11) shouldBe true
    }

    "with three decimal places supplied decimalPlacesCheck(1.111) return false" in {
      decimalPlacesCheck(1.111) shouldBe false
    }
  }

  //############# Tests for yesNoCheck ##########################################
  "calling common.Validation.yesNoCheck" should {
    "return false with a non yes/no value" in {
      yesNoCheck("a") shouldBe false
    }

    "return true with a yes value" in {
      yesNoCheck("Yes") shouldBe true
    }

    "return true with a no value" in {
      yesNoCheck("No") shouldBe true
    }
  }

  "calling bigDecimalCheck" when {
    "input contains non-numeric characters" should {
      "fail" in {
        bigDecimalCheck("abc") shouldBe false
      }
    }

    "empty input" should {
      "pass" in {
        bigDecimalCheck("") shouldBe true
      }
    }

    "empty space" should {
      "pass" in {
        bigDecimalCheck("   ") shouldBe true
      }
    }

    "input only contains numeric characters" should {
      "pass" in {
        bigDecimalCheck("123") shouldBe true
      }
    }
  }

  "calling mandatoryCheck" when {
    "input contains no data" should {
      "fail" in {
        mandatoryCheck("") shouldBe false
      }
    }

    "input contains only empty space" should {
      "fail" in {
        mandatoryCheck("    ") shouldBe false
      }
    }

    "input contains data" should {
      "pass" in {
        mandatoryCheck("123") shouldBe true
      }
    }
  }

  "calling decimalPlacesCheck" when {
    "input has no decimal places" should {
      "pass" in {
        decimalPlacesCheck(BigDecimal(1)) shouldBe true
      }
    }

    "input has 1 decimal place" should {
      "pass" in {
        decimalPlacesCheck(BigDecimal(1.1)) shouldBe true
      }
    }

    "input has 2 decimal places" should {
      "pass" in {
        decimalPlacesCheck(BigDecimal(1.11)) shouldBe true
      }
    }

    "input has 3 decimal places" should {
      "fail" in {
        decimalPlacesCheck(BigDecimal(1.111)) shouldBe false
      }
    }
  }

  "calling isPositive" when {
    "input is more than min value" should {
      "pass" in {
        isPositive(BigDecimal(0.01)) shouldBe true
      }
    }

    "input is equal to min value" should {
      "pass" in {
        isPositive(BigDecimal(0)) shouldBe true
      }
    }

    "input is less than min value" should {
      "fail" in {
        isPositive(BigDecimal(-0.01)) shouldBe false
      }
    }
  }
  
  "calling yesNoCheck" when {
    "input is 'Yes'" should {
      "pass" in {
        yesNoCheck("Yes") shouldBe true
      }
    }

    "input is 'No'" should {
      "pass" in {
        yesNoCheck("No") shouldBe true
      }
    }

    "input is empty" should {
      "pass" in {
        yesNoCheck("") shouldBe true
      }
    }

    "input is 'yEs'" should {
      "fail" in {
        yesNoCheck("yEs") shouldBe false
      }
    }

    "input is 'nO'" should {
      "fail" in {
        yesNoCheck("nO") shouldBe false
      }
    }

    "input is empty space" should {
      "fail" in {
        yesNoCheck("    ") shouldBe false
      }
    }
  }

  "Calling .optionalMandatoryCheck" should {
    "return a false when an empty value is provided" in {
      optionalMandatoryCheck(Some(" ")) shouldBe false
    }

    "return a false when no value is provided" in {
      optionalMandatoryCheck(None) shouldBe false
    }

    "return a true when a value is provided" in {
      optionalMandatoryCheck(Some("test")) shouldBe true
    }
  }

  "Calling .optionalYesNoCheck" should {
    "return a true when no value is provided" in {
      optionalYesNoCheck(None) shouldBe true
    }

    "return a true when an empty value is provided" in {
      optionalYesNoCheck(Some("")) shouldBe true
    }

    "return a true when a Yes is provided" in {
      optionalYesNoCheck(Some("Yes")) shouldBe true
    }

    "return a true when a No is provided" in {
      optionalYesNoCheck(Some("No")) shouldBe true
    }

    "return a false when any other value is provided" in {
      optionalYesNoCheck(Some("test")) shouldBe false
    }
  }

  "Calling .optionStringToBoolean" should {
    "return a true when a Yes is provided" in {
      optionStringToBoolean(Some("Yes")) shouldBe true
    }

    "return a false when a yes is provided" in {
      optionStringToBoolean(Some("yes")) shouldBe false
    }

    "return a false when a random string is provided" in {
      optionStringToBoolean(Some("aabbbcc")) shouldBe false
    }

    "return a false when a none is provided" in {
      optionStringToBoolean(None) shouldBe false
    }
  }

  "Calling .booleanToOptionString" should {
    "return Some(Yes) when a true is provided" in {
      booleanToOptionString(true) shouldBe Some("Yes")
    }

    "return Some(No) when a false is provided" in {
      booleanToOptionString(false) shouldBe Some("No")
    }
  }

  "The max monetary value constraint" should {
    val maxValue: BigDecimal = 3000
    val extractMoney: BigDecimal => Option[BigDecimal] = money  => Some(money)

    "return invalid if given an amount of money more than the max" in {
      val result = maxMonetaryValueConstraint(maxValue, extractMoney)(maxValue + 1).getClass

      result shouldBe classOf[Invalid]
    }

    "return valid if given an amount of money less than the max" in {
      val result = maxMonetaryValueConstraint(maxValue, extractMoney)(maxValue - 1)

      result shouldBe Valid
    }

    "return valid if given an amount that results in None" in {
      val extractMoneyNone: BigDecimal => Option[BigDecimal] = _ => None
      val result = maxMonetaryValueConstraint(maxValue, extractMoneyNone)(maxValue)

      result shouldBe Valid
    }
  }
}
