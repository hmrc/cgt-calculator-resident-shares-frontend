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

package common

import uk.gov.hmrc.play.test.UnitSpec

import scala.util.Random

class TransformersSpec extends UnitSpec {

  val nonNumericString = "non-numeric"

  "The Transformers object" when {

    "Converting a String to a BigDecimal" should {

      "successfully parse a valid string to a BigDecimal value" in {

        val randomDouble: Double = Random.nextDouble

        Transformers.stringToBigDecimal(randomDouble.toString) shouldBe BigDecimal(randomDouble)
      }

      "produce BigDecimal(0) if an invalid string is passed to it" in {

        Transformers.stringToBigDecimal(nonNumericString) shouldBe BigDecimal(0)
      }
    }

    "Converting a BigDecimal to a String" should {

      "pad BigDecimals to 2 decimal places if they only have 1" in {

        val bigDecimal = BigDecimal(1234.5)

        Transformers.bigDecimalToString(bigDecimal) shouldBe bigDecimal.setScale(2).toString
      }

      "call .toString on BigDecimals that don't have 1 decimal place" in {

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
