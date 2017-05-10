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

import assets.MessageLookup.{WhatNextNonSaLoss => messages}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.Play.current
import play.api.i18n.Messages.Implicits.applicationMessages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.calculation.{whatNext => views}

class WhatNextNonSaLossViewSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper {

  "whatNextNonSaLoss view" should {

    lazy val view = views.whatNextNonSaLoss("iFormUrl")(fakeRequestWithSession, applicationMessages)
    lazy val doc = Jsoup.parse(view.body)

    "have charset UTF-8" in {
      doc.charset().toString shouldBe "UTF-8"
    }

    s"return a title of ${messages.title}" in {
      doc.title shouldBe messages.title
    }

    s"have a heading of ${messages.title}" in {
      doc.select("h1").text shouldBe messages.title
    }

    s"have a first paragraph with text ${messages.detailsOne}" in {
      doc.select("#details-one").text shouldBe messages.detailsOne
    }

    s"have a second paragraph with text ${messages.detailsTwo}" in {
      doc.select("#details-two").text shouldBe messages.detailsTwo
    }

    "have a report now link" which {

      lazy val reportNow = doc.select("#report-now").select("a")

      s"has text ${messages.reportNow}" in {
        reportNow.text shouldBe messages.reportNow
      }

      "has a link to 'iFormUrl'" in {
        reportNow.attr("href") shouldBe "iFormUrl"
      }
    }

    "have a return to GOV.UK link" which {

      lazy val govUk = doc.select("#govUk").select("a")

      s"has text ${messages.govUk}" in {
        govUk.text shouldBe messages.govUk
      }

      "has a link to 'http://www.gov.uk'" in {
        govUk.attr("href") shouldBe "http://www.gov.uk"
      }
    }
  }
}
