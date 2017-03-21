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

import assets.MessageLookup.Resident.{Shares => sharesMessages}
import assets.MessageLookup.{SummaryPage => messages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{summary => views}
import assets.DateAsset
import assets.{MessageLookup => pages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.Shares.{SharesSummaryMessages => sharesSummaryMessages}
import common.Dates._
import controllers.routes
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel, PreviousTaxableGainsModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import models.resident.IncomeAnswersModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SharesFinalSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Final Summary shares view" when {

    "Owned acquired after start of tax (31 March 1982) and not inherited" should {
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
      lazy val incomeAnswers = IncomeAnswersModel(None, Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))
      lazy val results = TotalGainAndTaxOwedModel(
        50000,
        20000,
        0,
        30000,
        3600,
        30000,
        18,
        None,
        None,
        None,
        None,
        0,
        0
      )
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
      lazy val homeLink = "home-link"
      lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, false)(fakeRequestWithSession, applicationMessages)
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

        s"has a link to '${routes.IncomeController.personalAllowance().toString()}'" in {
          backLink.attr("href") shouldBe routes.IncomeController.personalAllowance().toString
        }
      }

      "has a home link too 'home-link'" in {
        doc.select("#homeNavHref").attr("href") shouldEqual "home-link"
      }

      s"have a page heading" which {

        s"includes a secondary heading with text '${messages.pageHeading}'" in {
          doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
        }

        "includes an amount of tax due of £3,600.00" in {
          doc.select("h1").text should include("£3,600.00")
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

          "should have the value '£0'" in {
            doc.select("#deductions-amount").text should include("£0")
          }

          "has a breakdown that" should {

            "include a value for Capital gains tax allowance used of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
            }

            "include a value for Loss brought forward of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2015/16")} £0")
            }
          }
        }

        "has a numeric output row for the chargeable gain" which {

          "should have the question text 'Taxable Gain'" in {
            doc.select("#chargeableGain-question").text shouldBe messages.chargeableGain
          }

          "should have the value '£20,000'" in {
            doc.select("#chargeableGain-amount").text should include("£20,000")
          }
        }

        "has a numeric output row and a tax rate" which {

          "Should have the question text 'Tax Rate'" in {
            doc.select("#gainAndRate-question").text shouldBe messages.taxRate
          }

          "Should have the value £30,000" in {
            doc.select("#firstBand").text should include("£30,000")
          }
          "Should have the tax rate 18%" in {
            doc.select("#firstBand").text should include("18%")
          }
        }

        "has a numeric output row for the AEA remaining" which {

          "should have the question text 'Capital Gains Tax allowance left for 2015/16" in {
            doc.select("#aeaRemaining-question").text should include(messages.aeaRemaining("2015/16"))
          }

          "include a value for Capital gains tax allowance left of £0" in {
            doc.select("#aeaRemaining-amount").text should include("£0")
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

          s"should have the question text '${sharesSummaryMessages.disposalDateQuestion}'" in {
            doc.select("#disposalDate-question").text shouldBe sharesSummaryMessages.disposalDateQuestion
          }

          "should have the date '10 October 2016'" in {
            doc.select("#disposalDate-date span.bold-medium").text shouldBe "10 October 2016"
          }

          s"should have a change link to ${routes.GainController.disposalDate().url}" in {
            doc.select("#disposalDate-date a").attr("href") shouldBe routes.GainController.disposalDate().url
          }

          "has the question as part of the link" in {
            doc.select("#disposalDate-date a").text shouldBe s"${commonMessages.change} ${sharesSummaryMessages.disposalDateQuestion}"
          }

          "has the question component of the link is visuallyhidden" in {
            doc.select("#disposalDate-date a span.visuallyhidden").text shouldBe sharesSummaryMessages.disposalDateQuestion
          }
        }

        "has a numeric output row for the Disposal Value" which {

          s"should have the question text '${sharesSummaryMessages.disposalValueQuestion}'" in {
            doc.select("#disposalValue-question").text shouldBe sharesSummaryMessages.disposalValueQuestion
          }

          "should have the value '£200,000'" in {
            doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
          }

          s"should have a change link to ${routes.GainController.disposalValue().url}" in {
            doc.select("#disposalValue-amount a").attr("href") shouldBe routes.GainController.disposalValue().url
          }

        }

        "has a numeric output row for the Disposal Costs" which {

          s"should have the question text '${sharesSummaryMessages.disposalCostsQuestion}'" in {
            doc.select("#disposalCosts-question").text shouldBe sharesSummaryMessages.disposalCostsQuestion
          }

          "should have the value '£10,000'" in {
            doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£10,000"
          }

          s"should have a change link to ${routes.GainController.disposalCosts().url}" in {
            doc.select("#disposalCosts-amount a").attr("href") shouldBe routes.GainController.disposalCosts().url
          }

        }

        "has an option/radiobutton output row for the Owned Before Start of Tax" which {

          s"should have the question text '${sharesMessages.OwnerBeforeLegislationStart.title}'" in {
            doc.select("#ownerBeforeLegislationStart-question").text shouldBe sharesMessages.OwnerBeforeLegislationStart.title
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

          s"should have the question text '${sharesMessages.DidYouInheritThem.question}'" in {
            doc.select("#inheritedTheShares-question").text shouldBe sharesMessages.DidYouInheritThem.question
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

          s"should have the question text '${sharesSummaryMessages.acquisitionValueQuestion}'" in {
            doc.select("#acquisitionValue-question").text shouldBe sharesSummaryMessages.acquisitionValueQuestion
          }

          "should have the value '£100,000'" in {
            doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
          }

          s"should have a change link to ${routes.GainController.acquisitionValue().url}" in {
            doc.select("#acquisitionValue-amount a").attr("href") shouldBe routes.GainController.acquisitionValue().url
          }

        }

        "has a numeric output row for the Acquisition Costs" which {

          s"should have the question text '${sharesSummaryMessages.acquisitionCostsQuestion}'" in {
            doc.select("#acquisitionCosts-question").text shouldBe sharesSummaryMessages.acquisitionCostsQuestion
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
        "has a numeric output row for current income" which {

          s"should have the question text '${pages.CurrentIncome.title("2015/16")}'" in {
            doc.select("#currentIncome-question").text shouldBe pages.CurrentIncome.title("2015/16")
          }

          "should have the value '£0'" in {
            doc.select("#currentIncome-amount span.bold-medium").text shouldBe "£0"
          }

          s"should have a change link to ${routes.IncomeController.currentIncome().url}" in {
            doc.select("#currentIncome-amount a").attr("href") shouldBe routes.IncomeController.currentIncome().url
          }
        }
        "has a numeric output row for personal allowance" which {

          s"should have the question text '${pages.PersonalAllowance.question("2015/16")}'" in {
            doc.select("#personalAllowance-question").text shouldBe pages.PersonalAllowance.question("2015/16")
          }

          "should have the value '£0'" in {
            doc.select("#personalAllowance-amount span.bold-medium").text shouldBe "£0"
          }

          s"should have a change link to ${routes.IncomeController.personalAllowance().url}" in {
            doc.select("#personalAllowance-amount a").attr("href") shouldBe routes.IncomeController.personalAllowance().url
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

        s"with an href to ${controllers.routes.ReportController.finalSummaryReport().toString}" in {
          doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/final-report"
        }

        s"have the text ${messages.saveAsPdf}" in {
          doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
        }
      }
    }

    "not owned before start of tax (1 April 1982) and inherited" should {

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
      lazy val incomeAnswers = IncomeAnswersModel(None, Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))
      lazy val results = TotalGainAndTaxOwedModel(
        50000,
        20000,
        0,
        30000,
        3600,
        30000,
        18,
        None,
        None,
        None,
        None,
        0,
        0
      )
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
      lazy val homeLink = "home-link"
      lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, false)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${sharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe sharesMessages.OwnerBeforeLegislationStart.title
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

        s"should have the question text '${sharesMessages.DidYouInheritThem.question}'" in {
          doc.select("#inheritedTheShares-question").text shouldBe sharesMessages.DidYouInheritThem.question
        }

        "should have the value 'Yes'" in {
          doc.select("#inheritedTheShares-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.didYouInheritThem().url}" in {
          doc.select("#inheritedTheShares-option a").attr("href") shouldBe routes.GainController.didYouInheritThem().url
        }
      }

      "has a numeric output row for the Inherited Value" which {

        s"should have the question text '${sharesMessages.WorthWhenInherited.question}'" in {
          doc.select("#worthWhenInherited-question").text shouldBe sharesMessages.WorthWhenInherited.question
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

    "owned before start of tax (1 April 1982)" should {

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
      lazy val incomeAnswers = IncomeAnswersModel(None, Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))
      lazy val results = TotalGainAndTaxOwedModel(
        50000,
        20000,
        0,
        30000,
        3600,
        30000,
        18,
        None,
        None,
        None,
        None,
        0,
        0
      )
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
      lazy val homeLink = "home-link"
      lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, false)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${sharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe sharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
          doc.select("#ownerBeforeLegislationStart-option a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
        }
      }

      "has a numeric output row for the Worth on 31 March 1982 value" which {

        s"should have the question text '${sharesMessages.ValueBeforeLegislationStart.question}'" in {
          doc.select("#valueBeforeLegislationStart-question").text shouldBe sharesMessages.ValueBeforeLegislationStart.question
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
  
  "Final Summary shares view with a calculation that returns tax on both side of the rate boundary" should {

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
    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))
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
    lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
    lazy val homeLink = "home-link"
    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, false)(fakeRequestWithSession, applicationMessages)
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

      s"has a link to '${routes.IncomeController.personalAllowance().toString()}'" in {
        backLink.attr("href") shouldBe routes.IncomeController.personalAllowance().toString
      }
    }

    "has a numeric output row and a tax rate" which {

      "Should have the question text 'Tax Rate'" in {
        doc.select("#gainAndRate-question").text shouldBe messages.taxRate
      }

      "Should have the value £30,000 in the first band" in {
        doc.select("#firstBand").text should include("£30,000")
      }
      "Should have the tax rate 18% for the first band" in {
        doc.select("#firstBand").text should include("18%")
      }

      "Should have the value £10,000 in the second band" in {
        doc.select("#secondBand").text should include("£10,000")
      }
      "Should have the tax rate 28% for the first band" in {
        doc.select("#secondBand").text should include("28%")
      }
    }
  }

  "Final Summary shares when supplied with a date within the known tax years and tax owed" should {

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(200000),
      worthWhenSoldForLess = None,
      disposalCosts = 0,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(0),
      acquisitionCosts = 0)


    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(false)),
      None)

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(0)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))
    lazy val results = TotalGainAndTaxOwedModel(
      0,
      0,
      0,
      0,
      0,
      0,
      18,
      Some(0),
      Some(28),
      None,
      None,
      0,
      0
    )

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
    lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
    lazy val homeLink = "home-link"
    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, false)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextSharesLiabilityMessage}" in {
      doc.select("#whatToDoNextText").text shouldEqual s"${messages.whatToDoNextSharesLiabilityMessage} ${commonMessages.externalLink}."
    }

    s"display the additional text ${messages.whatToDoNextLiabilityAdditionalMessage}" in {
      doc.select("#whatToDoNext p").text shouldEqual messages.whatToDoNextLiabilityAdditionalMessage
    }

    "have a link" which {

      "should have a href attribute" in {
        doc.select("#whatToDoNextLink").hasAttr("href") shouldEqual true
      }

      "should link to the work-out-need-to-pay govuk page" in {
        doc.select("#whatToDoNextLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax"
      }

      "have the externalLink attribute" in {
        doc.select("#whatToDoNextLink").hasClass("external-link") shouldEqual true
      }

      "has a visually hidden span with the text opens in a new tab" in {
        doc.select("span#opensInANewTab").text shouldEqual commonMessages.externalLink
      }
    }
  }

  "Final Summary shares when supplied with a date above the known tax years" should {

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2018),
      soldForLessThanWorth = false,
      disposalValue = Some(200000),
      worthWhenSoldForLess = None,
      disposalCosts = 0,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(0),
      acquisitionCosts = 0)

    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(false)),
      None)

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

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

    lazy val taxYearModel = TaxYearModel(Dates.getCurrentTaxYear, false, DateAsset.getYearAfterCurrentTaxYear)
    lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
    lazy val homeLink = "home-link"
    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, false)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the what to do next content" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }

  "Shares Final Summary when the shares were sold for less than they were worth." should {

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = Dates.constructDate(10, 10, 2016),
      soldForLessThanWorth = true,
      disposalValue = None,
      worthWhenSoldForLess = Some(200000),
      disposalCosts = 0,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(0),
      acquisitionCosts = 0
    )

    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(false)),
      None)

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

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

    lazy val taxYearModel = TaxYearModel(Dates.getCurrentTaxYear, true, Dates.getCurrentTaxYear)
    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, "back-link", taxYearModel, "home-link", true)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for private residence relief value in" which {

      s"should have the question text '${sharesMessages.WorthWhenSoldForLess.question}'" in {
        doc.select("#worthWhenSoldForLess-question").text shouldBe sharesMessages.WorthWhenSoldForLess.question
      }

      "should have the value '£200,000'" in {
        doc.select("#worthWhenSoldForLess-amount span.bold-medium").text shouldBe "£200,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenSoldForLess().url}" in {
        doc.select("#worthWhenSoldForLess-amount a").attr("href") shouldBe routes.GainController.worthWhenSoldForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#worthWhenSoldForLess-amount a").text shouldBe s"${commonMessages.change} " +
          s"${sharesMessages.WorthWhenSoldForLess.question}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#worthWhenSoldForLess-amount a span.visuallyhidden").text shouldBe
          sharesMessages.WorthWhenSoldForLess.question
      }
    }
  }

  "Final Summary shares when supplied with a date in 2016/17" should {

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
      acquisitionValue = Some(0),
      acquisitionCosts = 0)

    lazy val deductionAnswers = DeductionGainAnswersModel(
      Some(LossesBroughtForwardModel(false)),
      None)

    lazy val incomeAnswers = IncomeAnswersModel(Some(PreviousTaxableGainsModel(1000)), Some(CurrentIncomeModel(0)), Some(PersonalAllowanceModel(0)))

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

    lazy val taxYearModel = TaxYearModel(Dates.getCurrentTaxYear, true, Dates.getCurrentTaxYear)
    lazy val backLink = "/calculate-your-capital-gains/resident/shares/personal-allowance"
    lazy val homeLink = "home-link"
    lazy val view = views.finalSummary(gainAnswers, deductionAnswers, incomeAnswers, results, backLink, taxYearModel, homeLink, true)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for current income" which {

      s"should have the question text '${pages.CurrentIncome.currentYearTitle}'" in {
        doc.select("#currentIncome-question").text shouldBe pages.CurrentIncome.currentYearTitle
      }
    }
  }
}
