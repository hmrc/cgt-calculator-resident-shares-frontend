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

import assets.MessageLookup.{AllowableLosses => messages}
import controllers.helpers.FakeRequestHelper
import forms.AllowableLossesForm.allowableLossesForm
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{deductions => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class AllowableLossesViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  lazy val postAction = controllers.routes.DeductionsController.submitAllowableLosses()

  "Allowable Losses view" should {

    lazy val backLink = Some(controllers.routes.DeductionsController.otherDisposals().toString())
    lazy val view = views.allowableLosses(allowableLossesForm, TaxYearModel("2015/16", true, "2015/16"), postAction, backLink,
      "home", "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title("2015/16")}" in {
      doc.title() shouldBe messages.title("2015/16")
    }

    "have a dynamic navTitle of navTitle" in {
      doc.select("span.header__menu__proposition-name").text() shouldBe "navTitle"
    }

    "have a H1 tag that" should {

      lazy val heading = doc.select("H1")

      s"have the page heading '${messages.title("2015/16")}'" in {
        heading.text shouldBe messages.title("2015/16")
      }

      "have the heading-large class" in {
        heading.hasClass("heading-large") shouldBe true
      }
    }

    "have a home link to 'home'" in {
      doc.select("#homeNavHref").attr("href") shouldEqual "home"
    }

    "have a fieldset with aria-details attribute" in {
      doc.select("fieldset").attr("aria-details") shouldBe "help"
    }

    s"have a drop down button with the text ${messages.helpInfoTitle}" in {
      doc.body.getElementsByTag("summary").attr("role") shouldBe "button"
      doc.body.getElementsByTag("summary").text shouldEqual messages.helpInfoTitle
    }

    "have a hidden legend" in {
      val legend = doc.select("legend")
      legend.hasClass("visuallyhidden") shouldBe true
    }

    "have the correct help info subtitle" in {
      val element = doc.select("#helpInfo > p")
      element.text shouldBe messages.helpInfoSubtitle
    }

    "have the correct help info bullet point #1" in {
      val element = doc.select("#helpInfo > ul > li:eq(0)")
      element.text shouldBe messages.helpInfoPoint1
    }

    "have the correct help info bullet point #2" in {
      val element = doc.select("#helpInfo > ul > li:eq(1)")
      element.text shouldBe messages.helpInfoPoint2
    }

    "have the correct help info bullet point #3" in {
      val element = doc.select("#helpInfo > ul > li:eq(2)")
      element.text shouldBe messages.helpInfoPoint3
    }
 }

  "Allowable Losses view with pre-selected values" should {
    lazy val backLink = Some(controllers.routes.DeductionsController.otherDisposals().toString())
    lazy val form = allowableLossesForm.bind(Map(("isClaiming", "Yes")))
    lazy val view = views.allowableLosses(form, TaxYearModel("2015/16", true, "2015/16"), postAction, backLink, "home",
      "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Yes' auto selected" in {
      doc.body.getElementById("isClaiming-yes").parent.className should include("selected")
    }
  }

  "Allowable Losses view with errors" should {
    lazy val backLink = Some(controllers.routes.DeductionsController.otherDisposals().toString())
    lazy val form = allowableLossesForm.bind(Map(("isClaiming", "")))
    lazy val view = views.allowableLosses(form, TaxYearModel("2015/16", true, "2015/16"), postAction, backLink, "home",
      "navTitle")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the amount" in {
      doc.body.select("#isClaiming-error-summary").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.body.select("span.error-notification").size shouldBe 1
    }
  }
}
