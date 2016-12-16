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

package views.helpers

import assets.MessageLookup.{Resident => commonMessages}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.resident.summaryNumericRowHelper
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SummaryNumericRowHelperSpec extends UnitSpec with WithFakeApplication {

  val row = summaryNumericRowHelper("testID","testQ",2000)(applicationMessages)
  val doc = Jsoup.parse(row.body)

  "The Summary Numeric Row Helper with no link" should {

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

      val amountDiv = doc.select("div#testID-amount")

      "has the id 'testID-amount" in {
        amountDiv.attr("id") shouldBe "testID-amount"
      }

      "has the class 'grid-layout__column'" in {
        amountDiv.hasClass("grid-layout__column") shouldBe true
      }

      "has the class 'grid-layout__column--1-2'" in {
        amountDiv.hasClass("grid-layout__column--1-2") shouldBe true
      }

      "has a span with the text 'testQ'" in {
        amountDiv.select("span").text shouldBe "£2,000"
      }

    }

    "have no link" in {
      doc.select("#testID-change-link").size shouldBe 0
    }

    s"if given data that includes a change link " should {

      lazy val rowWithChangeLink = summaryNumericRowHelper("testID","testQ",2000,Some("link"))(applicationMessages)
      lazy val link = Jsoup.parse(rowWithChangeLink.body).select("a")

      "include a change link" which {

        "has a link to 'link'" in {
          link.attr("href") shouldBe "link"
        }

        "has the text 'change'" in {
          link.text shouldBe commonMessages.change + " testQ"
        }
        "has the id testID-change-link" in {
          link.attr("id") shouldBe "testID-change-link"
        }
        "has a span" which {
          "contains the queston text" in {
            link.select("span").text shouldEqual "testQ"
          }

          "has the class visually hidden" in {
            link.select("span").hasClass("visuallyhidden") shouldEqual true
          }
        }
      }
    }
  }
}
