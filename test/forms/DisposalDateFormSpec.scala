/*
 * Copyright 2023 HM Revenue & Customs
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

import assets.MessageLookup.{DisposalDate => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm._
import models.resident.DisposalDateModel
import play.api.i18n.{Messages, MessagesApi}

import java.time.LocalDate

class DisposalDateFormSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  val messagesApi: MessagesApi = fakeApplication.injector.instanceOf[MessagesApi]
  implicit val testMessages: Messages = messagesApi.preferred(fakeRequest)

  "Creating the form for the disposal date" should {
    "return a populated form using .fill" in {
      lazy val model = DisposalDateModel(10, 10, 2016)
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).fill(model)
      form.value.get shouldBe DisposalDateModel(10, 10, 2016)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      lazy val map = Map(("disposalDate.day", "10"), ("disposalDate.month", "10"), ("disposalDate.year", "2016"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)
      form.value shouldBe Some(DisposalDateModel(10, 10, 2016))
    }
  }
  "Creating an invalid form for the disposal date" when {

    "empty fields are entered" should {
      lazy val map = Map(("disposalDate.day", ""), ("disposalDate.month", ""), ("disposalDate.year", ""))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for entire date of ${messages.requiredDateError}" in {
        form.error("disposalDate").get.message shouldBe "disposalDate.error.required"
      }
    }

    "non-numeric fields are entered" should {
      lazy val map = Map(("disposalDate.day", "a"), ("disposalDate.month", "b"), ("disposalDate.year", "c"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for entire date of ${messages.invalidDateError}" in {
        form.error("disposalDate").get.message shouldBe "disposalDate.error.invalid"
      }
    }

    "an invalid date is entered" should {
      lazy val map = Map(("disposalDate.day", "29"), ("disposalDate.month", "2"), ("disposalDate.year", "2017"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.realDateError}" in {
        form.errors.head.message shouldBe "disposalDate.error.notReal"
      }
    }

    "a year which is less than 1000" should {
      lazy val map = Map(("disposalDate.day", "1"), ("disposalDate.month", "1"), ("disposalDate.year", "999"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.invalidYearRange}" in {
        form.errors.head.message shouldBe "disposalDate.error.notReal.year"
      }
    }

    "a year which is greater than 9999" should {
      lazy val map = Map(("disposalDate.day", "1"), ("disposalDate.month", "1"), ("disposalDate.year", "10000"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.invalidYearRange}" in {
        form.errors.head.message shouldBe "disposalDate.error.notReal.year"
      }
    }

    "a date which is before the minimum date" should {
      lazy val map = Map(("disposalDate.day", "1"), ("disposalDate.month", "1"), ("disposalDate.year", "2014"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.invalidMinimumDate}" in {
        form.errors.head.message shouldBe "disposalDate.error.range.min"
      }
    }
  }
}
