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

import assets.MessageLookup.Resident.Shares.{OwnerBeforeLegislationStart => Messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.OwnerBeforeLegislationStartForm._
import models.resident.shares.OwnerBeforeLegislationStartModel
import org.jsoup.Jsoup
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.gain.ownerBeforeLegislationStart

class OwnerBeforeLegislationStartViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val ownerBeforeLegislationStartView = fakeApplication.injector.instanceOf[ownerBeforeLegislationStart]

  "Owned Before 1982 view with an empty form" should {

    lazy val view = ownerBeforeLegislationStartView(ownerBeforeLegislationStartForm, Some("back-link"))(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)
    lazy val form = doc.getElementsByTag("form")

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title ${Messages.title}" in {
      doc.title shouldBe Messages.title
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("h1")

      s"have the page heading '${Messages.heading}'" in {
        h1Tag.text shouldBe Messages.heading
      }

      "have the govuk-fieldset__heading class" in {
        h1Tag.hasClass("govuk-fieldset__heading") shouldBe true
      }
    }

    s"have the home link to 'home'" in {
      doc.body.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }

    "have a back button" which {

      lazy val backLink = doc.body.getElementById("back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the govuk-back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "has a back link to 'back'" in {
        backLink.attr("href") shouldBe "back-link"
      }
    }

    "render a form tag with a submit action" in {
      doc.select("form").attr("action") shouldEqual controllers.routes.GainController.submitOwnerBeforeLegislationStart.toString
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }

    "have a legend for the radio inputs" which {

      lazy val legend = doc.select("legend")

      s"contain the text ${Messages.heading}" in {
        legend.text should include(s"${Messages.heading}")
      }
    }

    "have a set of radio inputs" which {

      "are surrounded in a div with class govuk-fieldset" in {
        doc.select("#main-content > div > div > form > div > fieldset").hasClass("govuk-fieldset") shouldEqual true
      }

      "for the option 'Yes'" should {

        lazy val YesRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(1)")

        "have a label with class 'govuk-radios__item'" in {
          YesRadioOption.hasClass("govuk-radios__item") shouldEqual true
        }

        "have the property 'for'" in {
          YesRadioOption.select("label").hasAttr("for") shouldEqual true
        }

        "the for attribute has the value ownerBeforeLegislationStart-Yes" in {
          YesRadioOption.select("label").attr("for") shouldEqual "ownerBeforeLegislationStart"
        }

        "have the text 'Yes'" in {
          YesRadioOption.select("label").text shouldEqual "Yes"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#ownerBeforeLegislationStart")

          "have the id 'ownerBeforeLegislationStart'" in {
            optionLabel.attr("id") shouldEqual "ownerBeforeLegislationStart"
          }

          "have the value 'Yes'" in {
            optionLabel.attr("value") shouldEqual "Yes"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }

      "for the option 'No'" should {

        lazy val NoRadioOption = doc.select("#main-content > div > div > form > div > fieldset > div > div:nth-child(2)")

        "have a label with class 'govuk-radios__item'" in {
          NoRadioOption.hasClass("govuk-radios__item") shouldEqual true
        }

        "have the property 'for'" in {
          NoRadioOption.select("label").hasAttr("for") shouldEqual true
        }

        "the for attribute has the value ownerBeforeLegislationStart-No" in {
          NoRadioOption.select("label").attr("for") shouldEqual "ownerBeforeLegislationStart-2"
        }

        "have the text 'No'" in {
          NoRadioOption.select("label").text shouldEqual "No"
        }

        "have an input under the label that" should {

          lazy val optionLabel = doc.select("#ownerBeforeLegislationStart-2")

          "have the id 'livedInProperty-No'" in {
            optionLabel.attr("id") shouldEqual "ownerBeforeLegislationStart-2"
          }

          "have the value 'No'" in {
            optionLabel.attr("value") shouldEqual "No"
          }

          "be of type radio" in {
            optionLabel.attr("type") shouldEqual "radio"
          }
        }
      }
    }

    "have a continue button" which {

      lazy val button = doc.select("#submit")

      "has class 'button'" in {
        button.hasClass("govuk-button") shouldEqual true
      }

      "has attribute id" in {
        button.hasAttr("id") shouldEqual true
      }

      "has id equal to continue-button" in {
        button.attr("id") shouldEqual "submit"
      }

      s"has the text ${commonMessages.continue}" in {
        button.text shouldEqual s"${commonMessages.continue}"
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = ownerBeforeLegislationStartView.f(ownerBeforeLegislationStartForm, Some("back-link"))(fakeRequest,
        mockMessage)

      val render = ownerBeforeLegislationStartView.render(ownerBeforeLegislationStartForm, Some("back-link"), fakeRequest,
        mockMessage)

      f shouldBe render
    }
  }

  "Owned Before 1982 view with form errors" should {

    lazy val form = ownerBeforeLegislationStartForm.bind(Map("ownerBeforeLegislationStart" -> ""))
    lazy val view = ownerBeforeLegislationStartView(form, Some("back"))(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have an error summary" which {
      "display an error summary message for the page" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".govuk-error-message").size shouldBe 1
      }
    }
  }
}
