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

package views.helpers

import org.jsoup.Jsoup
import play.twirl.api.Html
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.helpers.expandableHelpTextHelper
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

class ExpandableHelpTextHelperSpec extends UnitSpec with WithFakeApplication{

  lazy val content = expandableHelpTextHelper("testQ", Html("someHtml"))(applicationMessages)
  lazy val doc = Jsoup.parse(content.body)

  "Expandable Help Text Helper" should {

    "have a details tag" which {

      lazy val details = doc.select("details#help")

      "has the id 'help'" in {
        details.attr("id") shouldBe "help"
      }

      "has the role of 'group'" in {
        details.attr("role") shouldBe "group"
      }
    }

    "have a header summary" which {

      lazy val summary = doc.select("summary")

      "has the role 'button'" in {
        summary.attr("role") shouldBe "button"
      }

      "has 'aria-controls' of 'details-content-0" in {
        summary.attr("aria-controls") shouldBe "details-content-0"
      }

      "has span with a class of 'summary'" in {
        summary.select("span").hasClass("summary") shouldBe true
      }

      "contains text 'testQ'" in {
        summary.select("summary").text shouldBe "testQ"
      }
    }

    "have hidden html" which {

      lazy val hiddenHtml = doc.select("div")

      "has the class 'panel-indent'" in {
        hiddenHtml.hasClass("panel-indent") shouldBe true
      }

      "contains additional html text" in {
        hiddenHtml.text shouldBe "someHtml"
      }

    }
  }
}
