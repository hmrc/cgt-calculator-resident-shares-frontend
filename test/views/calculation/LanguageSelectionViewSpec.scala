/*
 * Copyright 2020 HM Revenue & Customs
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

package views.calculation

import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages}
import play.api.mvc.{Call, MessagesControllerComponents}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.{calculation => views}

class LanguageSelectionViewSpec extends UnitSpec with FakeRequestHelper with MockitoSugar with WithFakeApplication {

  val appConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val langMap: Map[String, Lang] = Map.empty
  val langToCall: String => Call =_ => Call("", "")
  val customClass: Option[String] = None
  val appName: Option[String] = None

  "Language selection view" should {

    "generate the same template when .render and .f are called" in {

      val f = views.language_selection.f(langMap, langToCall, customClass, appName)(messages)

      val render = views.language_selection.render(langMap, langToCall, customClass, appName, messages)

      f shouldBe render
    }
  }
}
