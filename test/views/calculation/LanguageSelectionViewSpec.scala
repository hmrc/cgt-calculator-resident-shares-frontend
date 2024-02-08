/*
 * Copyright 2024 HM Revenue & Customs
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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Lang
import play.api.mvc.{Call, MessagesControllerComponents}
import views.html.calculation.language_selection

class LanguageSelectionViewSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  val appConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val languageSelectionView = fakeApplication.injector.instanceOf[language_selection]

  val langMap: Map[String, Lang] = Map.empty
  val langToCall: String => Call =_ => Call("", "")
  val customClass: Option[String] = None
  val appName: Option[String] = None

  "Language selection view" should {

    "generate the same template when .render and .f are called" in {

      val f = languageSelectionView.f(langMap, langToCall, customClass, appName)(messages)

      val render = languageSelectionView.render(langMap, langToCall, customClass, appName, messages)

      f shouldBe render
    }
  }
}
