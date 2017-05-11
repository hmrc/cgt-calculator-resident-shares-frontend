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

import assets.MessageLookup.{SummaryDetails, Resident => commonMessages, SummaryPage => messages}
import common.Dates
import controllers.helpers.FakeRequestHelper
import models.resident._
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{summary => views}
import assets.DateAsset
import assets.{MessageLookup => pages}
import assets.MessageLookup.Resident.Shares.{SharesSummaryMessages => sharesSummaryMessages}
import common.Dates._
import controllers.routes
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel, PreviousTaxableGainsModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import models.resident.IncomeAnswersModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import assets.MessageLookup.{Resident => residentMessages, SummaryDetails => summaryMessages, SummaryPage => messages}

class SharesFinalSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "ShareFinalSummaryViewSpec" when {
    val incomeAnswers = IncomeAnswersModel(
      currentIncomeModel = Some(CurrentIncomeModel(0)),
      personalAllowanceModel = Some(PersonalAllowanceModel(0))
    )

    lazy val backLinkUrl: String = controllers.routes.ReviewAnswersController.reviewFinalAnswers().url

    "the share was sold inside tax years, bought after legislation start," +
      " with reliefs and brought forward losses and taxed at both tax bands" should {

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
      val taxYearModel = TaxYearModel("2015/16", isValidYear = true, "2015/16")

      lazy val view = views.finalSummary(gainAnswers,
        deductionAnswers,
        results,
        backLinkUrl,
        taxYearModel,
        "",
        100,
        100)(fakeRequestWithSession, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "have a charset of UTF-8" in {
        doc.charset().toString shouldBe "UTF-8"
      }

      s"have a title ${messages.title}" in {
        doc.title() shouldBe messages.title
      }

      "have a back button" which {

        lazy val backLink = doc.getElementById("back-link")

        "has the id 'back-link'" in {
          backLink.attr("id") shouldBe "back-link"
        }

        s"has the text '${residentMessages.back}'" in {
          backLink.text shouldBe residentMessages.back
        }

        s"has a link to ${controllers.routes.ReviewAnswersController.reviewFinalAnswers().url}" in {
          backLink.attr("href") shouldEqual controllers.routes.ReviewAnswersController.reviewFinalAnswers().url
        }
      }

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "contains a h1" which {
          lazy val h1 = banner.select("h1")

          s"has the text '£3,600.00'" in {
            h1.text() shouldEqual "£3,600.00"
          }
        }

        "contains a h2" which {
          lazy val h2 = banner.select("h2")

          s"has the text ${summaryMessages.cgtToPay("2015 to 2016")}" in {
            h2.text() shouldEqual summaryMessages.cgtToPay("2015 to 2016")
          }
        }
      }

      "does not have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe true
      }

      "have a section for the Calculation details" which {

        "has a h2 tag" which {

          s"has the text '${summaryMessages.howWeWorkedThisOut}'" in {
            doc.select("section#calcDetails h2").text shouldBe summaryMessages.howWeWorkedThisOut
          }
        }

        "has a div for total gain" which {

          lazy val div = doc.select("#yourTotalGain")

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTotalGain}'" in {
              div.select("h3").text shouldBe summaryMessages.yourTotalGain
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
              div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
            }

            "has the value '£0'" in {
              div.select("#acquisitionValue-amount").text shouldBe "£0"
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

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourDeductions}'" in {
              div.select("h3").text shouldBe summaryMessages.yourDeductions
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

          "has a row for brought forward losses used" which {
            s"has the text '${summaryMessages.broughtForwardLossesUsed}'" in {
              div.select("#lossesUsed-text").text shouldBe summaryMessages.broughtForwardLossesUsed
            }

            "has the value '£35'" in {
              div.select("#lossesUsed-amount").text shouldBe "£35"
            }
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

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTaxableGain}'" in {
              div.select("h3").text shouldBe summaryMessages.yourTaxableGain
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

          "has a h3 tag" which {

            s"has the text ${summaryMessages.yourTaxRate}" in {
              div.select("h3").text shouldBe summaryMessages.yourTaxRate
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

          "has a row for second band" which {
            s"has the text '${summaryMessages.taxRate("£10,000", "28")}'" in {
              div.select("#secondBand-text").text shouldBe summaryMessages.taxRate("£10,000", "28")
            }

            "has the value '£15,000'" in {
              div.select("#secondBand-amount").text shouldBe "£15,000"
            }
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

        "have a section for the You remaining deductions" which {

          "has a div for remaining deductions" which {

            lazy val div = doc.select("#remainingDeductions")

            "has a h2 tag" which {

              s"has the text ${summaryMessages.remainingDeductions}" in {
                div.select("h2").text shouldBe summaryMessages.remainingDeductions
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

      "has a section for the What to do next details" which {

        lazy val section = doc.select("#whatToDoNext")

        "has a h2 tag" which {
          s"has the text ${summaryMessages.whatToDoNextHeading}" in {
            section.select("h2").text shouldBe summaryMessages.whatToDoNextHeading
          }
        }

        "has a paragraph" which {
          s"has the text ${summaryMessages.whatToDoNextContinue}" in {
            section.select("p.font-small").text shouldBe summaryMessages.whatToDoNextContinue
          }
        }

        "has a save as PDF section" which {

          lazy val savePDFSection = doc.select("#save-as-a-pdf")

          "contains an internal div which" should {

            lazy val icon = savePDFSection.select("div")

            "has class icon-file-download" in {
              icon.hasClass("icon-file-download") shouldBe true
            }

            "contains a span" which {

              lazy val informationTag = icon.select("span")

              "has the class visuallyhidden" in {
                informationTag.hasClass("visuallyhidden") shouldBe true
              }

              "has the text Download" in {
                informationTag.text shouldBe "Download"
              }
            }

            "contains a link" which {

              lazy val link = savePDFSection.select("a")

              "has the type submit" in {
                link.attr("type").equals("submit") shouldBe true
              }

              "has the class bold-small" in {
                link.hasClass("bold-small") shouldBe true
              }

              "has the class save-pdf-link" in {
                link.hasClass("save-pdf-link") shouldBe true
              }

              s"links to ${controllers.routes.ReportController.finalSummaryReport()}" in {
                link.attr("href") shouldBe controllers.routes.ReportController.finalSummaryReport().toString()
              }

              s"has the text ${messages.saveAsPdf}" in {
                link.text shouldBe messages.saveAsPdf
              }
            }
          }
        }

        "has a continue button" which {
          s"has the text ${summaryMessages.continue}" in {
            doc.select("a.button").text shouldBe summaryMessages.continue
          }

          s"has a link to ${controllers.routes.SaUserController.saUser().url}" in {
            doc.select("a.button").attr("href") shouldBe controllers.routes.SaUserController.saUser().url
          }
        }
      }
    }
  }
}