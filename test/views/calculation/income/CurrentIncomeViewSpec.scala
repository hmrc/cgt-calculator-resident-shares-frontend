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

package views.calculation.income

import assets.MessageLookup.{CurrentIncome => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.CurrentIncomeForm
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.income.currentIncome

class CurrentIncomeViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val currentIncomeView = fakeApplication.injector.instanceOf[currentIncome]
  val fakeLang: Lang = Lang("en")
  val injectedForm = fakeApplication.injector.instanceOf[CurrentIncomeForm]
  val currentIncomeForm = injectedForm("2022")

  "Current Income view" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
    lazy val backLink = controllers.routes.IncomeController.personalAllowance.toString
    lazy val view = currentIncomeView(currentIncomeForm, backLink, taxYearModel, false)(fakeRequest, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title("2015 to 2016")}" in {
      doc.title() shouldBe messages.title("2015 to 2016")
    }

    "have a back button" which {

      lazy val backLink = doc.select("a#back-link")

      "has the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }

      "has the back-link class" in {
        backLink.hasClass("govuk-back-link") shouldBe true
      }

      "has a link to Previous Taxable Gains" in {
        backLink.attr("href") shouldBe controllers.routes.IncomeController.personalAllowance.toString
      }
    }

    s"have the question of the page ${messages.question("2015 to 2016")}" in {
      doc.select("h1").text shouldEqual messages.question("2015 to 2016")
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.routes.IncomeController.submitCurrentIncome.toString}'" in {
        form.attr("action") shouldBe controllers.routes.IncomeController.submitCurrentIncome.toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }


      "has a label that" should {

        lazy val label = doc.body.getElementsByTag("label")

        s"have the question ${messages.question("2015 to 2016")}" in {
          label.text should include(messages.question("2015 to 2016"))
        }

        "have the class" in {
          label.hasAttr("class") shouldBe true
        }

        "have the class that contains govuk-visually-hidden" in {
          doc.getElementsByClass("govuk-visually-hidden").size shouldEqual 2
        }
      }

      s"have the help text ${messages.helpText}" in {
        doc.body.getElementsByClass("govuk-hint").text shouldBe messages.helpTextShares
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

      "have a continue button that" should {

        lazy val continueButton = doc.getElementsByClass("govuk-button")

        s"have the button text '${commonMessages.continue}'" in {
          continueButton.text shouldBe commonMessages.continue
        }

        "be of type submit" in {
          continueButton.attr("id") shouldBe "submit"
        }
      }
    }

    "generate the same template when .render and .f are called" in {

      val f = currentIncomeView(currentIncomeForm, backLink, taxYearModel, false)(fakeRequest, mockMessage, fakeLang)

      val render = currentIncomeView.render(currentIncomeForm, backLink, taxYearModel, false, fakeRequest, mockMessage, fakeLang)

      f shouldBe render
    }
  }

  "The Current Income View with form with errors" which {

    "is due to mandatory field error" should {

      lazy val form = currentIncomeForm.bind(Map("amount" -> ""))
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val backLink = controllers.routes.DeductionsController.lossesBroughtForward.toString
      lazy val view = currentIncomeView(form, backLink, taxYearModel, false)(fakeRequest, mockMessage, fakeLang)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select(".govuk-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select(".govuk-error-message").size shouldBe 1
      }

      "have a back button" which {

        lazy val backLink = doc.getElementsByClass("govuk-back-link")

        "has the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "has the back-link id" in {
          backLink.attr("id") shouldBe "back-link"
        }

        "has a link to Annual Exempt Amount" in {
          backLink.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForward.toString
        }
      }
    }
  }
}
