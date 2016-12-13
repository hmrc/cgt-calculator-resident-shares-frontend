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

package views.resident.helpers

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.resident.summaryOptionRowHelper
import assets.MessageLookup.{Resident => commonMessages}

class SummaryOptionRowHelperSpec extends UnitSpec with WithFakeApplication {

  val row = summaryOptionRowHelper("testID","testQ",true)
  val doc = Jsoup.parse(row.body)

  "The Summary Numeric Row Helper" should {

    "have an outer div" which {

      val outerDiv = doc.select("div#testID")

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

      val questionDiv = doc.select("div#testID-question")

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

    "have an inner amount div" which {

      val amountDiv = doc.select("div#testID-option")

      "has the id 'testID-option" in {
        amountDiv.attr("id") shouldBe "testID-option"
      }

      "has the class 'grid-layout__column'" in {
        amountDiv.hasClass("grid-layout__column") shouldBe true
      }

      "has the class 'grid-layout__column--1-2'" in {
        amountDiv.hasClass("grid-layout__column--1-2") shouldBe true
      }

      "has a span with the text 'testQ'" in {
        amountDiv.select("span").text shouldBe "Yes"
      }

    }

    s"if given data that includes a change link " should {

      lazy val rowWithChangeLink = summaryOptionRowHelper("testID","testQ",true,Some("link"))
      lazy val link = Jsoup.parse(rowWithChangeLink.body).select("a")

      "include a change link" which {

        "has a link to 'link'" in {
          link.attr("href") shouldBe "link"
        }

        "has the text 'change'" in {
          link.text shouldBe commonMessages.change + " testQ"
        }

        "has the question visually hidden as part of the link" in {
          link.select("span.visuallyhidden").text shouldBe "testQ"
        }

        "has the id testID-change-link" in {
          link.attr("id") shouldBe "testID-change-link"
        }
      }
    }
  }
}
