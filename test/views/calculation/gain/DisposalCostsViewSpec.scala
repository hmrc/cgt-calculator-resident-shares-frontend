/*
 * Copyright 2016 HM Revenue & Customs
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

import assets.MessageLookup.{SharesDisposalCosts => messages}
import assets.MessageLookup.{Resident => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.DisposalCostsForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{gain => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class DisposalCostsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Disposal Costs view" should {

    lazy val view = views.disposalCosts(disposalCostsForm, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have the correct page title" in {
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

      "have a link to Disposal Value" in {
        backLink.attr("href") shouldBe controllers.routes.GainController.disposalValue().toString
      }
    }

    "have a H1 tag that" should {

      lazy val h1Tag = doc.select("H1")

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.title
      }

      "have the heading-large class" in {
        h1Tag.hasClass("heading-large") shouldBe true
      }
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.GainController.submitDisposalCosts().toString}'" in {
        form.attr("action") shouldBe controllers.routes.GainController.submitDisposalCosts().toString
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
          label.select("span.visuallyhidden").size shouldBe 1
        }
      }

      "has help text that" should {

        s"have the text ${messages.helpText}" in {
          doc.body.getElementsByClass("form-hint").text shouldBe messages.helpText
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

  "Disposal Costs View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = disposalCostsForm.bind(Map("amount" -> ""))
      lazy val view = views.disposalCosts(form, "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#amount-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }
    }
  }
}
