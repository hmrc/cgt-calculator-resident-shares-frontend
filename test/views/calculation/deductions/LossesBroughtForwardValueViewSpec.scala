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

package views.calculation.deductions

import assets.MessageLookup.{LossesBroughtForwardValue => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.LossesBroughtForwardValueForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import views.html.calculation.deductions.lossesBroughtForwardValue
import play.api.mvc.MessagesControllerComponents

class LossesBroughtForwardValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val lossesBroughtForwardValueView = fakeApplication.injector.instanceOf[lossesBroughtForwardValue]

  "Losses Brought Forward Value view" when {

    "provided with a date in the 2015/16 tax year" should {

      lazy val taxYear = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = lossesBroughtForwardValueView(lossesBroughtForwardValueForm, taxYear, "back-link",
        "home-link", controllers.routes.DeductionsController.submitLossesBroughtForwardValue(), "navTitle")(fakeRequest, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title("2015/16")}" in {
        doc.title() shouldBe messages.title("2015/16")
      }

      "have a dynamic navTitle of navTitle" in {
        doc.select("span.header__menu__proposition-name").text() shouldBe "navTitle"
      }

      "have a home link to 'home-link'" in {
        doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
      }

      "have a back button that" should {

        lazy val backLink = doc.select("a#back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "have a link to 'back-link'" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }

      "have a H1 tag that" should {

        lazy val h1Tag = doc.select("H1")

        s"have the page heading '${messages.question("2015/16")}'" in {
          h1Tag.text shouldBe messages.question("2015/16")
        }

        "have the heading-large class" in {
          h1Tag.hasClass("heading-large") shouldBe true
        }
      }

      "have a form" which {
        lazy val form = doc.getElementsByTag("form")

        s"has the action '${controllers.routes.DeductionsController.submitLossesBroughtForwardValue().url}'" in {
          form.attr("action") shouldBe controllers.routes.DeductionsController.submitLossesBroughtForwardValue().url
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        "has a label that" should {

          lazy val label = doc.body.getElementsByTag("label")

          s"have the question ${messages.question("2015/16")}" in {
            label.text should include(messages.question("2015/16"))
          }

          "have the class 'visuallyhidden'" in {
            label.select("span.visuallyhidden").size shouldBe 1
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
            input.attr("type") shouldBe "number"
          }

          "has a step value of '0.01'" in {
            input.attr("step") shouldBe "0.01"
          }
        }

        "have a continue button that" should {

          lazy val continueButton = doc.select("button#continue-button")

          s"have the button text '${commonMessages.continue}'" in {
            continueButton.text shouldBe commonMessages.continue
          }

          "be of type submit" in {
            continueButton.attr("type") shouldBe "submit"
          }

          "have the class 'button'" in {
            continueButton.hasClass("button") shouldBe true
          }
        }
      }

      "generate the same template when .render and .f are called" in {

        val f = lossesBroughtForwardValueView.f(lossesBroughtForwardValueForm, taxYear, "back-link",
          "home-link", controllers.routes.DeductionsController.submitLossesBroughtForwardValue(), "navTitle")(fakeRequest,
          mockMessage)

        val render = lossesBroughtForwardValueView.render(lossesBroughtForwardValueForm, taxYear, "back-link",
          "home-link", controllers.routes.DeductionsController.submitLossesBroughtForwardValue(), "navTitle",
          fakeRequest, mockMessage)

        f shouldBe render
      }
    }

    "provided with a date in the 2014/15 tax year" should {

      lazy val taxYear = TaxYearModel("2014/15", false, "2015/16")
      lazy val view = lossesBroughtForwardValueView(lossesBroughtForwardValueForm, taxYear, "back-link",
        "home-link", controllers.routes.DeductionsController.submitLossesBroughtForwardValue(), "navTitle")(fakeRequest, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${messages.title("2014/15")}" in {
        doc.title() shouldBe messages.title("2014/15")
      }

      "have a H1 tag that" should {

        lazy val h1Tag = doc.select("H1")

        s"have the page heading '${messages.question("2014/15")}'" in {
          h1Tag.text shouldBe messages.question("2014/15")
        }

        "have the heading-large class" in {
          h1Tag.hasClass("heading-large") shouldBe true
        }
      }

      "have a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question("2014/15")}" in {
          label.text should include(messages.question("2014/15"))
        }

        "have the class 'visuallyhidden'" in {
          label.select("span.visuallyhidden").size shouldBe 1
        }
      }
    }
  }

  "Losses Brought Forward Value view with stored values" should {
    lazy val form = lossesBroughtForwardValueForm.bind(Map(("amount", "1000")))
    lazy val taxYear = TaxYearModel("2015/16", true, "2015/16")
    lazy val view = lossesBroughtForwardValueView(form, taxYear, "back-link",
      "home-link", controllers.routes.DeductionsController.submitLossesBroughtForwardValue(), "navTitle")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have the value of 1000 auto-filled in the input" in {
      lazy val input = doc.body.getElementsByTag("input")
      input.`val` shouldBe "1000"
    }
  }

  "Losses Brought Forward Value view with errors" should {
    lazy val form = lossesBroughtForwardValueForm.bind(Map(("amount", "")))
    lazy val taxYear = TaxYearModel("2015/16", true, "2015/16")
    lazy val view = lossesBroughtForwardValueView(form, taxYear, "back-link",
      "home-link", controllers.routes.DeductionsController.submitLossesBroughtForwardValue(), "navTitle")(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("span.error-notification").size shouldBe 1
    }
  }
}
