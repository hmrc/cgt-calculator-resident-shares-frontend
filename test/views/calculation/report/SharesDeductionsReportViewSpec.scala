/*
 * Copyright 2020 HM Revenue & Customs
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

import assets.MessageLookup.{SummaryDetails => messages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Lang
import play.api.i18n.Messages.Implicits._
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{report => views}

class SharesDeductionsReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val fakeLang: Lang = Lang("en")
  "Deductions Report view" when {

    "acquired after the start of the tax (1 April 1982) and not inherited" should {
      lazy val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2016),
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
      lazy val results = ChargeableGainResultModel(BigDecimal(50000),
        BigDecimal(38900),
        BigDecimal(11100),
        BigDecimal(0),
        BigDecimal(11100),
        BigDecimal(0),
        BigDecimal(0),
        None,
        None,
        0,
        0
      )

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

      lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel, 1000)(fakeRequestWithSession, mockMessage, fakeApplication, fakeLang)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have the hmrc logo with the hmrc name" in {
        doc.select("span.organisation-logo-text").text shouldBe "HM Revenue & Customs"
      }

      "have a banner for tax owed" in {
        doc.select("#tax-owed-banner").size() shouldBe 1
      }

      "have no tax year notice" in {
        doc.select("#notice-summary").size() shouldBe 0
      }

      "have a calculation details section" in {
        doc.select("#calcDetails").size() shouldBe 1
      }

      s"have a section for Your answers" which {

        "has an entry for disposal date" in {
          doc.select("#disposalDate-question").size() shouldBe 1
        }

        "has an entry for brought forward losses" in {
          doc.select("#broughtForwardLosses-question").size() shouldBe 1
        }

        "has no entry for personal allowance" in {
          doc.select("#personalAllowance-question").size() shouldBe 0
        }
      }

      "does not display the section for what to do next" in {
        doc.select("#whatToDoNext").isEmpty shouldBe true
      }
    }
  }

  "Deductions Report view with all options selected" should {
    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2016),
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
    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(2000),
      None,
      None,
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel, 1000)(fakeRequestWithSession, mockMessage, fakeApplication, fakeLang)
    lazy val doc = Jsoup.parse(view.body)


    "has a notice summary that" should {

      "have the class notice-wrapper" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have the text ${messages.noticeSummary}" in {
        doc.select("strong.bold-small").text shouldBe messages.noticeSummary
      }

    }

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }

    "generate the same template when .render and .f are called" in {

      val render = views.deductionsSummaryReport.render(gainAnswers, deductionAnswers, results, taxYearModel, 1000,
        fakeRequestWithSession, mockMessage, fakeApplication, fakeLang)

      val f = views.deductionsSummaryReport.f(gainAnswers, deductionAnswers, results, taxYearModel, 1000)(
        fakeRequestWithSession, mockMessage, fakeApplication, fakeLang)

      f shouldBe render
    }
  }

}
