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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.resident.income.CurrentIncomeModel
import play.api.data.Form
import play.api.i18n.{Lang, Messages, MessagesApi}

class CurrentIncomeFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  lazy val injectedForm: CurrentIncomeForm = fakeApplication.injector.instanceOf[CurrentIncomeForm]
  lazy val messagesApi: MessagesApi = fakeApplication.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(fakeRequest)
  lazy val currentIncomeForm: Form[CurrentIncomeModel] = injectedForm("2022")


  "Creating a form using an empty model" should {


    "return an empty string for amount" in {
      currentIncomeForm.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {

    "return a form with the data specified in the model" in {
      val model = CurrentIncomeModel(1)
      val form = currentIncomeForm.fill(model)
      form.data("amount") shouldBe "1"
    }

  }

  "Creating a form using an invalid post" when {

    "supplied with no data for amount" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> ""))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.currentIncome.question.error.mandatoryAmount", "2022")(using Lang("en"))
      }
    }

    "supplied with empty space for amount" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> "  "))

      "raise form error" in {
        form.hasErrors shouldBe true
      }


      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.currentIncome.question.error.mandatoryAmount", "2022")(using Lang("en"))
      }
    }

    "supplied with non numeric input for amount" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> "a"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.currentIncome.question.error.invalidAmount", "2022")(using Lang("en"))
      }
    }

    "supplied with an amount with 3 numbers after the decimal" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> "1.000"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.currentIncome.question.error.invalidDecimalPlace", "2022")(using Lang("en"))
      }
    }

    "supplied with an amount that's greater than the max" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> "1000000000.01"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe "calc.resident.currentIncome.question.error.maxAmountExceeded"
      }
    }

    "supplied with an amount that's less than the zero" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> "-0.01"))

      "raise form error" in {
        form.hasErrors shouldBe true
      }

      "raise 1 form error" in {
        form.errors.length shouldBe 1
      }

      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe messagesApi("calc.resident.currentIncome.question.error.minimumAmount", "2022")(using Lang("en"))
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with valid amount" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> "1"))

      "build a model with the correct amount" in {
        form.value.get shouldBe CurrentIncomeModel(BigDecimal(1))
      }

      "not raise form error" in {
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 1 number after the decimal" should {
      "not raise form error" in {
        val form = currentIncomeForm.bind(Map("amount" -> "1.1"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount with 2 numbers after the decimal" should {
      "not raise form error" in {
        val form = currentIncomeForm.bind(Map("amount" -> "1.11"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the max" should {
      "not raise form error" in {
        val form = currentIncomeForm.bind(Map("amount" -> "1000000000"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with an amount that's equal to the min" should {
      "not raise form error" in {
        val form = currentIncomeForm.bind(Map("amount" -> "0"))
        form.hasErrors shouldBe false
      }
    }
  }
}
