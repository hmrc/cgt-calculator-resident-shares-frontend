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

package forms

import models.resident.SaUserModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import forms.SaUserForm._
import assets.MessageLookup.{SaUser => messages}

class SaUserFormSpec extends UnitSpec with WithFakeApplication {

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      lazy val model = SaUserModel(true)
      lazy val form = saUserForm.fill(model)
      form.value shouldBe Some(model)
    }
  }

  "Creating a form using a valid map" should {

    "return a form with the data specified in the model" in {
      lazy val form = saUserForm.bind(Map(("isInSa", "Yes")))
      form.value shouldBe Some(SaUserModel(true))
    }
  }

  "Creating a form using an invalid map" when {

    "supplied with no data" should {
      lazy val form = saUserForm.bind(Map(("isInSa", "")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.error}" in {
        form.error("isInSa").get.message shouldBe "calc.resident.saUser.errorSelect"
      }
    }

    "supplied with invalid data" should {
      lazy val form = saUserForm.bind(Map(("isInSa", "a")))

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      "return 1 error" in {
        form.errors.size shouldBe 1
      }

      s"return an error with message ${messages.error}" in {
        form.error("isInSa").get.message shouldBe "calc.resident.saUser.errorSelect"
      }
    }
  }
}
