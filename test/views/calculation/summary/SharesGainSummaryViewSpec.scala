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

import assets.{MessageLookup => pages}
import assets.MessageLookup.{SummaryPage => messages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.MessageLookup.Resident.{Shares => SharesMessages}
import assets.MessageLookup.{SummaryDetails => summaryMessages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import controllers.routes
import models.resident.TaxYearModel
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{summary => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SharesGainSummaryViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "Summary view" when {

    "acquired after start of tax (1 April 1982) and not inherited" should {

      val testModel = GainAnswersModel(
        disposalDate = constructDate(12, 12, 2019),
        soldForLessThanWorth = false,
        disposalValue = Some(10),
        worthWhenSoldForLess = None,
        disposalCosts = 20,
        ownerBeforeLegislationStart = false,
        valueBeforeLegislationStart = None,
        inheritedTheShares = Some(false),
        worthWhenInherited = None,
        acquisitionValue = Some(10000),
        acquisitionCosts = 40
      )

      lazy val taxYearModel = TaxYearModel("2016/17", true, "2016/17")
      lazy val view = views.gainSummary(testModel, -100, taxYearModel, "home-link", 150 , 11000)(fakeRequest, applicationMessages)
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

        s"has the url ${routes.ReviewAnswersController.reviewGainAnswers().toString}" in {
          backLink.attr("href") shouldEqual routes.ReviewAnswersController.reviewGainAnswers().toString
        }
      }

      "have a home link to 'home-link'" in {
        doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
      }

      "has a banner" which {
        lazy val banner = doc.select("#tax-owed-banner")

        "contains a h1" which {
          lazy val h1 = banner.select("h1")

          s"has the text '£0.00'" in {
            h1.text() shouldEqual "£0.00"
          }
        }

        "contains a h2" which {
          lazy val h2 = banner.select("h2")

          s"has the text ${summaryMessages.cgtToPay("2016 to 2017")}" in {
            h2.text() shouldEqual summaryMessages.cgtToPay("2016 to 2017")
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

        "has a div for total loss" which {

          lazy val div = doc.select("#yourTotalLoss")

          "has a h3 tag" which {

            s"has the text '${summaryMessages.yourTotalLoss}'" in {
              div.select("h3").text shouldBe summaryMessages.yourTotalLoss
            }
          }

          "has a row for disposal value" which {
            s"has the text '${summaryMessages.disposalValue}'" in {
              div.select("#disposalValue-text").text shouldBe summaryMessages.disposalValue
            }

            "has the value '£10'" in {
              div.select("#disposalValue-amount").text shouldBe "£10"
            }
          }

          "has a row for acquisition value" which {
            s"has the text '${summaryMessages.acquisitionValue}'" in {
              div.select("#acquisitionValue-text").text shouldBe summaryMessages.acquisitionValue
            }

            "has the value '£10,000'" in {
              div.select("#acquisitionValue-amount").text shouldBe "£10,000"
            }
          }

          "has a row for total costs" which {
            s"has the text '${summaryMessages.totalCosts}'" in {
              div.select("#totalCosts-text").text shouldBe summaryMessages.totalCosts
            }

            "has the value '£150'" in {
              div.select("#totalCosts-amount").text shouldBe "£150"
            }
          }

          "has a row for total loss" which {
            s"has the text '${summaryMessages.totalLoss}'" in {
              div.select("#totalLoss-text").text shouldBe summaryMessages.totalLoss
            }

            "has the value '£100'" in {
              div.select("#totalLoss-amount").text shouldBe "£100"
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

            "has the value '£0'" in {
              div.select("#aeaUsed-amount").text shouldBe "£0"
            }
          }

          "not have a row for brought forward losses used" in {
            div.select("#lossesUsed-text") shouldBe empty
          }

          "has a row for total deductions" which {

            s"has the text '${summaryMessages.totalDeductions}'" in {
              div.select("#totalDeductions-text").text shouldBe summaryMessages.totalDeductions
            }

            "has the value '£0'" in {
              div.select("#totalDeductions-amount").text shouldBe "£0"
            }
          }
        }

        "has a div for Taxable Gain" which {

          lazy val div = doc.select("#yourTaxableGain")

          "does not have a h3 tag" in {
            div.select("h3") shouldBe empty
          }

          "does not have a row for gain" in {
            div.select("#gain-text") shouldBe empty
          }

          "does not have a row for minus deductions" in {
            div.select("#minusDeductions-text") shouldBe empty
          }

          "has a row for taxable gain" which {
            s"has the text '${summaryMessages.taxableGain}'" in {
              div.select("#taxableGain-text").text shouldBe summaryMessages.taxableGain
            }

            "has the value '£0'" in {
              div.select("#taxableGain-amount").text shouldBe "£0"
            }
          }
        }

        "has a div for tax rate" which {

          lazy val div = doc.select("#yourTaxRate")

          "does not have a h3 tag" in {
            div.select("h3") shouldBe empty
          }

          "does not have a row for first band"  in {
            div.select("#firstBand-text") shouldBe empty
          }

          "does not have a row for second band" in {
            div.select("#secondBand-text") shouldBe empty
          }

          "has a row for tax to pay" which {

            s"has the text ${summaryMessages.taxToPay}" in {
              div.select("#taxToPay-text").text shouldBe summaryMessages.taxToPay
            }

            "has the value '£0'" in {
              div.select("#taxToPay-amount").text shouldBe "£0"
            }
          }
        }
      }

      "have a section for the Your remaining deductions" which {

        "has a div for remaining deductions" which {

          lazy val div = doc.select("#remainingDeductions")

          "has a h2 tag" which {

            s"has the text ${summaryMessages.remainingDeductions}" in {
              div.select("h2").text shouldBe summaryMessages.remainingDeductions
            }
          }

          "has a row for annual exempt amount left" which {
            s"has the text ${summaryMessages.remainingAnnualExemptAmount("2016 to 2017")}" in {
              div.select("#aeaLeft-text").text shouldBe summaryMessages.remainingAnnualExemptAmount("2016 to 2017")
            }

            "has the value '£11,000'" in {
              div.select("#aeaLeft-amount").text shouldBe "£11,000"
            }
          }

          "not have a row for brought forward losses remaining" in {
            div.select("#broughtForwardLossesRemaining-text") shouldBe empty
          }

          "has a row for losses to carry forward" which {
            s"has the text${summaryMessages.lossesToCarryForwardFromCalculation}" in {
              div.select("#lossesToCarryForwardFromCalc-text").text shouldBe summaryMessages.lossesToCarryForwardFromCalculation
            }

            "has the value '£100" in {
              div.select("#lossesToCarryForwardFromCalc-amount").text shouldBe "£100"
            }
          }
        }
      }

      "have a section for What to do next" which {
        lazy val section = doc.select("#whatToDoNext")

        "has a h2 tag" which {
          s"has the text ${summaryMessages.whatToDoNext}" in {
            section.select("h2").text shouldBe summaryMessages.whatToDoNext
          }
        }

        "has a paragraph" which {
          s"has the text ${summaryMessages.whatToDoNextDetails}" in {
            section.select("p").text shouldBe summaryMessages.whatToDoNextDetails
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

      "has a save as PDF Button" which {

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

            "has the class bold-small" in {
              link.hasClass("bold-small") shouldBe true
            }

            "has the class save-pdf-link" in {
              link.hasClass("save-pdf-link") shouldBe true
            }

            s"links to ${controllers.routes.ReportController.gainSummaryReport()}" in {
              link.attr("href") shouldBe controllers.routes.ReportController.gainSummaryReport().toString
            }

            s"has the text ${messages.saveAsPdf}" in {
              link.text shouldBe messages.saveAsPdf
            }
          }
        }
      }
    }
  }
}
