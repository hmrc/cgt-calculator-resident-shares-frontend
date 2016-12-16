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

package forms

import assets.MessageLookup.{Resident => messages}
import forms.AcquisitionValueForm._
import models.resident.AcquisitionValueModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class AcquisitionValueFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the form for Acquisition Value from a valid input" should {
    "return a populated form using .fill" in {
      val model = AcquisitionValueModel(1000)
      val form = acquisitionValueForm.fill(model)
      form.value.get shouldBe AcquisitionValueModel(1000)
    }

    "return a valid model if supplied with valid inputs" in {
      val form = acquisitionValueForm.bind(Map(("amount", "1000")))
      form.value shouldBe Some(AcquisitionValueModel(1000))
    }
  }

  "Creating the form for Acquisition Value from an invalid input" when {

    "supplied with no data for amount" should {
      lazy val form = acquisitionValueForm.bind(Map(("amount", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.mandatoryAmount}" in {
        form.error("amount").get.message shouldBe messages.mandatoryAmount
      }
    }

    "supplied with a non-numeric value for amount" should {
      lazy val form = acquisitionValueForm.bind(Map(("amount", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.invalidAmount}" in {
        form.error("amount").get.message shouldBe messages.invalidAmount
      }
    }

    "supplied with an amount that is too big" should {
      lazy val form = acquisitionValueForm.bind(Map(("amount", "9999999999999")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.maximumAmount}" in {
        form.error("amount").get.message shouldBe messages.maximumAmount
      }
    }

    "supplied with a negative amount" should {
      lazy val form = acquisitionValueForm.bind(Map(("amount", "-1000")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.minimumAmount}" in {
        form.error("amount").get.message shouldBe messages.minimumAmount
      }
    }

    "supplied with an amount that has too many decimal places" should {
      lazy val form = acquisitionValueForm.bind(Map(("amount", "0.001")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.invalidAmount}" in {
        form.error("amount").get.message shouldBe messages.invalidAmount
      }
    }
  }
}
