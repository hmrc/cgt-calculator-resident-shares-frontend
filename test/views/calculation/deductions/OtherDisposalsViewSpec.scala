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

import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.{OtherProperties => viewMessages}
import assets.MessageLookup.{SharesOtherDisposals => messages}
import controllers.helpers.FakeRequestHelper
import forms.OtherPropertiesForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{deductions => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class OtherDisposalsViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Other Disposals view" when {

    "using a disposal date 0f 2015/16" should {

      lazy val view = views.otherDisposals(otherPropertiesForm, TaxYearModel("2015/16", true, "2015/16"), "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      "have a home link to 'home-link'" in {
        doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
      }

      s"have a title of ${viewMessages.title("2015/16")}" in {
        doc.title() shouldBe viewMessages.title("2015/16")
      }

      s"have a question of ${viewMessages.pageHeading("2015/16")}" in {
        doc.select("h1").text shouldBe viewMessages.pageHeading("2015/16")
      }

      "have an input field with id hasOtherProperties-yes" in {
        doc.body.getElementById("hasOtherProperties-yes").tagName() shouldEqual "input"
      }

      "have an input field with id hasOtherProperties-no" in {
        doc.body.getElementById("hasOtherProperties-no").tagName() shouldEqual "input"
      }

      s"have the help text ${viewMessages.help}" in {
        doc.body.select("span.form-hint p").text shouldBe viewMessages.help
      }

      s"have the help text ${messages.helpOne}" in {
        doc.body.select("ul.list-bullet li").get(0).text shouldBe messages.helpOne
      }

      s"have the help text ${viewMessages.helpTwo}" in {
        doc.body.select("ul.list-bullet li").get(1).text shouldBe viewMessages.helpTwo
      }

      s"have the help text ${messages.helpThree}" in {
        doc.body.select("ul.list-bullet li").get(2).text shouldBe messages.helpThree
      }

      "have a continue button " in {
        doc.body.getElementById("continue-button").text shouldEqual commonMessages.continue
      }

      "have a back button that" should {

        lazy val backLink = doc.select("a#back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the correct back link class" in {
          backLink.hasClass("back-link") shouldBe true
        }

        "have a link to acquisition costs" in {
          backLink.attr("href") shouldBe controllers.routes.GainController.acquisitionCosts().toString()
        }
      }

      "have a form" which {

        lazy val form = doc.getElementsByTag("form")

        s"has the action '${controllers.routes.DeductionsController.submitOtherDisposals().toString}'" in {
          form.attr("action") shouldBe controllers.routes.DeductionsController.submitOtherDisposals().toString()
        }

        "has the method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        s"have a legend for an input with text ${viewMessages.pageHeading("2015/16")}" in {
          doc.select("legend.visuallyhidden").text() shouldEqual viewMessages.pageHeading("2015/16")
        }
      }
    }

    "Other Disposals view with pre-selected values" should {

      lazy val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "Yes")))
      lazy val view = views.otherDisposals(form, TaxYearModel("2015/16", true, "2015/16"), "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have the option 'Yes' auto selected" in {
        doc.body.getElementById("hasOtherProperties-yes").parent.className should include("selected")
      }
    }

    "Other Disposals view with errors" should {

      lazy val form = otherPropertiesForm.bind(Map(("hasOtherProperties", "")))
      lazy val view = views.otherDisposals(form, TaxYearModel("2015/16", true, "2015/16"), "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "display an error summary message for the amount" in {
        doc.body.select("#hasOtherProperties-error-summary").size shouldBe 1
      }

      "display an error message for the input" in {
        doc.body.select("span.error-notification").size shouldBe 1
      }
    }

    "using a disposal date 0f 2013/14" should {
      lazy val view = views.otherDisposals(otherPropertiesForm, TaxYearModel("2013/14", false, "2015/16"), "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title of ${viewMessages.title("2013/14")}" in {
        doc.title() shouldBe viewMessages.title("2013/14")
      }

      s"have a question of ${viewMessages.pageHeading("2013/14")}" in {
        doc.select("h1").text shouldBe viewMessages.pageHeading("2013/14")
      }
    }
  }
}
