/*
 * Copyright 2021 HM Revenue & Customs
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

import assets.MessageLookup.{SharesAcquisitionValue => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.AcquisitionValueForm._
import org.jsoup.Jsoup
import views.html.calculation.{gain => views}
import play.api.mvc.MessagesControllerComponents

class AcquisitionValueViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  "Acquisition Value view" should {

    lazy val view = views.acquisitionValue(acquisitionValueForm, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
    }

    s"have title ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    "have a back button that" should {

      lazy val backLink = doc.select("a#back-link")

      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "have a link to Disposal Costs" in {
        backLink.attr("href") shouldBe controllers.routes.GainController.didYouInheritThem().url
      }
    }

    "have a H1 tag that" should {
      lazy val heading = doc.select("h1")
      lazy val form = doc.select("form")

      s"have the page heading '${messages.title}'" in {
        heading.text shouldBe messages.title
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }

      "have a p tag" which {
        s"with the extra text ${messages.hintText}" in {
          form.select("p.panel-indent").text shouldBe messages.hintText
        }
      }
    }


    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitAcquisitionValue().toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitAcquisitionValue().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.title}" in {
          label.text should include(messages.title)
        }

        "have the class 'visuallyhidden'" in {
          label.select("span").hasClass("visuallyhidden") shouldBe true
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
          input.attr("type") shouldBe "number"
        }

        "have a step value of '0.01'" in {
          input.attr("step") shouldBe "0.01"
        }
      }

      "has a continue button that" should {

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

      val f = views.acquisitionValue.f(acquisitionValueForm, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)

      val render = views.acquisitionValue.render(acquisitionValueForm, "home-link", fakeRequest, mockMessage, fakeApplication, mockConfig)

      f shouldBe render
    }
  }

  "Acquisition Value View with form with errors" should {
    lazy val form = acquisitionValueForm.bind(Map("amount" -> ""))
    lazy val view = views.acquisitionValue(form, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }
}
