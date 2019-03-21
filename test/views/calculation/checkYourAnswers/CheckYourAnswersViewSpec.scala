/*
 * Copyright 2019 HM Revenue & Customs
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

package views.calculation.checkYourAnswers

import controllers.helpers.FakeRequestHelper
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import views.html.calculation.{checkYourAnswers => views}
import assets.MessageLookup.Resident.Shares.{ReviewAnswers => messages}
import assets.MessageLookup.{Resident => commonMessages}
import assets.ModelsAsset._
import config.ApplicationConfig
import org.jsoup.nodes.Document
import play.api.i18n.Lang
import play.api.mvc.{Call, MessagesControllerComponents}
import play.twirl.api.HtmlFormat

class CheckYourAnswersViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {
  implicit lazy val mockMessage = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val fakeLang: Lang = Lang("en")
  val dummyBackLink = "backLink"
  val dummyPostCall: Call = Call("POST", "/dummy-url")
  val mockConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  lazy val view: HtmlFormat.Appendable = views.checkYourAnswers(dummyPostCall, dummyBackLink, gainAnswersMostPossibles,
    Some(deductionAnswersMostPossibles), Some(taxYearModel), Some(incomeAnswers))(fakeRequestWithSession,  mockMessage, fakeApplication, fakeLang, mockConfig)
  lazy val doc: Document = Jsoup.parse(view.body)

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

    s"has a link to '$dummyBackLink'" in {
      backLink.attr("href") shouldBe dummyBackLink
    }
  }

  s"have a page heading" which {

    s"includes a secondary heading with text '${messages.title}'" in {
      doc.select("h1.heading-large").text shouldBe messages.title
    }
  }

  "have a section for the check your answers" in {
    doc.select("section").attr("id") shouldBe "yourAnswers"
  }

  "have a form" which {

    lazy val form = doc.getElementsByTag("form")

    s"has the action '/dummy-url'" in {
      form.attr("action") shouldBe "/dummy-url"
    }

    "has the method of POST" in {
      form.attr("method") shouldBe "POST"
    }
  }

  "have a continue button that" should {

    lazy val continueButton = doc.select("button#continue-button")

    s"have the button text '${commonMessages.continue}'" in {
      continueButton.text shouldBe commonMessages.continue
    }

    "be of type submit" in {
      continueButton.attr("type") shouldBe "submit"
    }

    "have the class 'button'" in {
      continueButton.hasClass("button") shouldBe true
    }
  }
}

