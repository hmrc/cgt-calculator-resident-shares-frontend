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

package views.calculation.deductions

import assets.MessageLookup.{AnnualExemptAmount => messages, Resident => commonMessages}
import common.resident.JourneyKeys
import controllers.helpers.FakeRequestHelper
import forms.AnnualExemptAmountForm._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{deductions => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class AnnualExemptAmountViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {
  "The Annual Exempt Amount view" should {
    lazy val postAction = controllers.routes.DeductionsController.submitAnnualExemptAmount()
    lazy val backLink = Some(controllers.routes.DeductionsController.lossesBroughtForward().toString)
    lazy val homeLink = controllers.routes.GainController.disposalDate().url
    lazy val view = views.annualExemptAmount(annualExemptAmountForm(), backLink, postAction, homeLink, JourneyKeys.properties,
      "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)
    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }
    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    "have a dynamic navTitle of navTitle" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe "navTitle"
    }

    "have a home link that" should {
      lazy val homeLink = doc.select("a#homeNavHref")
      "has the text 'Home'" in {
        homeLink.text() shouldBe "Home"
      }
      "has a link to the resident properties disposal date page" in {
        homeLink.attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }
    }

    "have a back button that" should {
      lazy val backLink = doc.select("a#back-link")
      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }
      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }
      "have a link to Brought Forward Losses" in {
        backLink.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForward().toString
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
      s"has the action '${controllers.routes.DeductionsController.submitAnnualExemptAmount().toString}'" in {
        form.attr("action") shouldBe controllers.routes.DeductionsController.submitAnnualExemptAmount().toString
      }
      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }

      s"have a legend for an input with text ${messages.question}" in {
        doc.body.getElementsByClass("heading-large").text() shouldEqual messages.question
      }


      "has help one text that" should {
        s"have the text ${messages.help}" in {
          doc.body.getElementsByClass("form-hint").text contains messages.help
        }
      }

      "has help two text that" should {
        s"have the text ${messages.helpOne}" in {
          doc.body.getElementsByClass("form-hint").text contains messages.helpOne
        }
      }

      s"the Annual Exempt Amount Help link ${messages.helpLinkOne} should " +
        "have the address Some(https://www.gov.uk/capital-gains-tax/losses)" in {
        doc.select("a#annualExemptAmountLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/allowances"
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

  "Annual Exempt Amount view with stored values" should {
    lazy val postAction = controllers.routes.DeductionsController.submitAnnualExemptAmount()
    lazy val backLink = Some(controllers.routes.DeductionsController.lossesBroughtForwardValue().toString)
    lazy val form = annualExemptAmountForm().bind(Map(("amount", "1000")))
    lazy val homeLink = controllers.routes.GainController.disposalDate().url
    lazy val view = views.annualExemptAmount(form, backLink, postAction, homeLink, JourneyKeys.properties,
      "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a home link that" should {
      lazy val homeLink = doc.select("a#homeNavHref")
      "has the text 'Home'" in {
        homeLink.text() shouldBe "Home"
      }
      "has a link to the resident shares disposal date page" in {
        homeLink.attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }
    }

    "have the value of 1000 auto-filled in the input" in {
      lazy val input = doc.body.getElementsByTag("input")
      input.`val` shouldBe "1000"
    }

    "have a back button that" should {
      lazy val backLink = doc.select("a#back-link")
      "have the correct back link text" in {
        backLink.text shouldBe commonMessages.back
      }
      "have the back-link class" in {
        backLink.hasClass("back-link") shouldBe true
      }
      "have a link to Brought Forward Losses Value" in {
        backLink.attr("href") shouldBe controllers.routes.DeductionsController.lossesBroughtForwardValue().toString
      }
    }
  }

  "Annual Exempt Amount View with form with errors" which {
    "is due to mandatory field error" should {
      lazy val postAction = controllers.routes.DeductionsController.submitAnnualExemptAmount()
      lazy val backLink = Some(controllers.routes.DeductionsController.lossesBroughtForwardValue().toString)
      val form = annualExemptAmountForm().bind(Map("amount" -> ""))
      lazy val homeLink = controllers.routes.GainController.disposalDate().url
      lazy val view = views.annualExemptAmount(form, backLink, postAction, homeLink, JourneyKeys.properties,
        "navTitle")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)
      s"output an error summary with message '${commonMessages.mandatoryAmount}'" in {
        doc.body.getElementById("amount-error-summary").text should include(commonMessages.mandatoryAmount)
      }
      s"have the input error message '${commonMessages.invalidAmount}'" in {
        doc.body.getElementsByClass("error-notification").text should include (commonMessages.mandatoryAmount)
      }
    }
  }
}
