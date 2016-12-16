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

package views.calculation.income

import assets.DateAsset
import assets.MessageLookup.{PersonalAllowance => messages, Resident => commonMessages}
import common.Dates
import common.resident.JourneyKeys
import controllers.helpers.FakeRequestHelper
import forms.PersonalAllowanceForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{income => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PersonalAllowanceViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  val postAction = controllers.routes.IncomeController.submitPersonalAllowance()

  "Personal Allowance view" when {

    "supplied with a 2015/16 tax year" should {

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.personalAllowance(personalAllowanceForm(), taxYearModel, BigDecimal(10600), "home", postAction,
        Some("back-link"), JourneyKeys.shares, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.question("2015/16")}" in {
        doc.title() shouldBe messages.question("2015/16")
      }

      "have a dynamic navTitle of navTitle" in {
        doc.select("span.header__menu__proposition-name").text() shouldBe "navTitle"
      }

      "have a back button that" should {
        lazy val backLink = doc.select("a#back-link")
        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the back-link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "have a link to Current Income" in {
          backLink.attr("href") shouldBe "back-link"
        }
      }

      "have a home link to the shares disposal date" in {
        doc.select("#homeNavHref").attr("href") shouldEqual "home"
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

        s"has the action '${controllers.routes.IncomeController.submitPersonalAllowance().toString}'" in {
          form.attr("action") shouldBe controllers.routes.IncomeController.submitPersonalAllowance().toString
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"have a legend for an input with text ${messages.question("2015/16")}" in {
          doc.body.getElementsByClass("heading-large").text() shouldEqual messages.question("2015/16")
        }


        "has help text that" should {
          s"have the text ${messages.help("10,600")}" in {
            doc.body().select("div.form-group span.form-hint").text() shouldBe messages.help("10,600")
          }
        }


      }

      s"the Personal Allowance Help link ${messages.helpLinkOne} should " +
        "have the address Some(https://www.gov.uk/income-tax-rates/current-rates-and-allowances)" in {
        doc.select("a#personalAllowanceLink").attr("href") shouldEqual "https://www.gov.uk/income-tax-rates/current-rates-and-allowances"
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
        "has a step value of '1'" in {
          input.attr("step") shouldBe "1"
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


      "Personal Allowance view with stored values" should {
        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        lazy val form = personalAllowanceForm().bind(Map(("amount", "1000")))
        lazy val view = views.personalAllowance(form, taxYearModel, BigDecimal(10600), "home", postAction,
          Some("back-link"), JourneyKeys.shares, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, applicationMessages)
        lazy val doc = Jsoup.parse(view.body)

        "have the value of 1000 auto-filled in the input" in {
          lazy val input = doc.body.getElementsByTag("input")
          input.`val` shouldBe "1000"
        }
      }
    }

    "supplied with the current tax year" should {

      lazy val taxYearModel = TaxYearModel(Dates.getCurrentTaxYear, true, Dates.getCurrentTaxYear)
      lazy val view = views.personalAllowance(personalAllowanceForm(), taxYearModel, BigDecimal(11000), "home", postAction,
        Some("back-link"), JourneyKeys.shares, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      s"have a title ${messages.inYearQuestion}" in {
        doc.title() shouldBe messages.inYearQuestion
      }

      s"have the page heading '${messages.inYearQuestion}'" in {
        h1Tag.text shouldBe messages.inYearQuestion
      }

      s"have a legend for an input with text ${messages.inYearQuestion}" in {
        doc.body.getElementsByClass("heading-large").text() shouldEqual messages.inYearQuestion
      }

      s"have the help text '${messages.inYearHelp("11,000")}'" in {
        doc.body().select("div.form-group span.form-hint").text() shouldBe messages.inYearHelp("11,000")
      }
    }

    "supplied with a tax year a year after the current tax year" should {

      lazy val taxYearModel = TaxYearModel(DateAsset.getYearAfterCurrentTaxYear, false, Dates.getCurrentTaxYear)
      lazy val view = views.personalAllowance(personalAllowanceForm(), taxYearModel, BigDecimal(11000), "home", postAction,
        Some("back-link"), JourneyKeys.shares, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)
      lazy val h1Tag = doc.select("H1")

      s"have a title ${messages.question("2017/18")}" in {
        doc.title() shouldBe messages.question("2017/18")
      }

      s"have the page heading '${messages.question("2017/18")}'" in {
        h1Tag.text shouldBe messages.question("2017/18")
      }

      s"have a legend for an input with text ${messages.question("2017/18")}" in {
        doc.body.getElementsByClass("heading-large").text() shouldEqual messages.question("2017/18")
      }

      s"have the help text '${messages.help("11,000")}'" in {
        doc.body().select("div.form-group span.form-hint").text() shouldBe messages.help("11,000")
      }
    }

    "Personal Allowance View with form with errors" which {

      "is due to mandatory field error" should {

        lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
        val form = personalAllowanceForm().bind(Map("amount" -> ""))
        lazy val view = views.personalAllowance(form, taxYearModel, BigDecimal(11000), "home", postAction,
          Some("back-link"), JourneyKeys.shares, "navTitle", Dates.getCurrentTaxYear)(fakeRequest, applicationMessages)
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
}
