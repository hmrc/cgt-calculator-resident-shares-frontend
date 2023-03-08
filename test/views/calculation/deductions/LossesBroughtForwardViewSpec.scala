/*
 * Copyright 2023 HM Revenue & Customs
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

import assets.MessageLookup.{LossesBroughtForward => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.LossesBroughtForwardForm._
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import play.api.i18n.Lang
import views.html.calculation.deductions.lossesBroughtForward
import play.api.mvc.MessagesControllerComponents

class LossesBroughtForwardViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val postAction = controllers.routes.DeductionsController.submitLossesBroughtForward
  val lossesBroughtForwardView = fakeApplication.injector.instanceOf[lossesBroughtForward]
  val fakeLang: Lang = Lang("en")

  "Reliefs view" should {

    lazy val view = lossesBroughtForwardView(lossesBroughtForwardForm, postAction, "", TaxYearModel("2015/16", true, "2015/16"),
      "home-link", "navTitle")(fakeRequest, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a dynamic navTitle of navTitle" in {
      doc.getElementsByClass("hmrc-header__service-name hmrc-header__service-name--linked").text() shouldBe "Calculate your Capital Gains Tax"
    }

    s"have a title ${messages.title("2015 to 2016")}" in {
      doc.title() shouldBe messages.title("2015 to 2016")
    }

    "have a home link to 'home-link'" in {
      doc.select("body > header > div > div > div.govuk-header__logo > a").attr("href") shouldEqual "https://www.gov.uk"
    }

    s"have a back link with text ${commonMessages.back}" in {
      doc.getElementsByClass("govuk-back-link").text shouldEqual commonMessages.back
    }

    s"have the question of the page ${messages.question("2015 to 2016")}" in {
      doc.getElementsByClass("govuk-heading-xl").text() shouldEqual messages.question("2015 to 2016")
    }

    s"render a form tag with a POST action" in {
      doc.select("form").attr("method") shouldEqual "POST"
    }

    s"have a visually hidden legend for an input with text ${messages.question("2015 to 2016")}" in {
      doc.getElementsByClass("govuk-fieldset__legend govuk-visually-hidden").text() shouldEqual messages.question("2015 to 2016")
    }

    s"have an input field with id option-yes " in {
      doc.body.getElementById("option").tagName() shouldEqual "input"
    }

    s"have an input field with id option-no " in {
      doc.body.getElementById("option-2").tagName() shouldEqual "input"
    }
    s"have a help section with the text ${messages.helpText}" in {
      doc.getElementsByClass("govuk-hint").text shouldEqual messages.helpText
    }

    "have a continue button " in {
      doc.getElementsByClass("govuk-button").text shouldEqual commonMessages.continue
    }

    "generate the same template when .render and .f are called" in {

      val f = lossesBroughtForwardView.f(lossesBroughtForwardForm, postAction, "", TaxYearModel("2015/16", true, "2015/16"),
        "home-link", "navTitle")(fakeRequest, mockMessage, fakeLang)

      val render = lossesBroughtForwardView.render(lossesBroughtForwardForm, postAction, "", TaxYearModel("2015/16", true, "2015/16"),
        "home-link", "navTitle", fakeRequest, mockMessage, fakeLang)

      f shouldBe render
    }
  }

  "Losses Brought Forward view with pre-selected value of yes" should {
    lazy val form = lossesBroughtForwardForm.bind(Map(("option", "Yes")))
    lazy val view = lossesBroughtForwardView(form, postAction, "", TaxYearModel("2015/16", true, "2015/16"),
      "home", "navTitle")(fakeRequest, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'Yes' auto selected" in {
      doc.body.getElementById("option").hasAttr("checked")
    }
  }

  "Losses Brought Forward view with pre-selected value of no" should {
    lazy val form = lossesBroughtForwardForm.bind(Map(("option", "No")))
    lazy val view = lossesBroughtForwardView(form, postAction, "", TaxYearModel("2015/16", true, "2015/16"),
      "home", "navTitle")(fakeRequest, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have the option 'No' auto selected" in {
      doc.body.getElementById("option-2").hasAttr("checked")
    }
  }

  "Losses Brought Forward view with errors" should {
    lazy val form = lossesBroughtForwardForm.bind(Map(("option", "")))
    lazy val view = lossesBroughtForwardView(form, postAction, "", TaxYearModel("2015/16", true, "2015/16"),
      "home", "navTitle")(fakeRequest, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "display an error summary message for the page" in {
      doc.body.select(".govuk-error-summary__body").size shouldBe 1
    }

    "display an error message for the input" in {
      doc.getElementsByClass("govuk-form-group govuk-form-group--error").size shouldBe 1
    }
  }
}
