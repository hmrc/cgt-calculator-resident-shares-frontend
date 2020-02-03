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

package common

import uk.gov.hmrc.play.test.UnitSpec
import java.time.LocalDate

import common.Dates.TemplateImplicits.RichDate
import common.Dates.formatter
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages}

class DatesSpec extends UnitSpec with MockitoSugar {

  "Calling constructDate method" should {

    "return a valid date object with single digit inputs" in {
      Dates.constructDate(1, 2, 1990) shouldBe LocalDate.parse("01/02/1990", formatter)
    }

    "return a valid date object with double digit inputs" in {
      Dates.constructDate(10, 11, 2016) shouldBe LocalDate.parse("10/11/2016", formatter)
    }
  }

  "Calling getDay" should {
    "return an integer value of the day" in {
      Dates.getDay(LocalDate.parse("12/12/2014", formatter)) shouldEqual 12
    }
  }

  "Calling getMonth" should {
    "return an integer value of the month" in {
      Dates.getMonth(LocalDate.parse("11/12/2014", formatter)) shouldEqual 12
    }
  }
  "Calling getYear" should {
    "return an integer value of the year" in {
      Dates.getYear(LocalDate.parse("12/12/2014", formatter)) shouldEqual 2014
    }
  }

  "Calling getCurrent Tax Year" should {

    class TestDates(date: String) extends Dates {
      override def now: LocalDate = LocalDate.parse(date)
    }

    "return the current tax year correctly on the last day of a tax year" in {
      val res: String = new TestDates("2014-04-05").getCurrentTaxYear
      res.length shouldEqual 7
      res shouldBe "2013/14"
    }
    "return the current tax year correctly on the first day of a tax year" in {
      val res: String = new TestDates("2014-04-06").getCurrentTaxYear
      res shouldBe "2014/15"
    }
  }

  "Calling taxYearOfDateLongHand" should {
    "when called with 2016/4/6 return 2016 to 2017" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(2016, 4, 6)) shouldBe "2016 to 2017"
    }

    "when called with a date of 2016/4/5 return 2015 to 2016" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(2016, 4, 5)) shouldBe "2015 to 2016"
    }

    "when called with 1999/4/6 return 1999 to 2000" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(1999, 4, 6)) shouldBe "1999 to 2000"
    }

    "when called with 1999/4/5 return 1999 to 2000" in {
      Dates.taxYearOfDateLongHand(LocalDate.of(1999, 4, 5)) shouldBe "1998 to 1999"
    }
  }

  "The RichDate wrapper class" should {

    "format welsh dates accordingly" in {

      val localDate = LocalDate.parse("2014-04-05")
      val richDate: RichDate = new Dates.TemplateImplicits.RichDate(localDate)
      val monthWelshTranslation = "test-month"

      val mockLanguage = mock[Lang]
      val mockMessages = mock[Messages]

      when(mockLanguage.language) thenReturn "cy"
      when(mockMessages.apply(s"calc.month.${localDate.getMonthValue}")) thenReturn monthWelshTranslation

      val localFormatString = richDate.localFormat("")(mockLanguage, mockMessages)

      localFormatString shouldBe localDate.getDayOfMonth + " " + monthWelshTranslation + " " + localDate.getYear
    }
  }
}
