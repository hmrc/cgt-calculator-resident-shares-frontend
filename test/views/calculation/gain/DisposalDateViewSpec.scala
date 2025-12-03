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

package views.calculation.gain

import assets.MessageLookup.{DisposalDate as viewMessages, Resident as commonMessages, SharesDisposalDate as messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm.*
import models.resident.DisposalDateModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import util.helper.ViewBehaviours
import views.html.calculation.gain.disposalDate

import java.time.LocalDate

class DisposalDateViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with ViewBehaviours{
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val disposalDateView: disposalDate = fakeApplication.injector.instanceOf[disposalDate]
  "Disposal Date view" should {

    lazy val view = disposalDateView(disposalDateForm(LocalDate.parse("2015-04-06")))(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementsByClass("govuk-service-navigation__link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
    }

    "have the title 'When did you sell or give away the shares?'" in {
      doc.title() shouldBe messages.title
    }

    "have the heading question 'When did you sell or give away the shares?'" should {
      behave like pageWithExpectedMessage(legendHeadingStyle, messages.question)(using doc)
    }

    "have the helptext 'For example, 4 9 2021'" in {
      doc.body.getElementsByClass("govuk-hint").text should include(viewMessages.helpText)
    }

    "have an input box for day" in {
      doc.body.getElementById("disposalDate.day").parent.text shouldBe commonMessages.day
    }

    "have an input box for month" in {
      doc.body.getElementById("disposalDate.month").parent.text shouldBe commonMessages.month
    }

    "have an input box for year" in {
      doc.body.getElementById("disposalDate.year").parent.text shouldBe commonMessages.year
    }

    "have a button with the text 'Continue'" in {
      doc.body.getElementsByClass("govuk-button").text shouldBe commonMessages.continue
    }

    "generate the same template when .render and .f are called" in {

      val form = (disposalDateView(disposalDateForm(
        LocalDate.parse("2015-04-06")))
      (using fakeRequest, mockMessage))

      val render = disposalDateView.render(disposalDateForm(LocalDate.parse("2015-04-06")),
        fakeRequest, mockMessage)

      form shouldBe render
    }
  }

  "Disposal Date view with a pre-filled form" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).fill(DisposalDateModel(10, 6, 2016))
    lazy val view = disposalDateView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have a value auto-filled in the day input" in {
      doc.body.getElementById("disposalDate.day").`val`() shouldBe "10"
    }

    "have a value auto-filled in the month input" in {
      doc.body.getElementById("disposalDate.month").`val`() shouldBe "6"
    }

    "have a value auto-filled in the year input" in {
      doc.body.getElementById("disposalDate.year").`val`() shouldBe "2016"
    }
  }

  "Disposal Date view with a non-valid date input error" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(Map(
      ("disposalDate.day", "29"),
      ("disposalDate.month", "2"),
      ("disposalDate.year", "2017")
    ))
    lazy val view = disposalDateView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    "have the error summary message 'Enter a real date'" in {
      doc.body.getElementsByClass("govuk-list govuk-error-summary__list").text should include(commonMessages.errorInvalidDate)
    }
  }

  "Disposal Date view with an empty field date input error" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(Map(
      ("disposalDate.day", ""),
      ("disposalDate.month", "10"),
      ("disposalDate.year", "2016")
    ))
    lazy val view = disposalDateView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have the error summary message '${viewMessages.requiredDayError}'" in {
      doc.body.getElementsByClass("govuk-list govuk-error-summary__list").text should include(viewMessages.requiredDayError)
    }
  }

  "Disposal Date view with a non numeric date input error" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06")).bind(Map(
      ("disposalDate.day", "a"),
      ("disposalDate.month", "b"),
      ("disposalDate.year", "c")
    ))
    lazy val view = disposalDateView(form)(using fakeRequest, mockMessage)
    lazy val doc = Jsoup.parse(view.body)

    s"have the error summary message '${viewMessages.invalidDateError}'" in {
      doc.body.getElementsByClass("govuk-list govuk-error-summary__list").text should include(viewMessages.invalidDateError)
    }
  }
}
