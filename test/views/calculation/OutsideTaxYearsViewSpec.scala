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

package views.calculation

import assets.MessageLookup.{OutsideTaxYears => messages, Resident => commonMessages}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.outsideTaxYear

class OutsideTaxYearsViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val outsideTaxYearView: outsideTaxYear = fakeApplication.injector.instanceOf[outsideTaxYear]

  "Outside tax years views" when {

    "using a disposal date before 2015/16 with properties" should {
      lazy val taxYear = TaxYearModel("2014/15", false, "2015/16")
      lazy val view = outsideTaxYearView(taxYear, false, true, "back-link", "continue-link")(using fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"return a title of ${messages.title}" in {
        doc.title shouldBe messages.title
      }

      "have a navTitle" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").text() shouldBe "Calculate your Capital Gains Tax"
      }

      "have a home link to 'home-link'" in {
        doc.select("body > header > div > div > div.govuk-header__content > a").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
      }

      s"have a heading of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      s"have a message of ${messages.tooEarly}" in {
        doc.select("p.govuk-body").text() shouldBe messages.tooEarly
      }

      "have a 'Change your date' link that" should {
        lazy val backLink = doc.select(".govuk-back-link")

        "have the correct text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the govuk-body govuk-link class" in {
          backLink.hasClass("govuk-back-link") shouldBe true
        }

        "have a link to 'back-link'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "generate the same template when .render and .f are called" in {

        val f = outsideTaxYearView.f(taxYear, false, true, "back-link", "continue-link")(fakeRequestWithSession, mockMessage)

        val render = outsideTaxYearView.render(taxYear, false, true, "back-link", "continue-link",
          fakeRequestWithSession, mockMessage)

        f shouldBe render
      }
    }

    "using a disposal date after 2016/17" should {
      lazy val taxYear = TaxYearModel("2017/18", false, "2016/17")
      lazy val view = outsideTaxYearView(taxYear, true, true, "back-link", "continue-link")(using fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "have charset UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"return a title of ${messages.title}" in {
        doc.title shouldBe messages.title
      }

      s"have a heading of ${messages.heading}" in {
        doc.select("h1").text() shouldBe messages.heading
      }

      s"have a message of ${messages.content("2016/17")}" in {
        doc.select("p.govuk-body").text() shouldBe messages.content("2016/17")
      }

      "have a back link that" should {
        lazy val backLink = doc.select(".govuk-back-link")

        "have the correct back link text" in {
          backLink.text shouldBe commonMessages.back
        }

        "have the govuk-back-link class" in {
          backLink.hasClass("govuk-back-link") shouldBe true
        }

        "have a link to 'back-link'" in {
          backLink.attr("href") shouldBe "#"
        }
      }

      "have a continue button that" should {
        lazy val button = doc.select("a#continue-button")

        "have the correct text 'Continue'" in {
          button.text() shouldBe commonMessages.continue
        }

        s"have an href to 'continue-link'" in {
          button.attr("href") shouldBe "continue-link"
        }
      }
    }

    "using a disposal date before 2015/16 with shares" should {
      lazy val taxYear = TaxYearModel("2014/15", false, "2015/16")
      lazy val view = outsideTaxYearView(taxYear, false, false, "back-link", "continue-link")(using fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"have a message of ${messages.sharesTooEarly}" in {
        doc.select("p.govuk-body").text() shouldBe messages.sharesTooEarly
      }
    }
  }
}
