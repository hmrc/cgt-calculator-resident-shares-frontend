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
            doc.select("#deductions-amount span.bold-medium").text should include("£11,100")
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
            doc.select("#aeaRemaining-amount span.bold-medium").text should include("£0")
          }

          "not include the additional help text for AEA" in {
            doc.select("#aeaRemaining-amount div span").isEmpty shouldBe true
          }
        }
      }

      s"have a section for Your answers" which {

        "has the class 'summary-section' to underline the heading" in {

          doc.select("section#yourAnswers h2").hasClass("summary-underline") shouldBe true

        }

        s"has a h2 tag" which {

          s"should have the title '${messages.yourAnswersHeading}'" in {
            doc.select("section#yourAnswers h2").text shouldBe messages.yourAnswersHeading
          }

          "has the class 'heading-large'" in {
            doc.select("section#yourAnswers h2").hasClass("heading-large") shouldBe true
          }
        }

        "has a date output row for the Disposal Date" which {

          s"should have the question text '${pages.SharesDisposalDate.title}'" in {
            doc.select("#disposalDate-question").text shouldBe pages.SharesDisposalDate.title
          }

          "should have the value '10 October 2016'" in {
            doc.select("#disposalDate-date span.bold-medium").text shouldBe "10 October 2016"
          }

          s"should have a change link to ${routes.GainController.disposalDate().url}" in {
            doc.select("#disposalDate-date a").attr("href") shouldBe routes.GainController.disposalDate().url
          }

          "has the question as part of the link" in {
            doc.select("#disposalDate-date a").text shouldBe s"${commonMessages.change} ${pages.SharesDisposalDate.title}"
          }

          "has the question component of the link is visuallyhidden" in {
            doc.select("#disposalDate-date a span.visuallyhidden").text shouldBe pages.SharesDisposalDate.title
          }
        }

        "has a numeric output row for the Disposal Value" which {

          s"should have the question text '${pages.Resident.Shares.DisposalValue.question}'" in {
            doc.select("#disposalValue-question").text shouldBe pages.Resident.Shares.DisposalValue.question
          }

          "should have the value '£200,000'" in {
            doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
          }

          s"should have a change link to ${routes.GainController.disposalValue().url}" in {
            doc.select("#disposalValue-amount a").attr("href") shouldBe routes.GainController.disposalValue().url
          }

        }

        "has a numeric output row for the Disposal Costs" which {

          s"should have the question text '${pages.SharesDisposalCosts.title}'" in {
            doc.select("#disposalCosts-question").text shouldBe pages.SharesDisposalCosts.title
          }

          "should have the value '£10,000'" in {
            doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£10,000"
          }

          s"should have a change link to ${routes.GainController.disposalCosts().url}" in {
            doc.select("#disposalCosts-amount a").attr("href") shouldBe routes.GainController.disposalCosts().url
          }

        }

        "has an option/radiobutton output row for the Owned Before Start of Tax" which {

          s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
            doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
          }

          "should have the value 'No'" in {
            doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "No"
          }

          s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
            doc.select("#ownerBeforeLegislationStart-option a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
          }
        }

        "does not have a numeric output row for the Worth on 31 March 1982 value" in {
          //Tests here for Worth On
        }

        "has an option/radiobutton output row for Did You Inherit the Shares" which {

          s"should have the question text '${SharesMessages.DidYouInheritThem.question}'" in {
            doc.select("#inheritedTheShares-question").text shouldBe SharesMessages.DidYouInheritThem.question
          }

          "should have the value 'No'" in {
            doc.select("#inheritedTheShares-option span.bold-medium").text shouldBe "No"
          }

          s"should have a change link to ${routes.GainController.didYouInheritThem().url}" in {
            doc.select("#inheritedTheShares-option a").attr("href") shouldBe routes.GainController.didYouInheritThem().url
          }
        }

        "does not have a numeric output row for the Inherited Value" in {
          doc.select("#worthWhenInherited-question").isEmpty shouldBe true
        }

        "has a numeric output row for the Acquisition Value" which {

          s"should have the question text '${pages.SharesAcquisitionValue.title}'" in {
            doc.select("#acquisitionValue-question").text shouldBe pages.SharesAcquisitionValue.title
          }

          "should have the value '£100,000'" in {
            doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
          }

          s"should have a change link to ${routes.GainController.acquisitionValue().url}" in {
            doc.select("#acquisitionValue-amount a").attr("href") shouldBe routes.GainController.acquisitionValue().url
          }

        }

        "has a numeric output row for the Acquisition Costs" which {

          s"should have the question text '${pages.SharesAcquisitionCosts.title}'" in {
            doc.select("#acquisitionCosts-question").text shouldBe pages.SharesAcquisitionCosts.title
          }

          "should have the value '£10,000'" in {
            doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£10,000"
          }

          s"should have a change link to ${routes.GainController.acquisitionCosts().url}" in {
            doc.select("#acquisitionCosts-amount a").attr("href") shouldBe routes.GainController.acquisitionCosts().url
          }

        }

        "has an option output row for brought forward losses" which {

          s"should have the question text '${pages.LossesBroughtForward.title("2015/16")}'" in {
            doc.select("#broughtForwardLosses-question").text shouldBe pages.LossesBroughtForward.title("2015/16")
          }

          "should have the value 'No'" in {
            doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "No"
          }

          s"should have a change link to ${routes.DeductionsController.lossesBroughtForward().url}" in {
            doc.select("#broughtForwardLosses-option a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().url
          }

          "has the question as part of the link" in {
            doc.select("#broughtForwardLosses-option a").text shouldBe s"${commonMessages.change} ${pages.LossesBroughtForward.question("2015/16")}"
          }

          "has the question component of the link as visuallyhidden" in {
            doc.select("#broughtForwardLosses-option a span.visuallyhidden").text shouldBe pages.LossesBroughtForward.question("2015/16")
          }
        }
      }

      "display the save as PDF Button" which {

        "should render only one button" in {
          doc.select("a.save-pdf-button").size() shouldEqual 1
        }

        "with the class save-pdf-button" in {
          doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
        }

        s"with an href to ${controllers.routes.ReportController.deductionsReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/deductions-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }

    "acquired after start of tax (1 April 1982) and inherited" should {

      val gainAnswers = GainAnswersModel(
        disposalDate = constructDate(12, 12, 2019),
        soldForLessThanWorth = false,
        disposalValue = Some(10),
        worthWhenSoldForLess = None,
        disposalCosts = 20,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(true),
        worthWhenInherited = Some(5000),
        acquisitionValue = None,
        acquisitionCosts = 40
      )
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

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'No'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "No"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
          doc.select("#ownerBeforeLegislationStart-option a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
        }
      }

      "does not have a numeric output row for the Worth on 31 March 1982 value" in {
        //Tests here for Worth On
      }

      "has an option/radiobutton output row for Did You Inherit the Shares" which {

        s"should have the question text '${SharesMessages.DidYouInheritThem.question}'" in {
          doc.select("#inheritedTheShares-question").text shouldBe SharesMessages.DidYouInheritThem.question
        }

        "should have the value 'Yes'" in {
          doc.select("#inheritedTheShares-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.didYouInheritThem().url}" in {
          doc.select("#inheritedTheShares-option a").attr("href") shouldBe routes.GainController.didYouInheritThem().url
        }
      }

      "has a numeric output row for the Inherited Value" which {

        s"should have the question text '${SharesMessages.WorthWhenInherited.question}'" in {
          doc.select("#worthWhenInherited-question").text shouldBe SharesMessages.WorthWhenInherited.question
        }

        "should have the value '5000'" in {
          doc.select("#worthWhenInherited-amount span.bold-medium").text shouldBe "£5,000"
        }

        s"should have a change link to ${routes.GainController.worthWhenInherited().url}" in {
          doc.select("#worthWhenInherited-amount a").attr("href") shouldBe routes.GainController.worthWhenInherited().url
        }
      }

      "does not have a numeric output row for the Acquisition Value" in {
        doc.select("#acquisitionValue-question").isEmpty shouldBe true
      }
    }

    "acquired before start of tax (1 April 1982)" should {

      val gainAnswers = GainAnswersModel(

        disposalDate = constructDate(12, 12, 2019),
        soldForLessThanWorth = false,
        disposalValue = Some(10),
        worthWhenSoldForLess = None,
        disposalCosts = 20,
        ownerBeforeLegislationStart = true,
        valueBeforeLegislationStart = Some(700),
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(30),
        acquisitionCosts = 40
      )
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

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
          doc.select("#ownerBeforeLegislationStart-option a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
        }
      }

      "has a numeric output row for the Worth on 31 March 1982 value" which {

        s"should have the question text '${SharesMessages.ValueBeforeLegislationStart.question}'" in {
          doc.select("#valueBeforeLegislationStart-question").text shouldBe SharesMessages.ValueBeforeLegislationStart.question
        }

        "should have the value '£700'" in {
          doc.select("#valueBeforeLegislationStart-amount span.bold-medium").text shouldBe "£700"
        }

        s"should have a change link to ${routes.GainController.valueBeforeLegislationStart().url}" in {
          doc.select("#valueBeforeLegislationStart-amount a").attr("href") shouldBe routes.GainController.valueBeforeLegislationStart().url
        }
      }

      "does not have an option/radiobutton output row for Did You Inherit the Shares" in {
        doc.select("#inheritedTheShares-question").isEmpty shouldBe true
      }

      "does not have a numeric output row for the Inherited Value" in {
        doc.select("#worthWhenInherited-question").isEmpty shouldBe true
      }

      "does not have a numeric output row for the Acquisition Value" in {
        doc.select("#acquisitionValue-question").isEmpty shouldBe true
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
          doc.select("#deductions-amount span.bold-medium").text should include("£71,000")
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
          doc.select("#aeaRemaining-amount span.bold-medium").text should include("£11,000")
        }

        "include the additional help text for AEA" in {
          doc.select("#aeaRemaining-amount div span").text shouldBe messages.aeaHelp
        }
      }
    }

    s"have a section for Your answers" which {

      "has the class 'summary-section' to underline the heading" in {

        doc.select("section#yourAnswers h2").hasClass("summary-underline") shouldBe true

      }

      s"has a h2 tag" which {

        s"should have the title '${messages.yourAnswersHeading}'" in {
          doc.select("section#yourAnswers h2").text shouldBe messages.yourAnswersHeading
        }

        "has the class 'heading-large'" in {
          doc.select("section#yourAnswers h2").hasClass("heading-large") shouldBe true
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${pages.LossesBroughtForward.title("2013/14")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe pages.LossesBroughtForward.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForward().url}" in {
          doc.select("#broughtForwardLosses-option a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForward().url
        }

        "has the question as part of the link" in {
          doc.select("#broughtForwardLosses-option a").text shouldBe s"${commonMessages.change} ${pages.LossesBroughtForward.question("2013/14")}"
        }

        "has the question component of the link as visuallyhidden" in {
          doc.select("#broughtForwardLosses-option a span.visuallyhidden").text shouldBe pages.LossesBroughtForward.question("2013/14")
        }
      }

      "has a numeric output row for brought forward losses value" which {

        s"should have the question text '${pages.LossesBroughtForwardValue.title("2013/14")}'" in {
          doc.select("#broughtForwardLossesValue-question").text shouldBe pages.LossesBroughtForwardValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#broughtForwardLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }

        s"should have a change link to ${routes.DeductionsController.lossesBroughtForwardValue().url}" in {
          doc.select("#broughtForwardLossesValue-amount a").attr("href") shouldBe routes.DeductionsController.lossesBroughtForwardValue().url
        }
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${controllers.routes.ReportController.deductionsReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/deductions-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
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

    "has a numeric output row for brought forward losses remaining" which {

      "should have the question text for an out of year loss" in {
        doc.select("#broughtForwardLossRemaining-question").text() shouldBe messages.remainingBroughtForwardLoss("2015/16")
      }

      "should have the value £2000" in {
        doc.select("#broughtForwardLossRemaining-amount").text() should include("£2,000")
      }

      "should have the correct help text" in {
        doc.select("#broughtForwardLossRemaining-amount div span").text() should include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${commonMessages.externalLink} ${messages.remainingBroughtForwardLossHelp}")
      }
    }

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("h2#whatToDoNextNoLossTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("div#whatToDoNextNoLossText").text shouldBe s"${messages.whatToDoNextNoLossText} ${messages.whatToDoNextNoLossLinkShares} ${commonMessages.externalLink}."
    }

    s"have the link text ${messages.whatToDoNextNoLossLinkShares}${commonMessages.externalLink}" in {
      doc.select("div#whatToDoNextNoLossText a").text should  include(s"${messages.whatToDoNextNoLossLinkShares}")
    }

    s"have a link to https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax" in {
      doc.select("div#whatToDoNextNoLossText a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax"
    }

    s"have the visually hidden text ${commonMessages.externalLink}" in {
      doc.select("div#whatToDoNextNoLossText span#opensInANewWindow").text shouldBe s"${commonMessages.externalLink}"
    }
  }

  "Shares Deductions Summary when the shares were sold for less than they were worth." should {

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2016),
      soldForLessThanWorth = true,
      disposalValue = None,
      worthWhenSoldForLess = Some(200000),
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

    "has an option output row for private residence relief value in" which {

      s"should have the question text '${pages.Resident.Shares.WorthWhenSoldForLess.question}'" in {
        doc.select("#worthWhenSoldForLess-question").text shouldBe pages.Resident.Shares.WorthWhenSoldForLess.question
      }

      "should have the value '£200,000'" in {
        doc.select("#worthWhenSoldForLess-amount span.bold-medium").text shouldBe "£200,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenSoldForLess().url}" in {
        doc.select("#worthWhenSoldForLess-amount a").attr("href") shouldBe routes.GainController.worthWhenSoldForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#worthWhenSoldForLess-amount a").text shouldBe s"${commonMessages.change} " +
          s"${pages.Resident.Shares.WorthWhenSoldForLess.question}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#worthWhenSoldForLess-amount a span.visuallyhidden").text shouldBe
          pages.Resident.Shares.WorthWhenSoldForLess.question
      }
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
