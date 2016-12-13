/*
 * Copyright 2016 HM Revenue & Customs
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

package views.resident.shares.report

import assets.MessageLookup.Resident.{Shares => SharesMessages}
import assets.MessageLookup.{SummaryPage => messages}
import assets.{MessageLookup => commonMessages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.resident.shares.{report => views}

class SharesDeductionsReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

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
      lazy val deductionAnswers = DeductionGainAnswersModel(
        Some(OtherPropertiesModel(false)),
        None,
        None,
        Some(LossesBroughtForwardModel(false)),
        None,
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

      lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
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

            "include a value for Allowable Losses of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLossesUsed("2015/16")} £0")
            }

            "include a value for Capital gains tax allowance used of £11,100" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £11,100")
            }

            "include a value for Loss brought forward of £0" in {
              doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2015/16")} £0")
            }
          }
        }

        "has no numeric output row for allowable losses remaining" in {
          doc.select("#allowableLossRemaining").isEmpty shouldBe true
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

          s"should have the question text '${commonMessages.SharesDisposalDate.title}'" in {
            doc.select("#disposalDate-question").text shouldBe commonMessages.SharesDisposalDate.title
          }

          "should have the value '10 October 2016'" in {
            doc.select("#disposalDate-date span.bold-medium").text shouldBe "10 October 2016"
          }
        }

        "has a numeric output row for the Disposal Value" which {

          s"should have the question text '${commonMessages.Resident.Shares.DisposalValue.question}'" in {
            doc.select("#disposalValue-question").text shouldBe commonMessages.Resident.Shares.DisposalValue.question
          }

          "should have the value '£200,000'" in {
            doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£200,000"
          }
        }

        "has a numeric output row for the Disposal Costs" which {

          s"should have the question text '${commonMessages.SharesDisposalCosts.title}'" in {
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

          s"should have the question text '${commonMessages.SharesAcquisitionValue.title}'" in {
            doc.select("#acquisitionValue-question").text shouldBe commonMessages.SharesAcquisitionValue.title
          }

          "should have the value '£100,000'" in {
            doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£100,000"
          }
        }

        "has a numeric output row for the Acquisition Costs" which {

          s"should have the question text '${commonMessages.SharesAcquisitionCosts.title}'" in {
            doc.select("#acquisitionCosts-question").text shouldBe commonMessages.SharesAcquisitionCosts.title
          }

          "should have the value '£10,000'" in {
            doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£10,000"
          }
        }

        "has an option output row for other disposals" which {

          s"should have the question text '${commonMessages.OtherProperties.title("2015/16")}'" in {
            doc.select("#otherProperties-question").text shouldBe commonMessages.OtherProperties.title("2015/16")
          }

          "should have the value 'No'" in {
            doc.select("#otherProperties-option span.bold-medium").text shouldBe "No"
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
      }

      "does not display the section for what to do next" in {
        doc.select("#whatToDoNext").isEmpty shouldBe true
      }
    }

    "acquired after the start of the tax (1 April 1982) and inherited" should {

      val testModel = GainAnswersModel(

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
      lazy val deductionAnswers = DeductionGainAnswersModel(
        Some(OtherPropertiesModel(false)),
        None,
        None,
        Some(LossesBroughtForwardModel(false)),
        None,
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
      lazy val view = views.deductionsSummaryReport(testModel, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
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

      val testModel = GainAnswersModel(

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
      lazy val deductionAnswers = DeductionGainAnswersModel(
        Some(OtherPropertiesModel(false)),
        None,
        None,
        Some(LossesBroughtForwardModel(false)),
        None,
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
      lazy val view = views.deductionsSummaryReport(testModel, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
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
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(true)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)))
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

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)


    "has a notice summary that" should {

      "have the class notice-wrapper" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have the text ${messages.noticeWarning("2015/16")}" in {
        doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2015/16")
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

          "include a value for Allowable Losses of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsAllowableLossesUsed("2013/14")} £10,000")
          }

          "include a value for Capital gains tax allowance used of £0" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsCapitalGainsTax} £0")
          }

          "include a value for Loss brought forward of £10,000" in {
            doc.select("#deductions-amount").text should include(s"${messages.deductionsDetailsLossBeforeYearUsed("2013/14")} £10,000")
          }
        }
      }

      "has a numeric output row for allowable losses remaining" which {

        "should have the question text for an in year loss" in {
          doc.select("#allowableLossRemaining-question").text() shouldBe messages.remainingAllowableLoss("2013/14")
        }

        "should have the value £1000" in {
          doc.select("#allowableLossRemaining-amount").text() should include("£1,000")
        }

        "should have the correct help text" in {
          doc.select("#allowableLossRemaining-amount div span").text() should
            include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingAllowableLossHelp}")
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
          doc.select("#broughtForwardLossRemaining-amount div span").text() should
            include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingBroughtForwardLossHelp}")
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

      "has an option output row for other properties" which {

        s"should have the question text '${commonMessages.OtherProperties.title("2013/14")}'" in {
          doc.select("#otherProperties-question").text shouldBe commonMessages.OtherProperties.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#otherProperties-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has an option output row for allowable losses" which {

        s"should have the question text '${commonMessages.AllowableLosses.title("2013/14")}'" in {
          doc.select("#allowableLosses-question").text shouldBe commonMessages.AllowableLosses.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#allowableLosses-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for allowable losses value" which {

        s"should have the question text '${commonMessages.AllowableLossesValue.title("2013/14")}'" in {
          doc.select("#allowableLossesValue-question").text shouldBe commonMessages.AllowableLossesValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#allowableLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }
      }

      "has an option output row for brought forward losses" which {

        s"should have the question text '${commonMessages.LossesBroughtForward.title("2013/14")}'" in {
          doc.select("#broughtForwardLosses-question").text shouldBe commonMessages.LossesBroughtForward.title("2013/14")
        }

        "should have the value 'Yes'" in {
          doc.select("#broughtForwardLosses-option span.bold-medium").text shouldBe "Yes"
        }
      }

      "has a numeric output row for brought forward losses value" which {

        s"should have the question text '${commonMessages.LossesBroughtForwardValue.title("2013/14")}'" in {
          doc.select("#broughtForwardLossesValue-question").text shouldBe commonMessages.LossesBroughtForwardValue.title("2013/14")
        }

        "should have the value '£10,000'" in {
          doc.select("#broughtForwardLossesValue-amount span.bold-medium").text shouldBe "£10,000"
        }
      }
    }

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }

  "Summary when shares have been sold for less" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

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
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest)
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

  "Deductions Report view with AEA options selected" which {

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
      Some(OtherPropertiesModel(true)),
      Some(AllowableLossesModel(false)),
      Some(AllowableLossesValueModel(10000)),
      Some(LossesBroughtForwardModel(true)),
      Some(LossesBroughtForwardValueModel(10000)),
      Some(AnnualExemptAmountModel(1000)))
    lazy val results = ChargeableGainResultModel(BigDecimal(50000),
      BigDecimal(-11000),
      BigDecimal(0),
      BigDecimal(11000),
      BigDecimal(71000),
      BigDecimal(1000),
      BigDecimal(0),
      None,
      None,
      10000,
      10000
    )
    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    lazy val view = views.deductionsSummaryReport(gainAnswers, deductionAnswers, results, taxYearModel)(fakeRequestWithSession)
    lazy val doc = Jsoup.parse(view.body)

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

    "has a numeric output row for allowable losses remaining" which {

      "should have the question text for an in year loss" in {
        doc.select("#allowableLossRemaining-question").text() shouldBe messages.remainingAllowableLoss("2015/16")
      }

      "should have the value £1000" in {
        doc.select("#allowableLossRemaining-amount").text() should include("£1,000")
      }

      "should have the correct help text" in {
        doc.select("#allowableLossRemaining-amount div span").text() should
          include(s"${messages.remainingLossHelp} ${messages.remainingLossLink} ${messages.remainingAllowableLossHelp}")
      }
    }

    "has no numeric output row for brought forward losses remaining" in {
      doc.select("#broughtForwardLossRemaining").isEmpty shouldBe true
    }

    "has a numeric output row for AEA value" should {

      s"should have the question text '${commonMessages.AnnualExemptAmount.title}'" in {
        doc.select("#annualExemptAmount-question").text shouldBe commonMessages.AnnualExemptAmount.title
      }

      "should have the value '£1,000'" in {
        doc.select("#annualExemptAmount-amount span.bold-medium").text shouldBe "£1,000"
      }
    }

    "does not display the section for what to do next" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }
}
