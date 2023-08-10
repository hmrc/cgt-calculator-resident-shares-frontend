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

package views.calculation.gain

import assets.MessageLookup.{Resident => commonMessages, SharesAcquisitionCosts => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionCostsForm._
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.acquisitionCosts

class AcquisitionCostsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val acquisitionCostsView = fakeApplication.injector.instanceOf[acquisitionCosts]

  "Acquisition Costs shares view" should {

    lazy val view = acquisitionCostsView(acquisitionCostsForm, Some("back-link"))(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "have a link with href 'back-link'" in {
        backLink.attr("href") shouldBe "back-link"
      }
    }

    "have a home link to 'home-link'" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.question}'" in {
        h1Tag.text shouldBe messages.question
      }

      "have the govuk-heading-xl class" in {
        h1Tag.hasClass("govuk-heading-xl") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitAcquisitionCosts.toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitAcquisitionCosts.toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question}" in {
          label.text should include(messages.question)
        }

        "have the class 'govuk-visually-hidden'" in {
          label.hasClass("govuk-label govuk-visually-hidden") shouldBe true
        }
      }

      "has help text that" should {

        s"have the text ${messages.helpText}" in {
          doc.body.getElementsByClass("govuk-body").text shouldBe messages.helpText
        }
      }

      "have a p tag" which {
        s"with the extra text ${messages.hintText}" in {
          form.select("p.govuk-inset-text").text shouldBe messages.hintText
        }
      }

      "has a numeric input field" which {

        lazy val input = doc.body.getElementsByTag("input")

        "has the id 'amount'" in {
          input.attr("id") shouldBe "amount"
        }

        "has the name 'amount'" in {
          input.attr("name") shouldBe "amount"
        }

        "is of type number" in {
          input.attr("type") shouldBe "text"
        }

      }
    }

    "have a continue button that" should {

      lazy val continueButton = doc.select("button#submit")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of type submit" in {
        continueButton.attr("id") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("govuk-button") shouldBe true
      }

    }

    "generate the same template when .render and .f are called" in {

      val render = acquisitionCostsView.render(acquisitionCostsForm, Some("back-link"), fakeRequest, mockMessage)

      val f = acquisitionCostsView.f(acquisitionCostsForm, Some("back-link"))(fakeRequest, mockMessage)


      f shouldBe render
    }
  }

  "Acquisition Costs shares view with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = acquisitionCostsForm.bind(Map("amount" -> ""))
      lazy val view = acquisitionCostsView(form,  Some("back-link"))(fakeRequest, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".govuk-error-message").size shouldBe 1
      }
    }
  }
}
