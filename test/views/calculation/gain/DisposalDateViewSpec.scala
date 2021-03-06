/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.{LocalDate, ZoneId}

import assets.MessageLookup.{DisposalDate => viewMessages, Resident => commonMessages, SharesDisposalDate => messages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import forms.DisposalDateForm._
import models.resident.DisposalDateModel
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.{gain => views}

class DisposalDateViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  "Disposal Date view" should {

    lazy val view = views.disposalDate(disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))), "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    "have a home link to 'home-link'" in {
      doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
    }

    "have the title 'When did you sell or give away the shares?'" in {
      doc.title() shouldBe messages.title
    }

    "have the heading question 'When did you sell or give away the shares?'" in {
      doc.body.getElementsByTag("h1").text should include(messages.title)
    }

    "have the helptext 'For example, 4 9 2016'" in {
      doc.body.getElementsByClass("form-hint").text should include(viewMessages.helpText)
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
      doc.body.getElementById("continue-button").text shouldBe commonMessages.continue
    }

    "generate the same template when .render and .f are called" in {

      val f = (views.disposalDate.f(disposalDateForm(
        LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))), "home-link")
      (fakeRequest, mockMessage, fakeApplication, mockConfig))

      val render = views.disposalDate.render(disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))), "home-link",
        fakeRequest, mockMessage, fakeApplication, mockConfig)

      f shouldBe render
    }
  }

  "Disposal Date view with a pre-filled form" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).fill(DisposalDateModel(10, 6, 2016))
    lazy val view = views.disposalDate(form, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
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

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(Map(
      ("disposalDate.day", "32"),
      ("disposalDate.month", "10"),
      ("disposalDate.year", "2016")
    ))
    lazy val view = views.disposalDate(form, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    "have the error summary message 'Enter a real date'" in {
      doc.body.getElementById("disposalDateDay-error-summary").text shouldBe commonMessages.errorInvalidDate
    }
  }

  "Disposal Date view with an empty field date input error" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(Map(
      ("disposalDate.day", ""),
      ("disposalDate.month", "10"),
      ("disposalDate.year", "2016")
    ))
    lazy val view = views.disposalDate(form, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    s"have the error summary message '${viewMessages.invalidDayError}'" in {
      doc.body.getElementById("disposalDate.day-error-summary").text should include(viewMessages.invalidDayError)
    }
  }

  "Disposal Date view with a non numeric date input error" should {

    lazy val form = disposalDateForm(LocalDate.parse("2015-04-06").atStartOfDay(ZoneId.of("Europe/London"))).bind(Map(
      ("disposalDate.day", "a"),
      ("disposalDate.month", "b"),
      ("disposalDate.year", "c")
    ))
    lazy val view = views.disposalDate(form, "home-link")(fakeRequest, mockMessage, fakeApplication, mockConfig)
    lazy val doc = Jsoup.parse(view.body)

    s"have the error summary message '${viewMessages.invalidDayError}'" in {
      doc.body.getElementById("disposalDate.day-error-summary").text should include(viewMessages.invalidDayError)
    }

    s"have the error summary message '${viewMessages.invalidMonthError}'" in {
      doc.body.getElementById("disposalDate.month-error-summary").text should include(viewMessages.invalidMonthError)
    }

    s"have the error summary message '${viewMessages.invalidYearError}'" in {
      doc.body.getElementById("disposalDate.year-error-summary").text should include(viewMessages.invalidYearError)
    }
  }
}
