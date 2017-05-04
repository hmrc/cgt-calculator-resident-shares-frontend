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

package views.calculation.summary

import assets.MessageLookup.Resident.{Shares => SharesMessages}
import assets.MessageLookup.{SummaryPage => messages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.{MessageLookup => pages}
import common.Dates
import common.Dates._
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{summary => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SharesDeductionsSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  lazy val homeLink = controllers.routes.GainController.disposalDate().url

  "Shares Deductions Summary view" when {

    "acquired after start of tax (1 April 1982) and not inherited" should {
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
        acquisitionCosts = 10000)
      lazy val deductionAnswers = DeductionGainAnswersModel(
        Some(LossesBroughtForwardModel(false)),
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
      lazy val backLink = "/calculate-your-capital-gains/resident/shares/losses-brought-forward"
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, homeLink)(fakeRequestWithSession, applicationMessages)
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

        s"has the text '${commonMessages.back}'" in {
          backLink.text shouldBe commonMessages.back
        }

        s"has a link to '${routes.DeductionsController.lossesBroughtForward().toString()}'" in {
          backLink.attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().toString
        }

      }

      s"have a page heading" which {

        s"includes a secondary heading with text '${messages.pageHeading}'" in {
          doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
        }

        "includes an amount of tax due of £0.00" in {
          doc.select("h1").text should include("£0.00")
        }
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      s"have a section for the Calculation details" which {

        "has the class 'summary-section' to underline the heading" in {

          doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

        }

        s"has a h2 tag" which {

          s"should have the title '${messages.calcDetailsHeadingDate("2015/16")}'" in {
            doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2015/16")
          }

          "has the class 'heading-large'" in {
            doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
          }
        }

        "has a numeric output row for the gain" which {

          "should have the question text 'Total Gain'" in {
            doc.select("#gain-question").text shouldBe messages.totalGain
          }

          "should have the value '£50,000'" in {
            doc.select("#gain-amount").text shouldBe "£50,000"
          }
        }

        "has a numeric output row for the deductions" which {

          "should have the question text 'Deductions'" in {
            doc.select("#deductions-question").text shouldBe messages.deductions
          }

          "should have the value '£11,100'" in {
            doc.select("#deductions-amount").text should include("£11,100")
          }

          "has a breakdown that" should {

            "include a value for Capital gains tax allowance used of £11,100" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £11,100")
            }

            "include a value for Loss brought forward of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2015/16")} £0")
            }
          }
        }

        "has no numeric output row for brought forward losses remaining" in {
          doc.select("#broughtForwardLossRemaining").isEmpty shouldBe true
        }

        "has a numeric output row for the AEA remaining" which {

          "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
            doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
          }

          "include a value for Capital gains tax allowance left of £0" in {
            doc.select("#aeaRemaining-amount").text should include("£0")
          }

          "not include the additional help text for AEA" in {
            doc.select("#aeaRemaining-amount div span").isEmpty shouldBe true
          }
        }
      }

      "display the save as PDF Link" which {

        "should render only one button" in {
          doc.select("a.save-pdf-link").size() shouldEqual 1
        }

        s"with an href to ${controllers.routes.ReportController.deductionsReport().toString}" in {
          doc.select("a.save-pdf-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/deductions-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-link").text shouldEqual messages.saveAsPdf
        }
      }

      "has a continue button" which {
        s"has the text ${commonMessages.continue}" in {
          doc.select("button").text shouldBe commonMessages.continue
        }
      }
    }
  }

  "Shares Deductions Summary view with all options selected" should {
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
        acquisitionCosts = 10000)
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

    lazy val backLink = "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, homeLink)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a back button" which {

      lazy val backLink = doc.getElementById("back-link")

      "has the id 'back-link'" in {
        backLink.attr("id") shouldBe "back-link"
      }

      s"has the text '${commonMessages.back}'" in {
        backLink.text shouldBe commonMessages.back
      }

      s"has a link to '${routes.DeductionsController.annualExemptAmount().toString()}'" in {
        backLink.attr("href") shouldBe routes.DeductionsController.annualExemptAmount().toString
      }

    }

    "has a notice summary that" should {

      "have the class notice-wrapper" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have the text ${messages.noticeWarning("2015/16")}" in {
        doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2015/16")
      }

      "have a warning icon" in {
        doc.select("i.icon-important").isEmpty shouldBe false
      }

      "have a visually hidden warning text" in {
        doc.select("div.notice-wrapper span.visuallyhidden").text shouldBe messages.warning
      }
    }

    s"have a section for the Calculation details" which {

      "has the class 'summary-section' to underline the heading" in {

        doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

      }

      s"has a h2 tag" which {

        s"should have the title '${messages.calcDetailsHeadingDate("2013/14")}'" in {
          doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2013/14")
        }

        "has the class 'heading-large'" in {
          doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
        }
      }

      "has a numeric output row for the gain" which {

        "should have the question text 'Total Gain'" in {
          doc.select("#gain-question").text shouldBe messages.totalGain
        }

        "should have the value '£50,000'" in {
          doc.select("#gain-amount").text shouldBe "£50,000"
        }
      }

      "has a numeric output row for the deductions" which {

        "should have the question text 'Deductions'" in {
          doc.select("#deductions-question").text shouldBe messages.deductions
        }

        "should have the value '£71,000'" in {
          doc.select("#deductions-amount").text should include("£71,000")
        }

        "has a breakdown that" should {

          "include a value for Capital gains tax allowance used of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
          }

          "include a value for Loss brought forward of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2013/14")} £10,000")
          }
        }
      }

      "has a numeric output row for brought forward losses remaining" which {

        "should have the question text for an out of year loss" in {
          doc.select("#broughtForwardLossRemaining-question").text() shouldBe messages.remainingBroughtForwardLoss("2013/14")
        }

        "should have the value £2000" in {
          doc.select("#broughtForwardLossRemaining-amount").text() should include("£2,000")
        }

        "should have the correct help text" in {
          doc.select("#broughtForwardLossRemaining-amount div span").text() should include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${commonMessages.externalLink} ${messages.remainingBroughtForwardLossHelp}")
        }

        "should have a link in the help text to https://www.gov.uk/capital-gains-tax/losses" in {
          doc.select("#broughtForwardLossRemaining-amount div span a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/losses"
        }
      }

      "has a numeric output row for the AEA remaining" which {

        "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
          doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
        }

        "include a value for Capital gains tax allowance left of £11,000" in {
          doc.select("#aeaRemaining-amount").text should include("£11,000")
        }

        "include the additional help text for AEA" in {
          doc.select("#aeaRemaining-amount div span").text shouldBe messages.aeaHelp
        }
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-link").size() shouldEqual 1
      }

      s"with an href to ${controllers.routes.ReportController.deductionsReport().toString}" in {
        doc.select("a.save-pdf-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/deductions-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-link").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Shares Deductions Summary when supplied with a date within the known tax years and no gain or loss" should {

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2016),
      soldForLessThanWorth = false,
      disposalValue = Some(200000),
      worthWhenSoldForLess = None,
      disposalCosts = 0,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(100000),
      acquisitionCosts = 0)
    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(0)))
    lazy val results = ChargeableGainResultModel(BigDecimal(0),
      BigDecimal(0),
      BigDecimal(0),
      BigDecimal(0),
      BigDecimal(50000),
      BigDecimal(0),
      BigDecimal(2000),
      None,
      None,
      0,
      0
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, homeLink)(fakeRequest, applicationMessages)


    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("#whatToDoNext p").text shouldBe messages.whatToDoNextText
    }
  }

  "Shares Deductions Summary when supplied with a date above the known tax years" should {

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
      acquisitionCosts = 10000)
    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(0),
      BigDecimal(0),
      None,
      None,
      10000,
      10000
    )

    lazy val taxYearModel = TaxYearModel("2017/18", false, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    lazy val view = views.deductionsSummary(gainAnswers, deductionAnswers, results, backLink, taxYearModel, homeLink)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }
}
