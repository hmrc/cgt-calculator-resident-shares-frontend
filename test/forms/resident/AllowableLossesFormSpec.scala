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

package forms.resident

import assets.MessageLookup.{AllowableLosses => messages}
import forms.AllowableLossesForm._
import models.resident.AllowableLossesModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class AllowableLossesFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val model = AllowableLossesModel(true)
      lazy val form = allowableLossesForm.fill(model)
      form.value shouldBe Some(model)
    }
  }

  "Creating a form using a valid map" should {

    "return a form with the data specified in the model" in {
      lazy val form = allowableLossesForm.bind(Map(("isClaiming", "Yes")))
      form.value shouldBe Some(AllowableLossesModel(true))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaiming", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorSelect("2015/16")}" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect("2015/16")
      }
    }

    "supplied with invalid data" should {
      lazy val form = allowableLossesForm.bind(Map(("isClaiming", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.errorSelect("2015/16")}" in {
        form.error("isClaiming").get.message shouldBe messages.errorSelect("2015/16")
      }
    }
  }

}
