/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.{LocalDate, ZoneId}

import assets.MessageLookup.{DisposalDate => messages}
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm._
import models.resident.DisposalDateModel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class DisposalDateFormSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Creating the form for the disposal date" should {
    "return a populated form using .fill" in {
      lazy val model = DisposalDateModel(10, 10, 2016)
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).fill(model)
      form.value.get shouldBe DisposalDateModel(10, 10, 2016)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      lazy val map = Map(("disposalDate.day", "10"), ("disposalDate.month", "10"), ("disposalDate.year", "2016"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(map)
      form.value shouldBe Some(DisposalDateModel(10, 10, 2016))
    }
  }
  "Creating an invalid form for the disposal date" when {

    "empty fields are entered" should {
      lazy val map = Map(("disposalDate.day", ""), ("disposalDate.month", ""), ("disposalDate.year", ""))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for day of ${messages.invalidDayError}" in {
        form.error("disposalDate.day").get.message shouldBe "calc.resident.disposalDate.invalidDayError"
      }

      s"have an error message for month of ${messages.invalidMonthError}" in {
        form.error("disposalDate.month").get.message shouldBe "calc.resident.disposalDate.invalidMonthError"
      }

      s"have an error message for year of ${messages.invalidYearError}" in {
        form.error("disposalDate.year").get.message shouldBe "calc.resident.disposalDate.invalidYearError"
      }
    }

    "non-numeric fields are entered" should {
      lazy val map = Map(("disposalDate.day", "a"), ("disposalDate.month", "b"), ("disposalDate.year", "c"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for day of ${messages.invalidDayError}" in {
        form.error("disposalDate.day").get.message shouldBe "calc.resident.disposalDate.invalidDayError"
      }

      s"have an error message for month of ${messages.invalidMonthError}" in {
        form.error("disposalDate.month").get.message shouldBe "calc.resident.disposalDate.invalidMonthError"
      }

      s"have an error message for year of ${messages.invalidYearError}" in {
        form.error("disposalDate.year").get.message shouldBe "calc.resident.disposalDate.invalidYearError"
      }
    }

    "an invalid date is entered" should {
      lazy val map = Map(("disposalDate.day", "32"), ("disposalDate.month", "4"), ("disposalDate.year", "2016"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.realDateError}" in {
        form.errors.head.message shouldBe "calc.common.date.error.invalidDate"
      }
    }

    "a year which is less than 1900" should {
      lazy val map = Map(("disposalDate.day", "1"), ("disposalDate.month", "1"), ("disposalDate.year", "1899"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.invalidYearRange}" in {
        form.errors.head.message shouldBe "calc.resident.disposalDate.invalidYearRangeError"
      }
    }

    "a year which is greater than 9999" should {
      lazy val map = Map(("disposalDate.day", "1"), ("disposalDate.month", "1"), ("disposalDate.year", "10000"))
      lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(map)

      "return a form with errors" in {
        form.hasErrors shouldBe true
      }

      s"have an error message for the date of ${messages.invalidYearRange}" in {
        form.errors.head.message shouldBe "calc.resident.disposalDate.invalidYearRangeError"
      }
    }
  }
}
