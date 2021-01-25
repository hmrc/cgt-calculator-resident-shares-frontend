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

package views.calculation

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import views.html.{calculation => views}
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.twirl.api.Html

class ResidentMainTemplateViewSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  lazy val messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val applicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]

  val title = ""
  val sidebarLinks: Option[Html] = None
  val contentHeader: Option[Html] = None
  val bodyClasses: Option[String] = None
  val mainClass: Option[String] = None
  val scriptElem: Option[Html] = None
  val isUserResearchBannerVisible = false
  val articleLayout = true
  val backLink: Option[String] = None
  val homeLink = ""
  val navTitle = ""
  val mainContent = Html("")

  "Resident shares template view" should {

    "generate the same template when .render and .f are called" in {

      val f = views.resident_main_template.f(title, sidebarLinks, contentHeader, bodyClasses, mainClass, scriptElem,
        isUserResearchBannerVisible, articleLayout, backLink, homeLink, navTitle)(mainContent)(fakeRequest,
        messages, fakeApplication, applicationConfig)

      val render = views.resident_main_template.render(title, sidebarLinks, contentHeader, bodyClasses, mainClass, scriptElem,
        isUserResearchBannerVisible, articleLayout, backLink, homeLink, navTitle, mainContent, fakeRequest,
        messages, fakeApplication, applicationConfig)

      f shouldBe render
    }
  }
}
