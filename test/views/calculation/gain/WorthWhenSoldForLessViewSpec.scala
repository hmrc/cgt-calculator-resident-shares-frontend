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

package views.calculation.gain

import assets.MessageLookup.Resident.Shares.{WorthWhenSoldForLess => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.WorthWhenSoldForLessForm._
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.worthWhenSoldForLess

class WorthWhenSoldForLessViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val worthWhenSoldForLessView = fakeApplication.injector.instanceOf[worthWhenSoldForLess]

  "The Shares Worth When Sold For Less View when supplied with an empty form" should {

    lazy val view = worthWhenSoldForLessView(worthWhenSoldForLessForm, "home-link")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.question}" in {
      doc.title shouldBe messages.question
    }

    "have a back link" which {

      lazy val backLink = doc.select("#back-link")

      s"should have the text ${commonMessages.back}" in {
        backLink.text shouldEqual commonMessages.back
      }

      "should link to the Did you sell it for less than it was worth page." in {
        backLink.attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/sell-for-less"
      }
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.question}'" in {
        heading.text shouldBe messages.question
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldEqual true
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
            label.select("span.visuallyhidden").text() shouldEqual messages.question
          }

          "has the class visually hidden" in {
            label.select("span.visuallyhidden").hasClass("visuallyhidden") shouldEqual true
          }

          "is tied to the input field" in {
            label.attr("for") shouldEqual "amount"
          }
        }

        s"has a p tag with the text ${messages.informationText}" in {
          doc.getElementById("information").text shouldBe messages.informationText
        }

        s"has a p taf with the text ${messages.jointOwnershipText}" in {
          doc.select("p.panel-indent").text shouldBe messages.jointOwnershipText
        }

        "renders in input tags" in {
          input.is("input") shouldEqual true
        }

        "has the field name as 'amount' to bind correctly to the form" in {

        }
      }

      "has a continue button" which {

        lazy val button = doc.select("#continue-button")

        "renders as button tags" in {
          button.is("button") shouldEqual true
        }

        "has type equal to 'submit'" in {
          button.attr("type") shouldEqual "submit"
        }

        "has class of button" in {
          button.hasClass("button") shouldEqual true
        }

        s"has the text ${commonMessages.continue}" in {
          button.text() shouldEqual commonMessages.continue
        }
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = worthWhenSoldForLessView.f(worthWhenSoldForLessForm, "home-link")(fakeRequest, mockMessage)

      val render = worthWhenSoldForLessView.render(worthWhenSoldForLessForm, "home-link", fakeRequest, mockMessage)

      f shouldBe render
    }
  }

  "The Shares Worth When Sold For Less View when supplied with a correct form" should {

    lazy val form = worthWhenSoldForLessForm.bind(Map("amount" -> "100"))
    lazy val view = worthWhenSoldForLessView(form, "home-link")(fakeRequest, mockMessage)
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
    lazy val view = worthWhenSoldForLessView(form, "home-link")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
