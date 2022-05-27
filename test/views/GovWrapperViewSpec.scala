/*
 * Copyright 2022 HM Revenue & Customs
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

package views

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.Html
import views.html.govuk_wrapper

class GovWrapperViewSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  lazy val applicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val govukWrapperView = fakeApplication.injector.instanceOf[govuk_wrapper]

  val title = "title"
  val mainClass = None
  val mainDataAttributes = None
  val bodyClasses = None
  val sidebar = Html("")
  val contentHeader = None
  val mainContent = Html("")
  val serviceInfoContent = Html("")
  val scriptElem = None
  val afterHeader = Html("")
  val homeLink = ""
  val navTitle = ""

  ".render and .f" should {

    "both generate identical templates" in {

      val render = govukWrapperView.render(title, mainClass, mainDataAttributes, bodyClasses, sidebar, contentHeader,
        mainContent, serviceInfoContent, scriptElem, afterHeader, homeLink, navTitle, fakeRequest, messages)

      val f = govukWrapperView.f(title, mainClass, mainDataAttributes, bodyClasses, sidebar, contentHeader,
        mainContent, serviceInfoContent, scriptElem, afterHeader, homeLink, navTitle)(fakeRequest, messages)

      f shouldBe render
    }
  }
}
