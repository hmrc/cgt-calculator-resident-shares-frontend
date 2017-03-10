/*
 * Copyright 2017 HM Revenue & Customs
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

import assets.MessageLookup.{PreviousTaxableGains => messages, Resident => commonMessages}
import common.resident.JourneyKeys
import controllers.helpers.FakeRequestHelper
import forms.PreviousTaxableGainsForm.previousTaxableGainsForm
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{deductions => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class PreviousTaxableGainsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Previous taxable gains view" should {

    lazy val postAction = controllers.routes.IncomeController.previousTaxableGains()
    lazy val homeLink = controllers.routes.GainController.disposalDate().url
    lazy val view = views.previousTaxableGains(previousTaxableGainsForm, "#", postAction, homeLink, JourneyKeys.shares,
      "2016/17", "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset.toString shouldBe "UTF-8"
    }

    s"have a title of ${messages.title("2016/17")}" in {
      doc.title shouldBe messages.title("2016/17")
    }

    "have a dynamic navTitle of navTitle" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe "navTitle"
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("h1")

      s"have the page heading '${messages.title("2016/17")}'" in {
        heading.text shouldBe messages.title("2016/17")
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }
    }

    "have a home link that" should {
      lazy val homeLink = doc.select("a#homeNavHref")
      "has the text 'Home'" in {
        homeLink.text() shouldBe "Home"
      }
      "has a link to the resident shares disposal date page" in {
        homeLink.attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }
    }

    "have the correct back link" in {
      lazy val link = doc.select("#back-link")
      link.attr("href") shouldBe "#"
    }

    "have the correct label" in {
      lazy val label = doc.select("label")
      label.text should startWith(messages.question("2016/17"))
    }

    "have a hidden label" in {
      lazy val label = doc.select("label > span")
      label.hasClass("visuallyhidden") shouldBe true
    }

    "have a sidebar" in {
      lazy val sidebar = doc.select("aside")
      sidebar.hasClass("sidebar") shouldBe true
    }

    "have an external link" which {
      lazy val link = doc.select("a#helpLink1")

      s"has the text ${messages.helpLinkOne}" in {
        link.text() should include(messages.helpLinkOne)
      }

      s"has a visually hidden external link message" in {
        link.select("span#opensInANewTab").text() shouldBe commonMessages.externalLink
      }

      "links to https://www.gov.uk/capital-gains-tax/work-out-need-to-pay" in {
        link.attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/work-out-need-to-pay"
      }
    }

    "not display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "not display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }

    "have a form with an action for shares" in {
      doc.body.select("form").attr("action") shouldBe controllers.routes.IncomeController.previousTaxableGains().url
    }
  }

  "Previous taxable gains view with form without errors" should {

    lazy val form = previousTaxableGainsForm.bind(Map("amount" -> "100"))
    lazy val postAction = controllers.routes.IncomeController.previousTaxableGains()
    lazy val homeLink = controllers.routes.GainController.disposalDate().url
    lazy val view = views.previousTaxableGains(form, "#", postAction, homeLink, JourneyKeys.shares,
      "2016/17", "navTitle")(fakeRequest, applicationMessages)
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

    "display the value of the form" in {
      doc.body.select("#amount").attr("value") shouldEqual "100"
    }

    "display no error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 0
    }

    "display no error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 0
    }

    "have a form with an action for shares" in {
      doc.body.select("form").attr("action") shouldBe controllers.routes.IncomeController.previousTaxableGains().url
    }
  }

  "Previous taxable gains view with form with errors" should {

    lazy val form = previousTaxableGainsForm.bind(Map("amount" -> ""))
    lazy val postAction = controllers.routes.IncomeController.previousTaxableGains()
    lazy val homeLink = controllers.routes.GainController.disposalDate().url
    lazy val view = views.previousTaxableGains(form, "#", postAction, homeLink, JourneyKeys.shares,
      "2016/17", "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#amount-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select(".form-group .error-notification").size shouldBe 1
    }
  }

}
