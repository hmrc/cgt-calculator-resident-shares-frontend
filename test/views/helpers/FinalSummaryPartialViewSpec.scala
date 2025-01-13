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

package views.helpers

import assets.MessageLookup.{SummaryDetails => summaryMessages}
import common.{CommonPlaySpec, Dates, WithCommonFakeApplication}
import controllers.helpers.FakeRequestHelper
import models.resident._
import models.resident.shares._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import views.html.playHelpers.resident.finalSummaryPartial

class FinalSummaryPartialViewSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper {
  private implicit lazy val mockMessage: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  private val finalSummaryPartialView = fakeApplication.injector.instanceOf[finalSummaryPartial]

  "FinalSummaryPartial" when {
    "the share was sold inside tax years, bought after legislation start," +
      " with no brought forward losses and taxed at 18%" should {
      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None
      )

      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = None,
        secondRate = None,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 30000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "contains a h1" which {
          lazy val h1 = banner.select("h1")

          s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
            h1.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
          }
        }

        "contains a paragraph" which {
          lazy val p = banner.select("p")

          s"has the text '£3,600.00'" in {
            p.text() shouldEqual "£3,600.00"
          }
        }
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a section for the Calculation details" which {
        "has a div for total gain" which {

          lazy val div = doc.select("#yourTotalGain").get(0)

          "has a caption tag" which {

            s"has the text '${summaryMessages.yourTotalGain}'" in {
              div.select("caption").text shouldBe summaryMessages.yourTotalGain
            }
          }

          "has a row for disposal value" which {

            s"has the text '${summaryMessages.disposalValue}'" in {
              div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
            }

            "has the value '£100,000'" in {
              div.select("#disposalValue-amount").text shouldBe "£100,000"
            }
          }

          "has a row for acquisition value" which {
            s"has the text '${summaryMessages.acquisitionValue}'" in {
              div.select("#acquisitionValueWhenBought-text").text shouldBe summaryMessages.acquisitionValue
            }

            "has the value '£0'" in {
              div.select("#acquisitionValueWhenBought-amount").text shouldBe "£0"
            }
          }

          "has a row for total costs" which {
            s"has the text '${summaryMessages.totalCosts}'" in {
              div.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
            }

            "has the value '£100'" in {
              div.select("#totalCosts-amount").text shouldBe "£100"
            }
          }

          "has a row for total gain" which {
            s"has the text '${summaryMessages.totalGain}'" in {
              div.select("#totalGain-text").text shouldBe summaryMessages.totalGain
            }

            "has the value '£50,000'" in {
              div.select("#totalGain-amount").text shouldBe "£50,000"
            }
          }
        }

        "has a div for deductions" which {

          lazy val div = doc.select("#yourDeductions")

          "has a caption tag" which {

            s"has the text '${summaryMessages.yourDeductions}'" in {
              div.select("caption").text shouldBe summaryMessages.yourDeductions
            }
          }

          "has a row for AEA used" which {

            s"has the text '${summaryMessages.aeaUsed}'" in {
              div.select("#aeaUsed-text").text shouldBe summaryMessages.aeaUsed
            }

            "has the value '£10'" in {
              div.select("#aeaUsed-amount").text shouldBe "£10"
            }
          }

          "not have a row for brought forward losses used" in {
            div.select("#lossesUsed-text") shouldBe empty
          }

          "has a row for total deductions" which {

            s"has the text '${summaryMessages.totalDeductions}'" in {
              div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
            }

            "has the value '£100'" in {
              div.select("#totalDeductions-amount").text shouldBe "£100"
            }
          }
        }

        "has a div for Taxable Gain" which {

          lazy val div = doc.select("#yourTaxableGain")

          "has a caption tag" which {

            s"has the text '${summaryMessages.yourTaxableGain}'" in {
              div.select("caption").text shouldBe summaryMessages.yourTaxableGain
            }
          }

          "has a row for gain" which {
            s"has the text '${summaryMessages.totalGain}'" in {
              div.select("#gain-text").text shouldBe summaryMessages.totalGain
            }

            "has the value '£50,000'" in {
              div.select("#gain-amount").text shouldBe "£50,000"
            }
          }

          "has a row for minus deductions" which {
            s"has the text '${summaryMessages.minusDeductions}'" in {
              div.select("#minusDeductions-text").text shouldBe summaryMessages.minusDeductions
            }

            "has the value '£100'" in {
              div.select("#minusDeductions-amount").text shouldBe "£100"
            }
          }

          "has a row for taxable gain" which {
            s"has the text '${summaryMessages.taxableGain}'" in {
              div.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
            }

            "has the value '£20,000'" in {
              div.select("#taxableGain-amount").text shouldBe "£20,000"
            }
          }
        }

        "has a div for tax rate" which {

          lazy val div = doc.select("#yourTaxRate")

          "has a caption tag" which {

            s"has the text ${summaryMessages.yourTaxRate}" in {
              div.select("#yourTaxRate > caption.govuk-table__caption.govuk-table__caption--m").text shouldBe summaryMessages.yourTaxRate
            }
          }

          "has row for first band" which {

            s"has the text '${summaryMessages.taxRate("£20,000", "18")}'" in {
              div.select("#firstBand-text").text shouldBe summaryMessages.taxRate("£20,000", "18")
            }

            "has the value '£30,000'" in {
              div.select("#firstBand-amount").text shouldBe "£30,000"
            }
          }

          "does not have a row for second band" in {
            div.select("#secondBand-text") shouldBe empty
          }

          "has a row for tax to pay" which {

            s"has the text ${summaryMessages.taxToPay}" in {
              div.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
            }

            "has the value '£3,600'" in {
              div.select("#taxToPay-amount").text shouldBe "£3,600"
            }
          }
        }
      }

      "have a section for the You remaining deductions" which {

        "has a div for remaining deductions" which {

          lazy val div = doc.select("#remainingDeductions")

          "has a caption tag" which {

            s"has the text ${summaryMessages.remainingDeductions}" in {
              div.select("caption").text shouldBe summaryMessages.remainingDeductions
            }
          }

          "has a row for annual exempt amount left" which {
            s"has the text ${summaryMessages.remainingAnnualExemptAmount("2015 to 2016")}" in {
              div.select("#aeaRemaining-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2015 to 2016")
            }

            "has the value '£0'" in {
              div.select("#aeaRemaining-amount").text shouldBe "£0"
            }
          }

          "not have a row for brought forward losses remaining" in {
            div.select("#broughtForwardLossesRemaining-text") shouldBe empty
          }

          "not have a row for losses to carry forward" in {
            div.select("#lossesToCarryForward-text") shouldBe empty
          }
        }
      }
    }

    "the share was sold for less than worth" should {
      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = None,
        worthWhenSoldForLess = Some(100000.00),
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = true,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = None,
        secondRate = None,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 30000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for worth when sold for less" which {

        s"has the text '${summaryMessages.disposalValue}'" in {
          doc.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
        }

        "has the value '£100,000'" in {
          doc.select("#disposalValue-amount").text shouldBe "£100,000"
        }
      }
    }

    "the calculation returns tax on both side of the rate boundary" should {
      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 0,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 30000,
        firstRate = 18,
        secondBand = Some(10000),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 101,
        upperRateTotal = 100
      )

      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel,
        100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a numeric output row and a tax rate" which {

        "has row for first band" which {

          s"has the text '${summaryMessages.taxRate("£30,000", "18")}'" in {
            doc.select("#firstBand-text").text shouldBe summaryMessages.taxRate("£30,000", "18")
          }

          "has the value '£101'" in {
            doc.select("#firstBand-amount").text shouldBe "£101"
          }
        }

        "has row for second band" which {

          s"has the text '${summaryMessages.taxRate("£10,000", "28")}'" in {
            doc.select("#secondBand-text").text shouldBe summaryMessages.taxRate("£10,000", "28")
          }

          "has the value '£100'" in {
            doc.select("#secondBand-amount").text shouldBe "£100"
          }
        }
      }
    }

    "a Brought Forward Loss is entered and none remains" should {

      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(true)),
        broughtForwardValueModel = Some(LossesBroughtForwardValueModel(35.00))
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = -20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = Some(10000.00),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(500)),
        prrUsed = Some(BigDecimal(125)),
        broughtForwardLossesUsed = 35,
        allowableLossesUsed = 0,
        baseRateTotal = 30000,
        upperRateTotal = 15000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for brought forward losses used" which {
        s"has the title ${summaryMessages.broughtForwardLossesUsed}" in {
          doc.select("#lossesUsed-text").text shouldBe summaryMessages.broughtForwardLossesUsed
        }

        "has the amount of '£35'" in {
          doc.select("#lossesUsed-amount").text shouldBe "£35"
        }
      }

      "not have a row for brought forward losses remaining" in {
        doc.select("#broughtForwardLossesRemaining-text") shouldBe empty
      }

    }

    "the share was bought before 31 March 1982" should {

      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = true,
        valueBeforeLegislationStart = Some(350000.00),
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = None,
        secondRate = None,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 30000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for acquisition value" which {
        s"has the text '${summaryMessages.acquisitionValueBeforeLegislation}'" in {
          doc.select("#acquisitionValueBeforeLegislation-text").text shouldBe summaryMessages.acquisitionValueBeforeLegislation
        }

        "has the value '£350,000'" in {
          doc.select("#acquisitionValueBeforeLegislation-amount").text shouldBe "£350,000"
        }
      }

    }

    "the share was inherited" should {
      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = None,
        worthWhenInherited = Some(300000),
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(true)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = None,
        secondRate = None,
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 30000
      )
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a row for acquisition value" which {
        s"has the text '${summaryMessages.acquisitionValue}'" in {
          doc.select("#acquisitionValueWhenInherited-text").text shouldBe summaryMessages.acquisitionValue
        }

        "has the value '£300,000'" in {
          doc.select("#acquisitionValueWhenInherited-amount").text shouldBe "£300,000"
        }
      }
    }

    "share was sold outside known tax years" should {

      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = Some(LossesBroughtForwardValueModel(36.00))
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 10,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 20000,
        firstRate = 18,
        secondBand = Some(10000.00),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(500)),
        prrUsed = Some(BigDecimal(125)),
        broughtForwardLossesUsed = 35,
        allowableLossesUsed = 0,
        baseRateTotal = 30000,
        upperRateTotal = 15000
      )
      val taxYearModel = TaxYearModel("2018/19", isValidYear = false, "2016/17")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results,
        taxYearModel, 100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      s"display a notice summary with text ${summaryMessages.noticeSummary}" in {
        doc.select(".govuk-warning-text").text should include(summaryMessages.noticeSummary)
      }
    }

    "the shares only have tax on the upper band" should {
      val gainAnswers = GainAnswersModel(
        disposalDate = Dates.constructDate(10, 10, 2015),
        disposalValue = Some(100000),
        worthWhenSoldForLess = None,
        disposalCosts = BigDecimal(10000),
        acquisitionValue = Some(0),
        worthWhenInherited = None,
        acquisitionCosts = BigDecimal(10000),
        soldForLessThanWorth = false,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false)
      )
      val deductionAnswers = DeductionGainAnswersModel(
        broughtForwardModel = Some(LossesBroughtForwardModel(false)),
        broughtForwardValueModel = None
      )
      val results = TotalGainAndTaxOwedModel(
        gain = 50000,
        chargeableGain = 20000,
        aeaUsed = 0,
        deductions = 30000,
        taxOwed = 3600,
        firstBand = 0,
        firstRate = 0,
        secondBand = Some(10000),
        secondRate = Some(28),
        lettingReliefsUsed = Some(BigDecimal(0)),
        prrUsed = Some(BigDecimal(0)),
        broughtForwardLossesUsed = 0,
        allowableLossesUsed = 0,
        baseRateTotal = 101,
        upperRateTotal = 100
      )

      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = finalSummaryPartialView(gainAnswers, deductionAnswers, results, taxYearModel,
        100, 100)(fakeRequestWithSession, mockMessage)
      lazy val doc = Jsoup.parse(view.body)

      "has a numeric output row and a tax rate" which {

        "has no row for first band" in {
            doc.select("#firstBand-amount").size() shouldBe 0
        }

        "has row for second band" which {

          s"has the text '${summaryMessages.taxRate("£10,000", "28")}'" in {
            doc.select("#secondBand-text").text shouldBe summaryMessages.taxRate("£10,000", "28")
          }

          "has the value '£100'" in {
            doc.select("#secondBand-amount").text shouldBe "£100"
          }
        }
      }
    }
  }
}
