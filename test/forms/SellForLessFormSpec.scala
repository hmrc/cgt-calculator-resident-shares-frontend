/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.Resident.Shares.{SellForLess => Messages}
import forms.SellForLessForm._
import models.resident.SellForLessModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class SellForLessFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the SellForLess form from valid inputs" should {

    "return a populated form using .fill" in {
      val model = SellForLessModel(true)
      val form = sellForLessForm.fill(model)

      form.value.get shouldBe SellForLessModel(true)
    }

    "return a populated form using .bind with an answer of Yes" in {
      val form = sellForLessForm.bind(Map(("sellForLess", "Yes")))

      form.value.get shouldBe SellForLessModel(true)
    }

    "return a populated form using .bind with an answer of No" in {
      val form = sellForLessForm.bind(Map(("sellForLess", "No")))

      form.value.get shouldBe SellForLessModel(false)
    }
  }

  "Creating the SellForLess form from invalid inputs" when {

    "supplied with no selection" should {
      lazy val form = sellForLessForm.bind(Map(("sellForLess", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"error with message '${}'" in {
        form.error("sellForLess").get.message shouldBe Messages.errorSelect
      }

    }

    "supplied with an incorrect selection" should {
      lazy val form = sellForLessForm.bind(Map(("sellForLess", "true")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      s"error with message '${}'" in {
        form.error("sellForLess").get.message shouldBe Messages.errorSelect
      }
    }
  }
}
