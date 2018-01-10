/*
 * Copyright 2018 HM Revenue & Customs
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

import assets.MessageLookup.Resident.Shares.{OwnerBeforeLegislationStart => Messages}
import forms.OwnerBeforeLegislationStartForm._
import models.resident.shares.OwnerBeforeLegislationStartModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class OwnerBeforeLegislationStartFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the OwnerBeforeLegislationStart form from valid inputs" should {

    "return a populated form using .fill" in {
      val model = OwnerBeforeLegislationStartModel(true)
      val form = ownerBeforeLegislationStartForm.fill(model)
      form.value.get shouldBe OwnerBeforeLegislationStartModel(true)
    }

    "return a populated form using .bind with an answer of Yes" in {
      val form = ownerBeforeLegislationStartForm.bind(Map(("ownerBeforeLegislationStart", "Yes")))
      form.value.get shouldBe OwnerBeforeLegislationStartModel(true)
    }

    "return a populated form using .bind with an answer of No" in {
      val form = ownerBeforeLegislationStartForm.bind(Map(("ownerBeforeLegislationStart", "No")))
      form.value.get shouldBe OwnerBeforeLegislationStartModel(false)
    }
  }

  "Creating the OwnerBeforeLegislationStart form from invalid inputs" when {

    "supplied with no selection" should {
      lazy val form = ownerBeforeLegislationStartForm.bind(Map(("ownerBeforeLegislationStart", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"return a form with the error message ${Messages.errorNoSelect}" in {
        form.error("ownerBeforeLegislationStart").get.message shouldBe Messages.errorNoSelect
      }

    }

    "supplied with an incorrect selection" should {
      lazy val form = ownerBeforeLegislationStartForm.bind(Map(("ownerBeforeLegislationStart", "true")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"return a form with the error message ${Messages.errorNoSelect}" in {
        form.error("ownerBeforeLegislationStart").get.message shouldBe Messages.errorNoSelect
      }
    }
  }
}
