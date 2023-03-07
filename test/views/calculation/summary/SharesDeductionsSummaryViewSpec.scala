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

package views.calculation.summary

import assets.MessageLookup.{Resident => residentMessages, SummaryDetails => messages}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares._
import org.jsoup.Jsoup
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.summary.deductionsSummary

class SharesDeductionsSummaryViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val deductionsSummaryView = fakeApplication.injector.instanceOf[deductionsSummary]
  val fakeLang: Lang = Lang("en")

  "Properties Deductions Summary view when a valid tax year is supplied" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = true,
      disposalValue = Some(100000),
      worthWhenSoldForLess = Some(200000),
      disposalCosts = BigDecimal(10000),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(0),
      acquisitionCosts = BigDecimal(10000)
    )
    val deductionAnswers = DeductionGainAnswersModel(
      broughtForwardModel = Some(LossesBroughtForwardModel(true)),
      broughtForwardValueModel = Some(LossesBroughtForwardValueModel(36))
    )
    val results = ChargeableGainResultModel(
      gain = 30000,
      chargeableGain = 0,
      aeaUsed = 900,
      aeaRemaining = 1000,
      deductions = 30000,
      allowableLossesRemaining = 0,
      broughtForwardLossesRemaining = 1,
      lettingReliefsUsed = Some(BigDecimal(0)),
      prrUsed = Some(BigDecimal(0)),
      broughtForwardLossesUsed = 35,
      allowableLossesUsed = 0
    )
    val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

    lazy val backUrl = controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url

    lazy val view = deductionsSummaryView(gainAnswers, deductionAnswers, results, backUrl,
      taxYearModel, "home-link", 100, showUserResearchPanel = true)(fakeRequestWithSession, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have a charset of UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"have a title ${messages.title}" in {
      doc.title() shouldBe messages.title
    }

    s"have a back button" which {

      lazy val backLink = doc.getElementById("back-link")

      "has the id 'back-link'" in {
        backLink.attr("id") shouldBe "back-link"
      }

      s"has the text '${residentMessages.back}'" in {
        backLink.text shouldBe residentMessages.back
      }

      s"has a link to '${controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url}'" in {
        backLink.attr("href") shouldBe controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url
      }
    }

    "has a h1" which {
      s"has the text '£0.00'" in {
        doc.select("h1").text() should include(messages.cgtToPay("2015 to 2016"))
      }
    }

    "should display the what to do next section" which {
      lazy val whatToDoNext = doc.select("section#whatToDoNext")

      s"should have a h2 with the text ${messages.whatToDoNextHeading}" in {
        whatToDoNext.select("h2").text shouldBe messages.whatToDoNextHeading
      }

      s"should have a p with the text ${messages.whatToDoNextContinue}" in {
        whatToDoNext.select("p").text shouldBe messages.whatToDoNextContinue
      }

    }

    "has a save as PDF Button" which {

      lazy val savePDFSection = doc.select("#save-as-a-pdf")

      "contains an internal div which" should {

        lazy val icon = savePDFSection.select("div")

        "has class icon-file-download" in {
          icon.hasClass("icon-file-download") shouldBe true
        }

        "contains a span" which {

          lazy val informationTag = icon.select("span")

          "has the class govuk-visually-hidden" in {
            informationTag.hasClass("govuk-visually-hidden") shouldBe true
          }

          "has the text Download" in {
            informationTag.text shouldBe "Download"
          }
        }

        "contains a link" which {

          lazy val link = savePDFSection.select("a")

          "has the class " in {
            link.hasClass("govuk-link govuk-body") shouldBe true
          }

          s"links to ${controllers.routes.ReportController.deductionsReport()}" in {
            link.attr("href") shouldBe controllers.routes.ReportController.deductionsReport().toString
          }

          s"has the text ${messages.saveAsPdf}" in {
            link.text shouldBe messages.saveAsPdf
          }
        }
      }
    }

    "display the continue button" in {
      doc.select("a.govuk-button").size() shouldBe 1
    }

    s"the continue button has a link to ${controllers.routes.SaUserController.saUser().url}" in {
      doc.select("a.govuk-button").attr("href") shouldBe controllers.routes.SaUserController.saUser().url
    }

    "does have ur panel" in {
      doc.toString.contains(messages.bannerPanelTitle)
      doc.toString.contains(messages.bannerPanelLinkText)
      doc.toString.contains(messages.bannerPanelCloseVisibleText)

    }

    "generate the same template when .render and .f are called" in {

      val f = deductionsSummaryView.f(gainAnswers, deductionAnswers, results, backUrl,
        taxYearModel, "home-link", 100, true)(fakeRequestWithSession, mockMessage, fakeLang)

      val render = deductionsSummaryView.render(gainAnswers, deductionAnswers, results, backUrl,
        taxYearModel, "home-link", 100, true, fakeRequestWithSession, mockMessage, fakeLang)

      f shouldBe render
    }
  }

  "Properties Deductions Summary view when a tax year outside the accepted is supplied" should {

    val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2014),
      soldForLessThanWorth = true,
      disposalValue = Some(100000),
      worthWhenSoldForLess = Some(200000),
      disposalCosts = BigDecimal(10000),
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(0),
      acquisitionCosts = BigDecimal(10000)
    )
    val deductionAnswers = DeductionGainAnswersModel(
      broughtForwardModel = Some(LossesBroughtForwardModel(true)),
      broughtForwardValueModel = Some(LossesBroughtForwardValueModel(36))
    )
    val results = ChargeableGainResultModel(
      gain = 30000,
      chargeableGain = 0,
      aeaUsed = 900,
      aeaRemaining = 1000,
      deductions = 30000,
      allowableLossesRemaining = 0,
      broughtForwardLossesRemaining = 1,
      lettingReliefsUsed = Some(BigDecimal(0)),
      prrUsed = Some(BigDecimal(0)),
      broughtForwardLossesUsed = 35,
      allowableLossesUsed = 0
    )
    val taxYearModel = TaxYearModel("2014/15", isValidYear = false, "2015/16")

    lazy val backUrl = controllers.routes.ReviewAnswersController.reviewDeductionsAnswers().url

    lazy val view = deductionsSummaryView(gainAnswers, deductionAnswers, results, backUrl,
      taxYearModel, "home-link", 100, showUserResearchPanel = false)(fakeRequestWithSession, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "not display the what to do next section" in {
      doc.select("section#whatToDoNext").isEmpty shouldBe true
    }

    "not display the continue button" in {
      doc.select("a.button").isEmpty shouldBe true
    }

    "does not have ur panel" in {
      doc.select("div#ur-panel").size() shouldBe 0
    }
  }


}
