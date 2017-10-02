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

package views.calculation.whatNext

import assets.MessageLookup
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import assets.MessageLookup.{SaUser => messages}
import forms.SaUserForm
import org.jsoup.Jsoup
import views.html.calculation.whatNext.saUser
import play.api.i18n.Messages.Implicits._

class SaUserViewSpec extends UnitSpec with OneAppPerSuite with FakeRequestHelper {

  "SaUserView" when {

    "no errors are present" should {
      lazy val view = saUser(SaUserForm.saUserForm)(messages = applicationMessages, request = fakeRequestWithSession)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title of ${messages.title}" in {
        doc.title shouldBe messages.title
      }

      "have a back button that" should {

        lazy val backLink = doc.select("a#back-link")

        "have the correct back link text" in {
          backLink.text shouldBe messages.back
        }

        "have the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "have a link to Summary" in {
          backLink.attr("href") shouldBe controllers.routes.SummaryController.summary().url
        }
      }

      s"have a heading with the text ${messages.title}" in {
        doc.select("h1").text() shouldBe messages.title
      }

      "have a form" which {
        lazy val form = doc.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"has an action of ${controllers.routes.SaUserController.submitSaUser().url}" in {
          form.attr("action") shouldBe controllers.routes.SaUserController.submitSaUser().url
        }
      }

      "have a legend" which {
        lazy val legend = doc.select("legend")

        s"has the text ${messages.title}" in {
          legend.text() shouldBe messages.title
        }

        "has the class 'visuallyhidden'" in {
          legend.attr("class") shouldBe "visuallyhidden"
        }
      }

      "have an input option for the 'Yes' response" which {

        s"has a label of ${MessageLookup.Resident.yes}" in {
          doc.select("label").get(0).text() shouldBe MessageLookup.Resident.yes
        }

        s"has a value of ${MessageLookup.Resident.yes}" in {
          doc.select("div.multiple-choice input").get(0).attr("value") shouldBe MessageLookup.Resident.yes
        }
      }

      "have an input option for the 'No' response" which {

        s"has a label of ${MessageLookup.Resident.no}" in {
          doc.select("label").get(1).text() shouldBe MessageLookup.Resident.no
        }

        s"has a value of ${MessageLookup.Resident.no}" in {
          doc.select("div.multiple-choice input").get(1).attr("value") shouldBe MessageLookup.Resident.no
        }
      }

      "have a button" which {
        lazy val button = doc.select("button")

        s"has the text ${MessageLookup.Resident.continue}" in {
          button.text() shouldBe MessageLookup.Resident.continue
        }

        "has the type 'submit'" in {
          button.attr("type") shouldBe "submit"
        }
      }

      "display no error summary message for the amount" in {
        doc.body.select("#isInSa-error-summary").size shouldBe 0
      }

      "display no error message for the input" in {
        doc.body.select("span.error-notification").size shouldBe 0
      }
    }

    "errors are present" should {
      lazy val form = SaUserForm.saUserForm.bind(Map("isInSa" -> ""))
      lazy val view = saUser(form)(messages = applicationMessages, request = fakeRequestWithSession)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#isInSa-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("span.error-notification").size shouldBe 1
      }
    }
  }
}
