/*
 * Copyright 2021 HM Revenue & Customs
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

package views.calculation.gain

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.Html
import views.html.calculation.gain.resident_shares_main_template

class ResidentSharesMainTemplateViewSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  val appConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val messages: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  val residentSharesMainTemplateView = fakeApplication.injector.instanceOf[resident_shares_main_template]

  val title: String = ""
  val sidebarLinks: Option[Html] = None
  val contentHeader: Option[Html] = None
  val bodyClasses: Option[String] = None
  val mainClass: Option[String] = None
  val scriptElem: Option[Html] = None
  val articleLayout: Boolean = true
  val backLink: Option[String] = None
  val mainContent: Html = Html("")

  "Resident shares main template view" should {

    "generate the same template when .render and .f are called" in {

      val f = residentSharesMainTemplateView.f(title, sidebarLinks, contentHeader, bodyClasses, mainClass, scriptElem,
        articleLayout, backLink)(mainContent)(fakeRequest, messages)

      val render = residentSharesMainTemplateView.render(title, sidebarLinks, contentHeader, bodyClasses, mainClass, scriptElem,
        articleLayout, backLink, mainContent, fakeRequest, messages)

      f shouldBe render
    }
  }
}
