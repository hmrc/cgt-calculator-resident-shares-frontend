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
        acquisitionValue = Some(30),
        acquisitionCosts = 40
      )

      lazy val taxYearModel = TaxYearModel("2019/20", false, "2016/17")
      lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
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

        s"has the url ${routes.GainController.acquisitionCosts().toString}" in {
          backLink.attr("href") shouldEqual routes.GainController.acquisitionCosts().toString
        }
      }

      "have a home link to 'home-link'" in {
        doc.getElementById("homeNavHref").attr("href") shouldEqual "home-link"
      }

      s"have a page heading" which {

        s"includes a secondary heading with text '${messages.pageHeading}'" in {
          doc.select("h1 span.pre-heading").text shouldBe messages.pageHeading
        }

        "includes an amount of tax due of £0.00" in {
          doc.select("h1").text should include("£0.00")
        }
      }

      "have a notice summary" in {
        doc.select("div.notice-wrapper").isEmpty shouldBe false
      }

      s"have a section for the Calculation details" which {

        "has the class 'summary-section' to underline the heading" in {

          doc.select("section#calcDetails h2").hasClass("summary-underline") shouldBe true

        }

        s"has a h2 tag" which {

          s"should have the title '${messages.calcDetailsHeadingDate("2015/16")}'" in {
            doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2019/20")
          }

          "has the class 'heading-large'" in {
            doc.select("section#calcDetails h2").hasClass("heading-large") shouldBe true
          }
        }

        "has a numeric output row for the gain" which {

          "should have the question text 'Loss'" in {
            doc.select("#gain-question").text shouldBe messages.totalLoss
          }

          "should have the value '£2,000'" in {
            doc.select("#gain-amount").text shouldBe "£2,000"
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

          "should have the value '12 December 2019'" in {
            doc.select("#disposalDate-date").text shouldBe "12 December 2019"
          }

          s"should have a change link to ${routes.GainController.disposalDate().url}" in {
            doc.select("#disposalDate-change-link a").attr("href") shouldBe routes.GainController.disposalDate().url
          }

          "has the question as part of the link" in {
            doc.select("#disposalDate-change-link a").text shouldBe s"${commonMessages.change} ${pages.SharesDisposalDate.title}"
          }

          "has the question component of the link is visuallyhidden" in {
            doc.select("#disposalDate-change-link a span.visuallyhidden").text shouldBe pages.SharesDisposalDate.title
          }
        }

        "has a numeric output row for the Disposal Value" which {

          s"should have the question text '${SharesMessages.DisposalValue.question}'" in {
            doc.select("#disposalValue-question").text shouldBe SharesMessages.DisposalValue.question
          }

          "should have the value '£10'" in {
            doc.select("#disposalValue-amount").text shouldBe "£10"
          }

          s"should have a change link to ${routes.GainController.disposalValue().url}" in {
            doc.select("#disposalValue-change-link a").attr("href") shouldBe routes.GainController.disposalValue().url
          }

        }

        "has a numeric output row for the Disposal Costs" which {

          s"should have the question text '${pages.SharesDisposalCosts.title}'" in {
            doc.select("#disposalCosts-question").text shouldBe pages.SharesDisposalCosts.title
          }

          "should have the value '£20'" in {
            doc.select("#disposalCosts-amount").text shouldBe "£20"
          }

          s"should have a change link to ${routes.GainController.disposalCosts().url}" in {
            doc.select("#disposalCosts-change-link a").attr("href") shouldBe routes.GainController.disposalCosts().url
          }

        }

        "has an option/radiobutton output row for the Owned Before Start of Tax" which {

          s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
            doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
          }

          "should have the value 'No'" in {
            doc.select("#ownerBeforeLegislationStart-option").text shouldBe "No"
          }

          s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
            doc.select("#ownerBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
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
            doc.select("#inheritedTheShares-option").text shouldBe "No"
          }

          s"should have a change link to ${routes.GainController.didYouInheritThem().url}" in {
            doc.select("#inheritedTheShares-change-link a").attr("href") shouldBe routes.GainController.didYouInheritThem().url
          }
        }

        "does not have a numeric output row for the Inherited Value" in {
          doc.select("#worthWhenInherited-question").isEmpty shouldBe true
        }

        "has a numeric output row for the Acquisition Value" which {

          s"should have the question text '${pages.SharesAcquisitionValue.title}'" in {
            doc.select("#acquisitionValue-question").text shouldBe pages.SharesAcquisitionValue.title
          }

          "should have the value '£30'" in {
            doc.select("#acquisitionValue-amount").text shouldBe "£30"
          }

          s"should have a change link to ${routes.GainController.acquisitionValue().url}" in {
            doc.select("#acquisitionValue-change-link a").attr("href") shouldBe routes.GainController.acquisitionValue().url
          }

        }

        "has a numeric output row for the Acquisition Costs" which {

          s"should have the question text '${pages.SharesAcquisitionCosts.title}'" in {
            doc.select("#acquisitionCosts-question").text shouldBe pages.SharesAcquisitionCosts.title
          }

          "should have the value '£40'" in {
            doc.select("#acquisitionCosts-amount").text shouldBe "£40"
          }

          s"should have a change link to ${routes.GainController.acquisitionCosts().url}" in {
            doc.select("#acquisitionCosts-change-link a").attr("href") shouldBe routes.GainController.acquisitionCosts().url
          }

        }

        "does not display the section for what to do next" in {
          doc.select("#whatToDoNext").text shouldEqual ""
        }

        "display the save as PDF Button" which {

          "should render only one button" in {
            doc.select("a.save-pdf-button").size() shouldEqual 1
          }

          "with the class save-pdf-button" in {
            doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
          }

          s"with an href to ${routes.ReportController.gainSummaryReport().toString}" in {
            doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/gain-report"
          }

          s"have the text ${messages.saveAsPdf}" in {
            doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
          }
        }
      }
    }

    "acquired after start of tax (1 April 1982) and inherited" should {

      val testModel = GainAnswersModel(

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

      lazy val taxYearModel = TaxYearModel("2019/20", false, "2016/17")
      lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'No'" in {
          doc.select("#ownerBeforeLegislationStart-option").text shouldBe "No"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
          doc.select("#ownerBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
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
          doc.select("#inheritedTheShares-option").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.didYouInheritThem().url}" in {
          doc.select("#inheritedTheShares-change-link a").attr("href") shouldBe routes.GainController.didYouInheritThem().url
        }
      }

      "has a numeric output row for the Inherited Value" which {

        s"should have the question text '${SharesMessages.WorthWhenInherited.question}'" in {
          doc.select("#worthWhenInherited-question").text shouldBe SharesMessages.WorthWhenInherited.question
        }

        "should have the value '5000'" in {
          doc.select("#worthWhenInherited-amount").text shouldBe "£5,000"
        }

        s"should have a change link to ${routes.GainController.worthWhenInherited().url}" in {
          doc.select("#worthWhenInherited-change-link a").attr("href") shouldBe routes.GainController.worthWhenInherited().url
        }
      }

      "does not have a numeric output row for the Acquisition Value" in {
        doc.select("#acquisitionValue-question").isEmpty shouldBe true
      }
    }

    "acquired before start of tax (1 April 1982)" should {

      val testModel = GainAnswersModel(

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

      lazy val taxYearModel = TaxYearModel("2019/20", false, "2016/17")
      lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

      "has an option/radiobutton output row for the Owned Before Start of Tax" which {

        s"should have the question text '${SharesMessages.OwnerBeforeLegislationStart.title}'" in {
          doc.select("#ownerBeforeLegislationStart-question").text shouldBe SharesMessages.OwnerBeforeLegislationStart.title
        }

        "should have the value 'Yes'" in {
          doc.select("#ownerBeforeLegislationStart-option").text shouldBe "Yes"
        }

        s"should have a change link to ${routes.GainController.ownerBeforeLegislationStart().url}" in {
          doc.select("#ownerBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.ownerBeforeLegislationStart().url
        }
      }

      "has a numeric output row for the Worth on 31 March 1982 value" which {

        s"should have the question text '${SharesMessages.ValueBeforeLegislationStart.question}'" in {
          doc.select("#valueBeforeLegislationStart-question").text shouldBe SharesMessages.ValueBeforeLegislationStart.question
        }

        "should have the value '£700'" in {
          doc.select("#valueBeforeLegislationStart-amount").text shouldBe "£700"
        }

        s"should have a change link to ${routes.GainController.valueBeforeLegislationStart().url}" in {
          doc.select("#valueBeforeLegislationStart-change-link a").attr("href") shouldBe routes.GainController.valueBeforeLegislationStart().url
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

  "Summary when supplied with a date within the known tax years and a loss" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    val testModel = GainAnswersModel(

      disposalDate = constructDate(12, 9, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )
    lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "display the what to do next section" in {
      doc.select("#whatToDoNext").hasText shouldEqual true
    }

    s"display the title ${messages.whatToDoNextTitle}" in {
      doc.select("#whatToDoNextTitle").text shouldEqual messages.whatToDoNextTitle
    }

    s"display the text ${messages.whatToDoNextText}" in {
      doc.select("#whatToDoNextText").text shouldEqual s"${messages.whatNextYouCan}${messages.whatNextLink}${commonMessages.externalLink} ${messages.whatNextText}"
    }

    "have a link" which {

      "should have a href attribute" in {
        doc.select("#whatToDoNextLink").hasAttr("href") shouldEqual true
      }

      "should link to the what-you-pay-on-it govuk page" in {
        doc.select("#whatToDoNextLink").attr("href") shouldEqual "https://www.gov.uk/capital-gains-tax/losses"
      }

      "have the externalLink attribute" in {
        doc.select("#whatToDoNextLink").hasClass("external-link") shouldEqual true
      }

      "has a visually hidden span with the text opens in a new tab" in {
        doc.select("span#opensInANewTab").text shouldEqual commonMessages.externalLink
      }
    }

    "display the save as PDF Button" which {

      "should render only one button" in {
        doc.select("a.save-pdf-button").size() shouldEqual 1
      }

      "with the class save-pdf-button" in {
        doc.select("a.button").hasClass("save-pdf-button") shouldEqual true
      }

      s"with an href to ${routes.ReportController.gainSummaryReport().toString}" in {
        doc.select("a.save-pdf-button").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/gain-report"
      }

      s"have the text ${messages.saveAsPdf}" in {
        doc.select("a.save-pdf-button").text shouldEqual messages.saveAsPdf
      }
    }
  }

  "Summary when supplied with a date within the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2015),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )
    lazy val view = views.gainSummary(testModel, 0, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

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
      doc.select("div#whatToDoNextNoLossText a").text should include(s"${messages.whatToDoNextNoLossLinkShares}")
    }

    s"have a link to https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax" in {
      doc.select("div#whatToDoNextNoLossText a").attr("href") shouldBe "https://www.gov.uk/capital-gains-tax/report-and-pay-capital-gains-tax"
    }

    s"have the visually hidden text ${commonMessages.externalLink}" in {
      doc.select("div#whatToDoNextNoLossText span#opensInANewTab2").text shouldBe s"${commonMessages.externalLink}"
    }
  }

  "Summary when supplied with a date above the known tax years" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2018),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )
    lazy val view = views.gainSummary(testModel,-2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "does not display the what to do next content" in {
      doc.select("#whatToDoNext").isEmpty shouldBe true
    }
  }

  "Shares Deductions Summary when the shares were sold for less than they were worth." should {

    lazy val gainAnswers = GainAnswersModel(
      disposalDate = constructDate(10, 10, 2016),
      soldForLessThanWorth = true,
      disposalValue = None,
      worthWhenSoldForLess = Some(200000),
      disposalCosts = 10000,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(100000),
      acquisitionCosts = 10000
    )

    lazy val taxYearModel = TaxYearModel("2017/18", false, "2015/16")

    lazy val backLink = "/calculate-your-capital-gains/resident/shares/annual-exempt-amount"
    lazy val view = views.gainSummary(gainAnswers, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "has an option output row for private residence relief value in" which {

      s"should have the question text '${SharesMessages.WorthWhenSoldForLess.question}'" in {
        doc.select("#worthWhenSoldForLess-question").text shouldBe SharesMessages.WorthWhenSoldForLess.question
      }

      "should have the value '£200,000'" in {
        doc.select("#worthWhenSoldForLess-amount").text shouldBe "£200,000"
      }

      s"should have a change link to ${routes.GainController.worthWhenSoldForLess().url}" in {
        doc.select("#worthWhenSoldForLess-change-link a").attr("href") shouldBe routes.GainController.worthWhenSoldForLess().url
      }

      "has the question as part of the link" in {
        doc.select("#worthWhenSoldForLess-change-link a").text shouldBe s"${commonMessages.change} " +
          s"${SharesMessages.WorthWhenSoldForLess.question}"
      }

      "has the question component of the link as visuallyhidden" in {
        doc.select("#worthWhenSoldForLess-change-link a span.visuallyhidden").text shouldBe
          SharesMessages.WorthWhenSoldForLess.question
      }
    }
  }

  "Summary view with an out of tax year date" should {

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2013),
      soldForLessThanWorth = false,
      disposalValue = Some(10),
      worthWhenSoldForLess = None,
      disposalCosts = 20,
      ownerBeforeLegislationStart = false,
      valueBeforeLegislationStart = None,
      inheritedTheShares = Some(false),
      worthWhenInherited = None,
      acquisitionValue = Some(30),
      acquisitionCosts = 40
    )

    lazy val taxYearModel = TaxYearModel("2013/14", false, "2015/16")

    lazy val view = views.gainSummary(testModel, -2000, taxYearModel, "home-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

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

    s"has a h2 tag" which {

      s"should have the title '${messages.calcDetailsHeadingDate("2013/14")}'" in {
        doc.select("section#calcDetails h2").text shouldBe messages.calcDetailsHeadingDate("2013/14")
      }
    }
  }
}
