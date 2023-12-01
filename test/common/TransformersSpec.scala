/*
 * Copyright 2023 HM Revenue & Customs
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


import scala.util.Random

class TransformersSpec extends CommonPlaySpec {

  val nonNumericString = "non-numeric"

  "The Transformers object" when {

    "stripping currency characters" should {
      "return a cleaned currency amount" when {
        "the transform is applied to an amount with commas and pound signs" in {
          val result = Transformers.stripCurrencyCharacters("£1,999")
          result shouldBe "1999"
        }
        "the transform is applied to an amount with commas" in {
          val result = Transformers.stripCurrencyCharacters("1,999")
          result shouldBe "1999"
        }
        "the transform is applied to an amount with pound signs" in {
          val result = Transformers.stripCurrencyCharacters("£1999")
          result shouldBe "1999"
        }
        "the transform is applied to an amount without commas and pound signs" in {
          val result = Transformers.stripCurrencyCharacters("1999")
          result shouldBe "1999"
        }
      }
    }

    "Converting a String to a BigDecimal" should {

      "successfully parse a valid string to a BigDecimal value" in {

        val randomDouble: Double = Random.nextDouble()

        Transformers.stringToBigDecimal(randomDouble.toString) shouldBe BigDecimal(randomDouble)
      }

      "produce BigDecimal(0) if an invalid string is passed to it" in {

        Transformers.stringToBigDecimal(nonNumericString) shouldBe BigDecimal(0)
      }
    }

    "Converting a BigDecimal to a String" should {

      "pad BigDecimals to 2 decimal places if they have a non 0 scale less than 2" in {

        val bigDecimal = BigDecimal(1234.5)
        val bigDecimalNegativeScale = BigDecimal("1.2345E+3")

        Transformers.bigDecimalToString(bigDecimal) shouldBe bigDecimal.setScale(2).toString
        Transformers.bigDecimalToString(bigDecimalNegativeScale) shouldBe bigDecimal.setScale(2).toString()
      }

      "call .toString on BigDecimals with no decimal places or more than 1 decimal" in {

        val bigDecimalNoDecimalPlaces = BigDecimal(1234)
        val bigDecimalSomeDecimalPlaces = BigDecimal(1234.56789)

        Transformers.bigDecimalToString(bigDecimalNoDecimalPlaces) shouldBe bigDecimalNoDecimalPlaces.toString
        Transformers.bigDecimalToString(bigDecimalSomeDecimalPlaces) shouldBe bigDecimalSomeDecimalPlaces.toString
      }
    }

    "Converting a String to an Integer" should {

      "produce 0 if an invalid string is passed to it" in {

        Transformers.stringToInteger(nonNumericString) shouldBe 0
      }
    }
  }
}
