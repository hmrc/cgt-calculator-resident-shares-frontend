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
import views.html.error_template

class ErrorTemplateViewSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  lazy val mockAppConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val mockMessages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)
  lazy val errorTemplateView = fakeApplication.injector.instanceOf[error_template]

  val pageTitle = ""
  val heading = ""
  val message = ""
  val homeNavLink = ""

  "Error Template view" should {

    "generate the same template when .render and .f are called" in {

      val f = (errorTemplateView.f(pageTitle, heading, message, homeNavLink)
      (fakeRequest, mockMessages))

      val render = errorTemplateView.render(pageTitle, heading, message, homeNavLink,
        fakeRequest, mockMessages)

      f shouldBe render
    }
  }
}
