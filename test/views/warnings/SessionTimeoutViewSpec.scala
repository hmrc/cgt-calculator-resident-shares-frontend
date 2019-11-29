/*
 * Copyright 2019 HM Revenue & Customs
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

package views.warnings

import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.html.{warnings => views}

class SessionTimeoutViewSpec extends UnitSpec with FakeRequestHelper with MockitoSugar with WithFakeApplication {

  lazy val applicationConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  lazy val messages: Messages = fakeApplication.injector.instanceOf[MessagesControllerComponents].messagesApi.preferred(fakeRequest)

  val restartUrl: String = ""
  val homeLink: String = ""

  "Session timeout view" should {
    
    "generate the same template when .render and .f are called" in {

      val f = views.sessionTimeout.f(restartUrl, homeLink)(fakeRequest, messages, fakeApplication, applicationConfig)

      val render = views.sessionTimeout.render(restartUrl, homeLink, fakeRequest, messages, fakeApplication, applicationConfig)

      f shouldBe render
    }
  }
}