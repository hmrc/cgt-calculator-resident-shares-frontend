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

package views.calculation.report

import assets.DateAsset
import assets.MessageLookup.{SummaryPage => messages}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import play.api.i18n.Lang
import play.api.mvc.MessagesControllerComponents
import views.html.calculation.report.finalSummaryReport

class SharesFinalReportViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {

  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val finalSummaryReportView = fakeApplication.injector.instanceOf[finalSummaryReport]
  val fakeLang: Lang = Lang("en")
  "Final Summary view" when {

    "property acquired after start of tax (1 April 1982) and not inherited" should {

      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        soldForLessThanWorth = false,
        disposalValue = Some(200000),
        worthWhenSoldForLess = None,
        disposalCosts = 10000,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(100000),
        acquisitionCosts = 10000
      )

      lazy val deductionAnswers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)),
        None)

      lazy val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

      lazy val results = TotalGainAndTaxOwedModel(
        50000,
        20000,
        0,
        30000,
        3600,
        30000,
        18,
        Some(10000),
        Some(28),
        None,
        None,
        0,
        0
      )

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

      lazy val view = finalSummaryReportView(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel,
        false, 100, 100)(fakeRequestWithSession, mockMessage, fakeLang)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have a page heading" which {

        "includes an amount of tax due of £3,600.00" in {
          doc.select("h2").text should include("£3,600.00")
        }
      }

      "have the HMRC logo with the HMRC name" in {
        doc.select("div.logo span").first().text shouldBe "HM Revenue & Customs"
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a calculation detail section" in {
        doc.select("#calcDetails").size() shouldBe 1
      }

      "generate the same template when .render and .f are called" in {

        val f = finalSummaryReportView.f(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel,
          false, 100, 100)(fakeRequestWithSession, mockMessage, fakeLang)

        val render = finalSummaryReportView.render(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel,
          false, 100, 100, fakeRequestWithSession, mockMessage, fakeLang)

        f shouldBe render
      }

    }
  }


  "Final Summary when supplied with a date above the known tax years" should {

    lazy val taxYearModel = TaxYearModel(DateAsset.getYearAfterCurrentTaxYear, false, Dates.getCurrentTaxYear)

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2018),
      soldForLessThanWorth = false,
      disposalValue = Some(200000),
      worthWhenSoldForLess = None,
      disposalCosts = 10000,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(100000),
      acquisitionCosts = 10000
    )
    lazy val deductionAnswers = DeductionGainAnswersModel(Some(LossesBroughtForwardModel(false)),
      None)

    lazy val incomeAnswers = IncomeAnswersModel(Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

    lazy val results = TotalGainAndTaxOwedModel(
      50000,
      20000,
      0,
      30000,
      3600,
      30000,
      18,
      Some(10000),
      Some(28),
      None,
      None,
      0,
      0
    )

    lazy val view = finalSummaryReportView(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel, false,
    100, 100)(fakeRequestWithSession, mockMessage, fakeLang)
    lazy val doc = Jsoup.parse(view.body)

    "have the class notice-wrapper" in {
      doc.select(".govuk-warning-text__text").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeSummary}" in {
      doc.select(".govuk-warning-text__text").text shouldBe messages.noticeSummary
    }
  }
}
