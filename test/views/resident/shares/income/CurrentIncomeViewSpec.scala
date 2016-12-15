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

package views.resident.shares.income

import assets.MessageLookup.{CurrentIncome => messages}
import assets.MessageLookup.{Resident => commonMessages}
import controllers.helpers.FakeRequestHelper
import forms.CurrentIncomeForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.shares.{income => views}

class CurrentIncomeViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Current Income view" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
    lazy val backLink = controllers.resident.shares.routes.IncomeController.previousTaxableGains().toString
    lazy val view = views.currentIncome(currentIncomeForm, backLink, taxYearModel, false)(fakeRequest)
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
        backLink.attr("href") shouldBe controllers.resident.shares.routes.IncomeController.previousTaxableGains().toString
      }
    }

    s"have the question of the page ${messages.question("2015/16")}" in {
      doc.select("h1").text shouldEqual messages.question("2015/16")
    }

    "have a form" which {

      lazy val form = doc.getElementsByTag("form")

      s"has the action '${controllers.resident.shares.routes.IncomeController.submitCurrentIncome().toString}'" in {
        form.attr("action") shouldBe controllers.resident.shares.routes.IncomeController.submitCurrentIncome().toString
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

      "have a sidebar that" should {

        lazy val sidebar = doc.select("a#incomeTaxLink")

        "have the address https://www.gov.uk/income-tax" in {
          sidebar.attr("href") shouldEqual "https://www.gov.uk/income-tax"
        }

        s"have the text ${messages.linkText}" in {
          sidebar.text() should include(messages.linkText)
        }

        s"have a span with the text ${commonMessages.externalLink}" in {
          sidebar.select("span").text() shouldEqual commonMessages.externalLink
        }

        "has the properties to open a link in a new tab" which {

          "has the external link class" in {
            sidebar.hasClass("external-link") shouldEqual true
          }

          "has the attribute rel" in {
            sidebar.hasAttr("rel") shouldEqual true
          }

          "rel has the value of external" in {
            sidebar.attr("rel") shouldEqual "external"
          }

          "has a target attribute" in {
            sidebar.hasAttr("target") shouldEqual true
          }

          "has a target value of _blank" in {
            sidebar.attr("target") shouldEqual "_blank"
          }
        }

        "record GA statistics" which {
          "has a data-journey-click attribute" in {
            sidebar.hasAttr("data-journey-click") shouldEqual true
          }

          "with the GA value of help:govUK:rtt-shares-incomeTax" in {
            sidebar.attr("data-journey-click") shouldEqual "help:govUK:rtt-shares-incomeTax"
          }
        }
      }
    }
  }

  "The Current Income View with form with errors" which {

    "is due to mandatory field error" should {

      val form = currentIncomeForm.bind(Map("amount" -> ""))
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val backLink = controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString
      lazy val view = views.currentIncome(form, backLink, taxYearModel, false)(fakeRequest)
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
          backLink.attr("href") shouldBe controllers.resident.shares.routes.DeductionsController.annualExemptAmount().toString
        }
      }
    }
  }
}
