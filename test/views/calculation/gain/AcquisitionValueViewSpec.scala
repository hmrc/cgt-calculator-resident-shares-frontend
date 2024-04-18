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

package views.calculation.gain

import assets.MessageLookup.{Resident => commonMessages, SharesAcquisitionValue => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionValueForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.acquisitionValue

class AcquisitionValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val acquisitionValueView = fakeApplication.injector.instanceOf[acquisitionValue]

  "Acquisition Value view" should {

    lazy val view = acquisitionValueView(acquisitionValueForm)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }

    s"have title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select(".govuk-back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link to Disposal Costs" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")
      lazy val form = doc.select("form")

      s"have the page heading '${messages.h1}'" in {
        heading.text should include(messages.h1)
      }

      "have the govuk-heading-xl class" in {
        heading.hasClass("govuk-heading-xl") shouldBe true
      }

      "have a p tag" which {
        s"with the extra text ${messages.hintText}" in {
          doc.getElementsByClass("govuk-inset-text").text shouldBe messages.hintText
        }
      }
    }


    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitAcquisitionValue.toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitAcquisitionValue.toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question}" in {
          label.text should include(messages.question)
        }

        s"have a legend for an input with text ${messages.question}" in {
          doc.body.getElementsByClass("govuk-label--m").text() shouldEqual messages.question
        }
      }

      "has a numeric input field that" should {

        lazy val input = doc.body.getElementsByTag("input")

        "have the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }

        "have the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }

        "have a type of number" in {
          input.attr("type") shouldBe "text"
        }

      }

      "has a continue button that" should {

        lazy val continueButton = doc.getElementsByClass("govuk-button")

        s"have the button text '${commonMessages.continue}'" in {
          continueButton.text shouldBe commonMessages.continue
        }

        "have id submit" in {
          continueButton.attr("id") shouldBe "submit"
        }

        "have the class 'govuk-button'" in {
          continueButton.hasClass("govuk-button") shouldBe true
        }
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = acquisitionValueView.f(acquisitionValueForm)(fakeRequest, mockMessage)

      val render = acquisitionValueView.render(acquisitionValueForm, fakeRequest, mockMessage)

      f shouldBe render
    }
  }

  "Acquisition Value View with form with errors" should {
    lazy val form = acquisitionValueForm.bind(Map("amount" -> ""))
    lazy val view = acquisitionValueView(form)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.getElementsByClass("govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.getElementsByClass("govuk-list govuk-error-summary__list").size shouldBe 1
    }
  }
}
