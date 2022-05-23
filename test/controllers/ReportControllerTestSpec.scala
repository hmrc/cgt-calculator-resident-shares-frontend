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

package controllers

import common.CommonPlaySpec
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class ReportControllerTestSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar {

  def buildApp(properties: Map[String, String]): Application = {
    new GuiceApplicationBuilder().configure(properties + ("metrics.enabled" -> "false")).build()
  }

  "host" should {
    "return a `https` host when `platform frontend host` has been set in configuration (indicates a MDTP environment)" in {
      implicit val application: Application = buildApp(Map("platform.frontend.host" -> "https://www.qa.tax.service.gov.uk/"))
      val controller = application.injector.instanceOf[ReportController]
      controller.host(fakeRequest) shouldBe "https://localhost"
    }
    "return a `http` host when `platform frontend host` has NOT been set in configuration (indicates localhost)" in {
      implicit val application: Application = buildApp(Map.empty)
      val controller = application.injector.instanceOf[ReportController]
      controller.host(fakeRequest) shouldBe "http://localhost"
    }
  }

}
