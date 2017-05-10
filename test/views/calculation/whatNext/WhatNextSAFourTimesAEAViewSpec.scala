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

package views.calculation.whatNext

import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{whatNext => views}
import assets.MessageLookup.WhatNextPages.{FourTimesAEA => pageMessages}
import assets.MessageLookup.{WhatNextPages => commonMessages}

class WhatNextSAFourTimesAEAViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "The whatNextSAFourTimesAEA view" should {

    lazy val view = views.whatNextSAFourTimesAEA("back-link")(fakeRequest, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    s"have a title ${commonMessages.title}" in {
      doc.title() shouldBe commonMessages.title
    }

    "have a back link to 'back-link'" in {
      doc.select("a#back-link").attr("href") shouldBe "back-link"
    }

    "have the correct heading" in {
      doc.select("h1").text shouldBe commonMessages.title
    }

    s"have the first paragraph of ${pageMessages.paragraphOne}" in {
      doc.select("article.content__body p").get(0).text shouldBe pageMessages.paragraphOne
    }

    s"have the second paragraph of ${pageMessages.paragraphTwo}" in {
      doc.select("article.content__body p").get(1).text shouldBe pageMessages.paragraphTwo
    }

    "have a finish button" which {

      lazy val finishButton = doc.select("article.content__body #finish")

      s"has the text ${commonMessages.finish}" in {
        finishButton.text shouldBe commonMessages.finish
      }

      "has a link to the 'www.gov.uk' page" in {
        finishButton.attr("href") shouldBe "http://www.gov.uk"
      }
    }
  }
}

