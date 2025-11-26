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

import assets.MessageLookup.Resident.Shares.{WorthWhenSoldForLess => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WorthWhenSoldForLessForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.worthWhenSoldForLess

class WorthWhenSoldForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val worthWhenSoldForLessView = fakeApplication.injector.instanceOf[worthWhenSoldForLess]

  "The Shares Worth When Sold For Less View when supplied with an empty form" should {

    lazy val view = worthWhenSoldForLessView(worthWhenSoldForLessForm)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back link" which {

      lazy val backLink = doc.select(".govuk-back-link")

      s"should have the text ${commonMessages.back}" in {
        backLink.text shouldEqual commonMessages.back
      }

      "should link to the Did you sell it for less than it was worth page." in {
        backLink.attr("href") shouldBe "#"
      }
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.h1}'" in {
        heading.text shouldBe messages.h1
      }

      "have the govuk-heading-xl class" in {
        heading.hasClass("govuk-heading-l") shouldEqual true
      }
    }

    "have a form that" should {

      lazy val form = doc.select("form")

      "have the action /calculate-your-capital-gains/resident/shares/worth-when-sold-for-less" in {
        form.attr("action") shouldEqual "/calculate-your-capital-gains/resident/shares/worth-when-sold-for-less"
      }

      "have the method POST" in {
        form.attr("method") shouldEqual "POST"
      }

      "have an input for the amount" which {

        lazy val input = doc.select("#amount")

        "has a label" which {

          lazy val label = doc.select("label")

          s"has the text ${messages.question}" in {
            label.select(".govuk-label--m").text() shouldEqual messages.question
          }

          "is tied to the input field" in {
            label.attr("for") shouldEqual "amount"
          }

          s"have a legend for an input with text ${messages.question}" in {
            doc.body.getElementsByClass("govuk-label--m").text() shouldEqual messages.question
          }
        }

        s"has a p tag with the text ${messages.informationText}" in {
          doc.getElementById("information").text shouldBe messages.informationText
        }

        s"has a p taf with the text ${messages.jointOwnershipText}" in {
          doc.select("p.govuk-inset-text").text shouldBe messages.jointOwnershipText
        }

        "renders in input tags" in {
          input.is("input") shouldEqual true
        }

        "has the field name as 'amount' to bind correctly to the form" in {

        }
      }

      "has a continue button" which {

        lazy val button = doc.select(".govuk-button")

        "renders as button tags" in {
          button.is("button") shouldEqual true
        }

        "has id equal to 'submit'" in {
          button.attr("id") shouldEqual "submit"
        }

        "has class of button" in {
          button.hasClass("govuk-button") shouldEqual true
        }

        s"has the text ${commonMessages.continue}" in {
          button.text() shouldEqual commonMessages.continue
        }
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = worthWhenSoldForLessView.f(worthWhenSoldForLessForm)(fakeRequest, mockMessage)

      val render = worthWhenSoldForLessView.render(worthWhenSoldForLessForm, fakeRequest, mockMessage)

      f shouldBe render
    }
  }

  "The Shares Worth When Sold For Less View when supplied with a correct form" should {

    lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "100"))
    lazy val view = worthWhenSoldForLessView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form in the input" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "The Shares Worth When Sold For Less View when supplied with an incorrect form" should {

    lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "adsa"))
    lazy val view = worthWhenSoldForLessView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select(".govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".govuk-error-message").size shouldBe 1
    }
  }
}
