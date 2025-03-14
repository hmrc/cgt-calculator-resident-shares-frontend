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

import assets.DateAsset
import assets.MessageLookup.{PersonalAllowance => messages, Resident => commonMessages}
import common.resident.JourneyKeys
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import forms.PersonalAllowanceForm
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import play.api.i18n.{Lang, Messages}
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.income.personalAllowance

class PersonalAllowanceViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  private val postAction = controllers.routes.IncomeController.submitPersonalAllowance
  private implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  private val personalAllowanceView = fakeApplication.injector.instanceOf[personalAllowance]
  private val fakeLang = Lang("en")
  private val injectedForm = fakeApplication.injector.instanceOf[PersonalAllowanceForm]
  private val personalAllowanceForm = injectedForm(11000, "2022", fakeLang)

  "Personal Allowance view" when {

    "supplied with a 2015 to 2016 tax year" should {

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = personalAllowanceView(personalAllowanceForm, taxYearModel, BigDecimal(10600), postAction,
        Some("back-link"), JourneyKeys.shares, Dates.getCurrentTaxYear)(fakeRequest, mockMessage,fakeLang)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have a navTitle of Calculate your Capital Gains Tax" in {
        doc.getElementsByClass("govuk-header__link govuk-header__service-name").text() shouldBe "Calculate your Capital Gains Tax"
      }

      "have a back button that" should {
        lazy val backLink = doc.select(".govuk-back-link")
        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the back-link class" in {
          backLink.hasClass("govuk-back-link") shouldBe true
        }

        "have a link to Current Income" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a home link to the shares disposal date" in {
        doc.getElementsByClass("govuk-header__link govuk-header__service-name").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
      }

      s"have the page heading '${messages.h1}'" in {
        doc.select("h1").text shouldBe messages.h1
      }

      s"have the help text ${messages.help}" in {
        doc.getElementsByClass("govuk-body").get(0).text() shouldBe messages.help
      }

      s"have a list title of ${messages.listTitle("2015", "2016", "")}" in {
        doc.getElementsByClass("govuk-body").get(1).text() shouldBe messages.listTitle("2015", "2016", "£10,600")
      }

      s"have a list with the first entry of ${messages.listOne}" in {
        doc.select("li").get(2).text() shouldBe messages.listOne
      }

      s"have a list with the second entry of ${messages.listTwo}" in {
        doc.select("li").get(3).text() shouldBe messages.listTwo
      }

      "have a link" which {
        s"has the full text ${messages.linkText + " " + messages.link}" in {
          doc.getElementsByClass("govuk-body").get(2).text() shouldBe messages.linkText + " " + messages.link + "."
        }

        "has the href to the gov uk rates page" in {
          doc.getElementsByClass("govuk-link").get(1).attr("href") shouldBe "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
        }

        s"has the link text ${messages.link}" in {
          doc.getElementsByClass("govuk-link").get(1).text() shouldBe messages.link
        }
      }

      "have a form" which {
        lazy val form = doc.getElementsByTag("form")

        s"has the action '${controllers.routes.IncomeController.submitPersonalAllowance.url}'" in {
          form.attr("action") shouldBe controllers.routes.IncomeController.submitPersonalAllowance.url
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"have a legend for an input with text ${messages.title}" in {
          doc.body.getElementsByClass("govuk-heading-xl").text() shouldEqual messages.h1
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


      "Personal Allowance view with stored values" should {
        lazy val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")
        lazy val form = personalAllowanceForm.bind(Map(("amount", "1000")))
        lazy val view = personalAllowanceView(form, taxYearModel, BigDecimal(10600), postAction,
          Some("back-link"), JourneyKeys.shares, "2015 to 16")(fakeRequest, mockMessage, fakeLang)
        lazy val doc = Jsoup.parse(view.body)

        "have the value of 1000 auto-filled in the input" in {
          lazy val input = doc.body.getElementsByTag("input")
          input.`val` shouldBe "1000"
        }
      }

      "generate the same template when .render and .f are called" in {

        val f = personalAllowanceView.f(personalAllowanceForm, taxYearModel, BigDecimal(10600), postAction,
          Some("back-link"), JourneyKeys.shares, Dates.getCurrentTaxYear)(fakeRequest, mockMessage, fakeLang)

        val render = personalAllowanceView.render(personalAllowanceForm, taxYearModel, BigDecimal(10600), postAction,
          Some("back-link"), JourneyKeys.shares, Dates.getCurrentTaxYear, fakeRequest, mockMessage, fakeLang)

        f shouldBe render
      }

    }

    "supplied with the current tax year" should {

      lazy val taxYearModel = TaxYearModel(Dates.getCurrentTaxYear, true, Dates.getCurrentTaxYear)
      lazy val view = personalAllowanceView(personalAllowanceForm, taxYearModel, BigDecimal(11000), postAction,
        Some("back-link"), JourneyKeys.shares, Dates.getCurrentTaxYear)(fakeRequest, mockMessage, fakeLang)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

        s"have a title ${messages.title}" in {
          doc.title() shouldBe messages.title
        }

        s"have the page heading '${messages.h1}'" in {
          h1Tag.text shouldBe messages.h1
        }

        s"have a legend for an input with text ${messages.inYearQuestion}" in {
          doc.body.getElementsByClass("govuk-label--m").text() shouldEqual messages.inYearQuestion
        }
    }

    "supplied with a tax year a year after the current tax year" should {

      lazy val taxYearModel = TaxYearModel(DateAsset.getYearAfterCurrentTaxYear, false, Dates.getCurrentTaxYear)
      lazy val view = personalAllowanceView(personalAllowanceForm, taxYearModel, BigDecimal(11000), postAction,
        Some("back-link"), JourneyKeys.shares, Dates.getCurrentTaxYear)(fakeRequest, mockMessage, fakeLang)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      val nextTaxYear = DateAsset.getYearAfterCurrentTaxYear
      val splitYear = nextTaxYear.split("/")
      val nextTaxYearFormatted = splitYear(0) + " to " + splitYear(0).substring(0, 2) + splitYear(1)

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      s"have the page heading '${messages.title}'" in {
        h1Tag.text shouldBe messages.h1
      }

      s"have a legend for an input with text ${messages.question(s"$nextTaxYearFormatted")}" in {
        doc.body.getElementsByClass("govuk-label--m").text() shouldEqual messages.question(s"$nextTaxYearFormatted")
      }
    }

    "Personal Allowance View with form with errors" which {

      "is due to mandatory field error" should {

        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val form = personalAllowanceForm.bind(Map("amount" -> ""))
        lazy val view = personalAllowanceView(form, taxYearModel, BigDecimal(11000), postAction,
          Some("back-link"), JourneyKeys.shares, Dates.getCurrentTaxYear)(fakeRequest, mockMessage, fakeLang)
        lazy val doc = Jsoup.parse(view.body)

        "display an error summary message for the amount" in {
          doc.body.select("#main-content > div > div > div").size shouldBe 1
        }

        "display an error message for the input" in {
          doc.body.getElementsByClass("govuk-list govuk-error-summary__list").size shouldBe 1
        }
      }
    }
  }
}
