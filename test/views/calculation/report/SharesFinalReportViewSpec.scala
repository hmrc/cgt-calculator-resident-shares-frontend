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

package views.calculation.report

import assets.MessageLookup.{SummaryPage => messages}
import assets.MessageLookup.Resident.{Shares => SharesMessages}
import assets.{DateAsset, MessageLookup => commonMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel, PreviousTaxableGainsModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{report => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SharesFinalReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

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

      lazy val view = views.finalSummaryReport(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel, false)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have a page heading" which {

        s"includes a secondary heading with text '${messages.pageHeading}'" in {
          doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
        }

        "includes an amount of tax due of £3,600.00" in {
          doc.select("h1").text should include("£3,600.00")
        }
      }

      "have the HMRC logo with the HMRC name" in {
        doc.select("div.logo span").text shouldBe "HM Revenue & Customs"
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a section for the Calculation Details" which {

        "has the class 'summary-section' to underline the heading" in {
          doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true
        }

        "has a h2 tag" which {

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

          "should have the question text 'Tax Rate'" in {
            doc.select("#gainAndRate-question").text shouldBe messages.taxRate
          }

          "should have the value £30,000" in {
            doc.select("#firstBand").text should include("£30,000")
          }

          "should have the tax rate 18%" in {
            doc.select("#firstBand").text should include("18%")
          }

          "should have the value £10,000 in the second band" in {
            doc.select("#secondBand").text should include("£10,000")
          }

          "should have the tax rate 28% for the first band" in {
            doc.select("#secondBand").text should include("28%")
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

      "have a section for Your answers" which {

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

          s"should have the question text '${commonMessages.DisposalDate.title}'" in {
            doc.select("#disposalDate-question").text shouldBe commonMessages.SharesDisposalDate.title
          }

          "should have the date '10 October 2015'" in {
            doc.select("#disposalDate-date span.bold-medium").text shouldBe "10 October 2015"
          }
        }

        "has a numeric output row for the Disposal Value" which {

          s"should have the question text '${commonMessages.DisposalValue.question}'" in {
            doc.select("#disposalValue-question").text shouldBe commonMessages.Resident.Shares.DisposalValue.question
          }

          "should have the value '£200,000'" in {
            doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
          }
        }

        "has a numeric output row for the Disposal Costs" which {

          s"should have the question text '${commonMessages.DisposalCosts.title}'" in {
            doc.select("#disposalCosts-question").text shouldBe commonMessages.SharesDisposalCosts.title
          }

          "should have the value '£10,000'" in {
            doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£10,000"
          }
        }

        "has an option/radiobutton output row for the Owned Before Start of Tax" which {

          s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
            doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
          }

          "should have the value 'No'" in {
            doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "No"
          }

          s"should not have a change link" in {
            doc.select("#ownerBeforeLegislationStart-option a").isEmpty shouldBe true
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

          s"should not have a change link" in {
            doc.select("#inheritedTheShares-option a").isEmpty shouldBe true
          }
        }

        "does not have a numeric output row for the Inherited Value" in {
          doc.select("#worthWhenInherited-question").isEmpty shouldBe true
        }

        "has a numeric output row for the Acquisition Value" which {

          s"should have the question text '${commonMessages.AcquisitionValue.title}'" in {
            doc.select("#acquisitionValue-question").text shouldBe commonMessages.SharesAcquisitionValue.title
          }

          "should have the value '£100,000'" in {
            doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
          }
        }

        "has a numeric output row for the Acquisition Costs" which {

          s"should have the question text '${commonMessages.AcquisitionCosts.title}'" in {
            doc.select("#acquisitionCosts-question").text shouldBe commonMessages.SharesAcquisitionCosts.title
          }

          "should have the value '£10,000'" in {
            doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£10,000"
          }
        }

        "has an option output row for brought forward losses" which {

          s"should have the question text '${commonMessages.LossesBroughtForward.title("2015/16")}'" in {
            doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.LossesBroughtForward.title("2015/16")
          }

          "should have the value 'No'" in {
            doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "No"
          }
        }

        "has a numeric output row for current income" which {

          s"should have the question text '${commonMessages.CurrentIncome.title("2015/16")}'" in {
            doc.select("#currentIncome-question").text shouldBe commonMessages.CurrentIncome.title("2015/16")
          }

          "should have the value '£0'" in {
            doc.select("#currentIncome-amount span.bold-medium").text shouldBe "£0"
          }
        }
      }
    }

    "acquired after the start of the tax (1 April 1982) and inherited" should {

      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 12, 2019),
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
      lazy val view = views.finalSummaryReport(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel, false)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'No'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "No"
        }

        s"should not have a change link" in {
          doc.select("#ownerBeforeLegislationStart-option a").isEmpty shouldBe true
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

        s"should not have a change link" in {
          doc.select("#inheritedTheShares-option a").isEmpty shouldBe true
        }
      }

      "has a numeric output row for the Inherited Value" which {

        s"should have the question text '${SharesMessages.WorthWhenInherited.question}'" in {
          doc.select("#worthWhenInherited-question").text shouldBe SharesMessages.WorthWhenInherited.question
        }

        "should have the value '5000'" in {
          doc.select("#worthWhenInherited-amount span.bold-medium").text shouldBe "£5,000"
        }

        s"should not have a change link" in {
          doc.select("#worthWhenInherited-amount a").isEmpty shouldBe true
        }
      }

      "does not have a numeric output row for the Acquisition Value" in {
        doc.select("#acquisitionValue-question").isEmpty shouldBe true
      }
    }

    "acquired before start of tax (1 April 1982)" should {

      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(12, 12, 2019),
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
      lazy val view = views.finalSummaryReport(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel, false)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeLegislationStart-option span.bold-medium").text shouldBe "Yes"
        }

        s"should not have a change link" in {
          doc.select("#ownerBeforeLegislationStart-option a").isEmpty shouldBe true
        }
      }

      "has a numeric output row for the Worth on 31 March 1982 value" which {

        s"should have the question text '${SharesMessages.ValueBeforeLegislationStart.question}'" in {
          doc.select("#valueBeforeLegislationStart-question").text shouldBe SharesMessages.ValueBeforeLegislationStart.question
        }

        "should have the value '£700'" in {
          doc.select("#valueBeforeLegislationStart-amount span.bold-medium").text shouldBe "£700"
        }

        s"should not have a change link" in {
          doc.select("#valueBeforeLegislationStart-option a").isEmpty shouldBe true
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

  "Summary when shares have been sold for less in the year after the current" should {

    lazy val taxYearModel = TaxYearModel(DateAsset.getYearAfterCurrentTaxYear, false, Dates.getCurrentTaxYear)

    val testModel = GainAnswersModel(
      disposalDate = Dates.constructDate(12, 9, 2015),
      soldForLessThanWorth = true,
      disposalValue = None,
      worthWhenSoldForLess = Some(10),
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has a numeric output row for the Worth of the shares when sold for less" which {

      s"should have the question text '${commonMessages.Resident.Shares.WorthWhenSoldForLess.question}'" in {
        doc.select("#worthWhenSoldForLess-question").text shouldBe commonMessages.Resident.Shares.WorthWhenSoldForLess.question
      }

      "should have the value '£10'" in {
        doc.select("#worthWhenSoldForLess-amount span.bold-medium").text shouldBe "£10"
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

    lazy val view = views.finalSummaryReport(gainAnswers, deductionAnswers, incomeAnswers, results, taxYearModel, false)(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have the class notice-wrapper" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeWarning(Dates.getCurrentTaxYear)}" in {
      doc.select("strong.bold-small").text shouldBe messages.noticeWarning(Dates.getCurrentTaxYear)
    }
  }
}
