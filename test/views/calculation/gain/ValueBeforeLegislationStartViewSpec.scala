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

import assets.MessageLookup.Resident.Shares.{ValueBeforeLegislationStart => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.ValueBeforeLegislationStartForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.valueBeforeLegislationStart

class ValueBeforeLegislationStartViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val valueBeforeLegislationStartView = fakeApplication.injector.instanceOf[valueBeforeLegislationStart]

  "ValueBeforeLegislationStart View" should {

    lazy val view = valueBeforeLegislationStartView(valueBeforeLegislationStartForm)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have the title of the page ${messages.title}" in {
      doc.title shouldEqual messages.title
    }

    s"have a back link to the owner before April 1982 page" in {
      doc.select(".govuk-back-link").attr("href") shouldEqual "#"
    }

    "have a heading that" should {

      lazy val heading = doc.select("H1")

      s"have the correct text" in {
        heading.text shouldBe messages.h1
      }

      "have the govuk-heading-xl class" in {
        heading.hasClass("govuk-heading-xl") shouldEqual true
      }
    }


    "have a form that" should {

      lazy val form = doc.select("form")

      "have the the correct action" in {
        form.attr("action") shouldEqual controllers.routes.GainController.submitValueBeforeLegislationStart.toString
      }

      "have the method POST" in {
        form.attr("method") shouldEqual "POST"
      }

      s"have a legend for an input with text ${messages.question}" in {
        doc.body.getElementsByClass("govuk-label--m").text() shouldEqual messages.question
      }

      "have an input for the amount" which {

        lazy val input = doc.select("#amount")

        "has a label" which {

          lazy val label = doc.body.getElementsByTag("label")

          s"has the correct text" in {
            label.text shouldEqual messages.question
          }

          "is tied to the input field" in {
            label.attr("for") shouldEqual "amount"
          }
        }

        "has help text that" should {

          s"have the text ${messages.helpText}" in {
            doc.select("#main-content > div > div > p:nth-child(3)").text shouldBe messages.helpText
          }
        }

        "have a p tag" which {
          s"with the extra text ${messages.hintText}" in {
            doc.body.getElementsByClass("govuk-inset-text").text shouldBe messages.hintText
          }
        }

        "renders in input tags" in {
          input.is("input") shouldEqual true
        }

        "has the field name as 'amount' to bind correctly to the form" in {

        }
      }

      "has a continue button" which {

        lazy val button = doc.getElementsByClass("govuk-button")

        "has id equal to 'submit'" in {
          button.attr("id") shouldEqual "submit"
        }

        "has class of 'govuk-button'" in {
          button.hasClass("govuk-button") shouldEqual true
        }

        s"has the text ${commonMessages.continue}" in {
          button.text() shouldEqual commonMessages.continue
        }
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = valueBeforeLegislationStartView.f(valueBeforeLegislationStartForm)(fakeRequest, mockMessage)

      val render = valueBeforeLegislationStartView.render(valueBeforeLegislationStartForm, fakeRequest, mockMessage)

      f shouldBe render
    }
  }

  "ValueBeforeLegislationStart View with form without errors" should {

    lazy val form = valueBeforeLegislationStartForm.bind(Map("amount" -> "100"))
    lazy val view = valueBeforeLegislationStartView(form)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }
  }

  "ValueBeforeLegislationStart View with form with errors" should {

    lazy val form = valueBeforeLegislationStartForm.bind(Map("amount" -> ""))
    lazy val view = valueBeforeLegislationStartView(form)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.getElementsByClass("govuk-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.getElementsByClass("govuk-list govuk-error-summary__list").size shouldBe 1
    }
  }
}
