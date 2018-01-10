/*
 * Copyright 2018 HM Revenue & Customs
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

package config

import org.jsoup.Jsoup
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.FrontendGlobal._
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

class FrontendGlobalSpec extends UnitSpec with WithFakeApplication {

  //############# Tests for homeLink ##########################
  "Rendering the error_template by causing an error" when {

    "on the resident/shares journey" should {

      s"have a link to the resident/shares start journey '${controllers.routes.GainController.disposalDate().url}'" in {

        val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/resident/shares/banana").withSession(SessionKeys.sessionId -> "12345")
        val result = standardErrorTemplate("test", "teat-heading", "test-message")(fakeRequest)
        val doc = Jsoup.parse(result.body)

        doc.getElementById("homeNavHref").attr("href") shouldBe controllers.routes.GainController.disposalDate().url
      }

    }

    "on a journey which does not exist" should {

      "have a link to the non-resident start journey" in {

        val fakeRequest = FakeRequest("GET", "/calculate-your-capital-gains/error").withSession(SessionKeys.sessionId -> "12345")
        val result = standardErrorTemplate("test", "teat-heading", "test-message")(fakeRequest)
        val doc = Jsoup.parse(result.body)

        doc.getElementById("homeNavHref").attr("href") shouldBe "/calculate-your-capital-gains/resident/shares/disposal-date"
      }

    }
  }
}
