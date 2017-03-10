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

import assets.MessageLookup.Resident.{Shares => SharesMessages}
import assets.MessageLookup.{SummaryPage => messages}
import assets.{MessageLookup => commonMessages}
import common.Dates._
import controllers.helpers.FakeRequestHelper
import models.resident.TaxYearModel
import models.resident.shares.GainAnswersModel
import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{report => views}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class SharesGainReportViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper{

  "Summary view" when {

    "property acquired after start of tax (1 April 1982) and not inherited" should {

      val testModel = GainAnswersModel(
        disposalDate = constructDate(12, 9, 1990),
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

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel)(fakeRequest, applicationMessages)
      lazy val doc = Jsoup.parse(view.body)

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

      "have the hmrc logo with the hmrc name" in {
        doc.select("div.logo span").text shouldBe "HM Revenue & Customs"
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

          s"should have the question text '${commonMessages.SharesDisposalDate.title}'" in {
            doc.select("#disposalDate-question").text shouldBe commonMessages.SharesDisposalDate.title
          }

          "should have the value '12 September 1990'" in {
            doc.select("#disposalDate-date span.bold-medium").text shouldBe "12 September 1990"
          }
        }

        "has a numeric output row for the Disposal Value" which {

          s"should have the question text '${commonMessages.Resident.Shares.DisposalValue.question}'" in {
            doc.select("#disposalValue-question").text shouldBe commonMessages.Resident.Shares.DisposalValue.question
          }

          "should have the value '£10'" in {
            doc.select("#disposalValue-amount span.bold-medium").text shouldBe "£10"
          }
        }

        "has a numeric output row for the Disposal Costs" which {

          s"should have the question text '${commonMessages.SharesDisposalCosts.title}'" in {
            doc.select("#disposalCosts-question").text shouldBe commonMessages.SharesDisposalCosts.title
          }

          "should have the value '£20'" in {
            doc.select("#disposalCosts-amount span.bold-medium").text shouldBe "£20"
          }

        }

        "has a numeric output row for the Acquisition Value" which {

          s"should have the question text '${commonMessages.SharesAcquisitionValue.title}'" in {
            doc.select("#acquisitionValue-question").text shouldBe commonMessages.SharesAcquisitionValue.title
          }

          "should have the value '£30'" in {
            doc.select("#acquisitionValue-amount span.bold-medium").text shouldBe "£30"
          }

        }

        "has a numeric output row for the Acquisition Costs" which {

          s"should have the question text '${commonMessages.SharesAcquisitionCosts.title}'" in {
            doc.select("#acquisitionCosts-question").text shouldBe commonMessages.SharesAcquisitionCosts.title
          }

          "should have the value '£40'" in {
            doc.select("#acquisitionCosts-amount span.bold-medium").text shouldBe "£40"
          }
        }
      }
    }

    "property acquired after the start of the tax (1 April 1982) and inherited" should {

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


      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel)(fakeRequest, applicationMessages)
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

    "property acquired before start of tax (1 April 1982)" should {

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

      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val view = views.gainSummaryReport(testModel, -2000, taxYearModel)(fakeRequest, applicationMessages)
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

  "Summary when shares have been sold for less" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

    val testModel = GainAnswersModel(
      disposalDate = constructDate(12, 9, 2015),
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

  "Summary when supplied with a date outside the known tax years and no gain or loss" should {

    lazy val taxYearModel = TaxYearModel("2018/19", false, "2016/17")

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
    lazy val view = views.gainSummaryReport(testModel, 0, taxYearModel)(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "should have the question text 'Total gain'" in {
      doc.select("#gain-question").text shouldBe messages.totalGain
    }

    "have the class notice-wrapper" in {
      doc.select("div.notice-wrapper").isEmpty shouldBe false
    }

    s"have the text ${messages.noticeWarning("2016/17")}" in {
      doc.select("strong.bold-small").text shouldBe messages.noticeWarning("2016/17")
    }
  }
}
