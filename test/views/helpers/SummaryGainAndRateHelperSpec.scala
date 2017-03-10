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

package views.helpers

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.resident.summaryGainAndRateHelper
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SummaryGainAndRateHelperSpec extends UnitSpec with WithFakeApplication {

  lazy val rowSingle = summaryGainAndRateHelper("testID","testQ", 1000, 18, None, None)(applicationMessages)
  lazy val docSingle = Jsoup.parse(rowSingle.body)

  lazy val rowDouble = summaryGainAndRateHelper("testID","testQ", 1000, 18, Some(2000), Some(28))(applicationMessages)
  lazy val docDouble = Jsoup.parse(rowDouble.body)

  "The Summary Gain and Rate Row Helper" should {

    "have an outer div" which {

      lazy val outerDiv = docSingle.select("div#testID")

      "has the id 'testID" in {
        outerDiv.attr("id") shouldBe "testID"
      }

      "has the class 'grid-layout'" in {
        outerDiv.hasClass("grid-layout") shouldBe true
      }

      "has the class 'grid-layout--stacked'" in {
        outerDiv.hasClass("grid-layout--stacked") shouldBe true
      }

      "has the class 'form-group'" in {
        outerDiv.hasClass("form-group") shouldBe true
      }

      "has the class 'font-medium'" in {
        outerDiv.hasClass("font-medium") shouldBe true
      }
    }

    "have an inner question div" which {

      lazy val questionDiv = docSingle.select("div#testID-question")

      "has the id 'testID-question" in {
        questionDiv.attr("id") shouldBe "testID-question"
      }

      "has the class 'grid-layout__column'" in {
        questionDiv.hasClass("grid-layout__column") shouldBe true
      }

      "has the class 'grid-layout__column--1-2'" in {
        questionDiv.hasClass("grid-layout__column--1-2") shouldBe true
      }

      "has the text 'testQ'" in {
        questionDiv.text shouldBe "testQ"
      }

    }

    "have an inner result div" which {

      lazy val amountDiv = docSingle.select("div#testID-result")

      "has the id 'testID-result" in {
        amountDiv.attr("id") shouldBe "testID-result"
      }

      "has the class 'grid-layout__column'" in {
        amountDiv.hasClass("grid-layout__column") shouldBe true
      }

      "has the class 'grid-layout__column--1-2'" in {
        amountDiv.hasClass("grid-layout__column--1-2") shouldBe true
      }

      "has a span with the text £1,000 at 18%" in {
        amountDiv.select("#firstBand").text shouldBe "£1,000 at 18%"
      }

    }
  }
  "The Summary Gain and Rate Row Helper with a spilt tax rate" should {

    "have an inner result div" which {

      lazy val amountDiv = docDouble.select("div#testID-result")

      "has the id 'testID-result" in {
        amountDiv.attr("id") shouldBe "testID-result"
      }

      "has the class 'grid-layout__column'" in {
        amountDiv.hasClass("grid-layout__column") shouldBe true
      }

      "has the class 'grid-layout__column--1-2'" in {
        amountDiv.hasClass("grid-layout__column--1-2") shouldBe true
      }

      "has a span with the text £1,000 at 18%" in {
        amountDiv.select("#firstBand").text shouldBe "£1,000 at 18%"
      }

      "and a span with the text £2,000 at 28%" in {
        amountDiv.select("#secondBand").text shouldBe "£2,000 at 28%"
      }

    }
  }
}
