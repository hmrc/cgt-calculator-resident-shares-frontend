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

package views.helpers

import assets.MessageLookup.{Resident => commonMessages}
import common.Dates._
import common.{CommonPlaySpec, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import views.html.playHelpers.resident.summaryDateRowHelper

class SummaryDateRowHelperSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper{
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val summaryDateRowHelperView = fakeApplication.injector.instanceOf[summaryDateRowHelper]
  val fakeLang: Lang = Lang("en")
  lazy val row = summaryDateRowHelperView("testID","testQ",constructDate(12,9,1990))(using mockMessage)
  lazy val doc = Jsoup.parse(row.body)

  "The Summary Date Row Helper" should {

    s"if given data that includes a change link " should {

      lazy val rowWithChangeLink = summaryDateRowHelperView("testID","testQ",constructDate(12,9,1990),Some("link"))(using mockMessage)
      lazy val link = Jsoup.parse(rowWithChangeLink.body).select("a")

      "include a change link" which {

        "has a link to 'link'" in {
          link.attr("href") shouldBe "link"
        }

        "has the text 'change'" in {
          link.text shouldBe commonMessages.change + " testQ"
        }

        "has the question govuk visually hidden as part of the link" in {
          link.select("span.govuk-visually-hidden").text shouldBe "testQ"
        }
      }
    }
  }
}
