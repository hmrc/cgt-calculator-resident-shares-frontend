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

package views.helpers

import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.playHelpers.resident.summaryNumericRowHelper

class SummaryNumericRowHelperSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val summaryNumericRowHelperView = fakeApplication.injector.instanceOf[summaryNumericRowHelper]
  lazy val row = summaryNumericRowHelperView("testID","testQ",2000)(mockMessage)
  lazy val doc = Jsoup.parse(row.body)

  "The Summary Numeric Row Helper with no link" should {

    "have no link" in {
      doc.select("#testID-change-link").size shouldBe 0
    }

    s"if given data that includes a change link " should {

      lazy val rowWithChangeLink = summaryNumericRowHelperView("testID","testQ",2000,Some("link"))(mockMessage)
      lazy val link = Jsoup.parse(rowWithChangeLink.body).select("a")

      "include a change link" which {

        "has a link to 'link'" in {
          link.attr("href") shouldBe "link"
        }

        "has the text 'change'" in {
          link.text shouldBe commonMessages.change + " testQ"
        }

        "has a span" which {
          "contains the question text" in {
            link.select("span").text shouldEqual "testQ"
          }

          "has the class govuk visually hidden" in {
            link.select("span").hasClass("govuk-visually-hidden") shouldEqual true
          }
        }
      }
    }
  }
}
