/*
 * Copyright 2023 HM Revenue & Customs
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

package util.helper

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

trait ViewBehaviours extends AnyWordSpec with Matchers {


  def pageWithExpectedMessage(check: (String, String))
                             (implicit document: Document): Unit = {
    val (cssSelector, message) = check
    s"element with class '$cssSelector' have message '$message'" in {
        val elem = document.select(cssSelector)
        elem.text() mustBe message
    }
  }

  val labelStyle = "label.govuk-label.govuk-label--l"
  val headingStyle = "h1.govuk-heading-l"
  val legendHeadingStyle = "legend.govuk-fieldset__legend--l h1.govuk-fieldset__heading"

}
