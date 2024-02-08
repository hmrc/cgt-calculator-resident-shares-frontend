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
import models.resident.income.PersonalAllowanceModel
import common.resident.MoneyPounds
import play.api.data.Form
import play.api.i18n.{Lang, MessagesApi}

class PersonalAllowanceFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  val injectedForm: PersonalAllowanceForm = fakeApplication.injector.instanceOf[PersonalAllowanceForm]
  val personalAllowanceForm: Form[PersonalAllowanceModel] = injectedForm(11000, "2022", Lang("en"))
  val messagesApi: MessagesApi = fakeApplication.injector.instanceOf[MessagesApi]

  "Creating a form using an empty model" should {
    "return an empty string for amount" in {
      personalAllowanceForm.data.isEmpty shouldBe true
    }
  }
  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = PersonalAllowanceModel(1)
      val form = personalAllowanceForm.fill(model)
      form.data("amount") shouldBe "1"
    }
  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = personalAllowanceForm.bind(Map("amount" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.mandatoryAmount}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.personalAllowance.error.mandatoryAmount", "2022")(Lang("en"))
      }
    }

    "supplied with a non-numeric value for amount" should {

      lazy val form = personalAllowanceForm.bind(Map("amount" -> "a"))

      "raise a form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.invalidAmountNoDecimal}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.personalAllowance.error.invalidAmount", "2022")(Lang("en"))
      }
    }

    "supplied with a negative amount" should {

      lazy val form = personalAllowanceForm.bind(Map("amount" -> "-1000"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.minimumAmount}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.personalAllowance.error.minimumAmount", "2022")(Lang("en"))
      }
    }

    "supplied with an amount that has too many decimal placed" should {

      lazy val form = personalAllowanceForm.bind(Map("amount" -> "100.1234"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      s"error with message '${messages.invalidAmountNoDecimal}'" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.personalAllowance.error.invalidAmount", "2022")(Lang("en"))
      }
    }

    "supplied with an amount that is larger than the maximum AEA" should {
            val limit = BigDecimal(11100)
            lazy val form = personalAllowanceForm.bind(Map("amount" -> "11100.01"))
            "raise form error" in {
              form.hasErrors shouldBe true
            }
           s"error with message '${messages.maximumLimit(MoneyPounds(limit, 0).quantity)}'" in {
                form.error("amount").get.message shouldBe "calc.common.error.maxAmountExceeded"
           }
          }

    "The max personal allowance validation" should {

      val maxPersonalAllowance: BigDecimal = BigDecimal(8000)
      val greaterThanMaxPersonalAllowance = maxPersonalAllowance + 1
      val lessThanMaxPersonalAllowance = maxPersonalAllowance - 1
      val validateMaxPaTest = injectedForm.validateMaxPA(maxPersonalAllowance)

      "evaluate as false when passed a value that's greater than the max personal allowance" in {

        validateMaxPaTest(greaterThanMaxPersonalAllowance) shouldBe false
      }

      "evaluate as true when passed a value that's less than the max personal allowance" in {

        validateMaxPaTest(lessThanMaxPersonalAllowance) shouldBe true
      }
    }
  }
}
