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

import assets.MessageLookup.{OtherProperties => messages}
import forms.OtherPropertiesForm._
import models.resident.OtherPropertiesModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class OtherPropertiesFormSpec extends UnitSpec with WithFakeApplication {

  "Creating the form for Other Properties from a valid selection" should {
    "return a populated form using .fill" in {
      val model = OtherPropertiesModel(true)
      val form = otherPropertiesForm.fill(model)

      form.value.get shouldBe OtherPropertiesModel(true)
    }

    "return a valid model if supplied with valid selection" in {
      val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "Yes")))
      form.value shouldBe Some(OtherPropertiesModel(true))
    }
  }

  "Creating the form for Other Properties from invalid selection" when {

    "supplied with no selection" should {

      lazy val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.errorSelect("2015/16")}" in {
        form.error("hasOtherProperties").get.message shouldBe messages.errorSelect("2015/16")
      }
    }

    "supplied with non Yes/No selection" should {
      lazy val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "abc")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"return a form with the error message ${messages.errorSelect("2015/16")}" in {
        form.error("hasOtherProperties").get.message shouldBe messages.errorSelect("2015/16")
      }
    }
  }

}
