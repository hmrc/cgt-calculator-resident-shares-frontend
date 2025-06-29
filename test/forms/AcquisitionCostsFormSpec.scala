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

package forms

import assets.MessageLookup.{Resident => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionCostsForm._
import models.resident.AcquisitionCostsModel

class AcquisitionCostsFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  "Creating a form using an empty model" should {

    val form = acquisitionCostsForm

    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      val model = AcquisitionCostsModel(1)
      val form = acquisitionCostsForm.fill(model)
      form.data("amount") shouldBe "1"
    }
  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = acquisitionCostsForm.bind(Map("amount" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.mandatoryAmount}'" in {
        form.error("amount").get.message shouldBe "calc.resident.shares.acquisitionCosts.error.mandatoryAmount"
      }
    }

    "supplied with a non-numeric value for amount" should {

      lazy val form = acquisitionCostsForm.bind(Map("amount" -> "a"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.invalidAmount}'" in {
        form.error("amount").get.message shouldBe "calc.resident.shares.acquisitionCosts.error.invalidAmount"
      }
    }

    "supplied with an amount that is too big" should {
      lazy val form = acquisitionCostsForm.bind(Map(("amount", "9999999999999")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.maximumAmount}" in {
        form.error("amount").get.message shouldBe "calc.resident.shares.acquisitionCosts.error.maxAmountExceeded"
      }
    }

    "supplied with a negative amount" should {

      lazy val form = acquisitionCostsForm.bind(Map("amount" -> "-1000"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.minimumAmount}'" in {
        form.error("amount").get.message shouldBe "calc.resident.shares.acquisitionCosts.error.minimumAmount"
      }
    }

    "supplied with an amount that has too many decimal places" should {

      lazy val form = acquisitionCostsForm.bind(Map("amount" -> "100.1234"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.invalidDecimalPlace}'" in {
        form.error("amount").get.message shouldBe "calc.resident.shares.acquisitionCosts.error.invalidDecimalPlace"
      }
    }
  }
}
