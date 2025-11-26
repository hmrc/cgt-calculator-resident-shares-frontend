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

import assets.MessageLookup.{Resident as commonMessages, SharesDisposalCosts as messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalCostsForm.*
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import util.helper.ViewBehaviours
import views.html.calculation.gain.disposalCosts

class DisposalCostsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with ViewBehaviours{
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val disposalCostsView = fakeApplication.injector.instanceOf[disposalCosts
  ]
  "Disposal Costs view" should {

    lazy val view = disposalCostsView(disposalCostsForm)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the correct page title" in {
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

      "have a link to Disposal Value" in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${messages.h1}'" in {
        h1Tag.text shouldBe messages.h1
      }

      "have the heading-large class" in {
        h1Tag.hasClass("govuk-heading-l") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitDisposalCosts.toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitDisposalCosts.toString
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

        s"has a p with the text ${messages.jointOwnership}" in {
          doc.getElementsByClass("govuk-inset-text").text shouldBe messages.jointOwnership
        }
      }

      "has help text that" should {

        s"have the text ${messages.helpText}" in {
          doc.body.getElementsByClass("govuk-body").text shouldBe messages.helpText
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

        "is of type text" in {
          input.attr("type") shouldBe "text"
        }
      }
    }

    "have a continue button that" should {

      lazy val continueButton = doc.getElementsByClass("govuk-button")

      s"have the button text '${commonMessages.continue}'" in {
        continueButton.text shouldBe commonMessages.continue
      }

      "be of id submit" in {
        continueButton.attr("id") shouldBe "submit"
      }

      "have the class 'button'" in {
        continueButton.hasClass("govuk-button") shouldBe true
      }

    }

    "generate the same template when .render and .f are called" in {

      val f = disposalCostsView.f(disposalCostsForm)(fakeRequest, mockMessage)

      val render = disposalCostsView.render(disposalCostsForm, fakeRequest, mockMessage)

      f shouldBe render
    }

  }

  "Disposal Costs View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = disposalCostsForm.bind(Map("amount" -> ""))
      lazy val view = disposalCostsView(form)(using fakeRequest, mockMessage)
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
