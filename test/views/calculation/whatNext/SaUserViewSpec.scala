/*
 * Copyright 2022 HM Revenue & Customs
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
import assets.MessageLookup.{SaUser => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.SaUserForm
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.whatNext.saUser

class SaUserViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val saUserView = fakeApplication.injector.instanceOf[saUser]

  "SaUserView" when {

    "no errors are present" should {
      lazy val view = saUserView(SaUserForm.saUserForm)(fakeRequest, mockMessage)
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
          backLink.hasClass("govuk-back-link") shouldBe true
        }

        "have a link to Summary" in {
          backLink.attr("href") shouldBe controllers.routes.SummaryController.summary().url
        }
      }

      s"have a heading with the text ${messages.heading}" in {
        doc.getElementsByClass("govuk-fieldset__heading").text() shouldBe messages.heading
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

        s"has the text ${messages.heading}" in {
          legend.text() shouldBe messages.heading
        }

        "has the class 'visuallyhidden'" in {
          legend.attr("class") shouldBe "govuk-fieldset__legend govuk-label--xl"
        }
      }

      "have an input option for the 'Yes' response" which {

        s"has a label of ${MessageLookup.Resident.yes}" in {
          doc.select("label").get(0).text() shouldBe MessageLookup.Resident.yes
        }

        s"has a value of ${MessageLookup.Resident.yes}" in {
          doc.select("input").get(0).attr("value") shouldBe MessageLookup.Resident.yes
        }
      }

      "have an input option for the 'No' response" which {

        s"has a label of ${MessageLookup.Resident.no}" in {
          doc.select("label").get(1).text() shouldBe MessageLookup.Resident.no
        }

        s"has a value of ${MessageLookup.Resident.no}" in {
          doc.select("input").get(1).attr("value") shouldBe MessageLookup.Resident.no
        }
      }

      "have a button" which {
        lazy val button = doc.select("button")

        s"has the text ${MessageLookup.Resident.continue}" in {
          button.text() shouldBe MessageLookup.Resident.continue
        }

        "has the type 'submit'" in {
          button.attr("id") shouldBe "submit"
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
      lazy val view = saUserView(form)(fakeRequest, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select(".govuk-error-summary__body").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.getElementsByClass("govuk-error-summary").size shouldBe 1
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = saUserView.f(SaUserForm.saUserForm)(fakeRequest, mockMessage)

      val render = saUserView.render(SaUserForm.saUserForm, fakeRequest, mockMessage)

      f shouldBe render
    }
  }
}
