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

package views.calculation.income

import assets.MessageLookup.{CurrentIncome => messages}
import assets.MessageLookup.{Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CurrentIncomeForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import views.html.calculation.income.currentIncome
import play.api.mvc.MessagesControllerComponents

class CurrentIncomeViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val currentIncomeView = fakeApplication.injector.instanceOf[currentIncome]

  "Current Income view" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
    lazy val backLink = controllers.routes.IncomeController.personalAllowance().toString
    lazy val view = currentIncomeView(currentIncomeForm, backLink, taxYearModel, false)(fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title("2015/16")}" in {
      doc.title() shouldBe messages.title("2015/16")
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }

      "has a link to Previous Taxable Gains" in {
        backLink.attr("href") shouldBe controllers.routes.IncomeController.personalAllowance().toString
      }
    }

    s"have the question of the page ${messages.question("2015/16")}" in {
      doc.select("h1").text shouldEqual messages.question("2015/16")
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.IncomeController.submitCurrentIncome().toString}'" in {
        form.attr("action") shouldBe controllers.routes.IncomeController.submitCurrentIncome().toString
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

      s"have the help text ${messages.helpText}" in {
        doc.body.getElementsByClass("form-hint").text shouldBe messages.helpTextShares
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

      val f = currentIncomeView.f(currentIncomeForm, backLink, taxYearModel, false)(fakeRequest, mockMessage)

      val render = currentIncomeView.render(currentIncomeForm, backLink, taxYearModel, false, fakeRequest, mockMessage)

      f shouldBe render
    }
  }

  "The Current Income View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> ""))
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val backLink = controllers.routes.DeductionsController.lossesBroughtForward().toString
      lazy val view = currentIncomeView(form, backLink, taxYearModel, false)(fakeRequest, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#amount-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".form-group .error-notification").size shouldBe 1
      }

      "have a back button" which {

        lazy val backLink = doc.select("a#back-link")

        "has the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "has the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "has a link to Annual Exempt Amount" in {
          backLink.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForward().toString
        }
      }
    }
  }
}
