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

package controllers

import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers.{defaultAwaitTimeout, status}

class FeedbackSurveyControllerSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  implicit val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMCC = fakeApplication.injector.instanceOf[MessagesControllerComponents]

  val feedbackSurveyController = new FeedbackSurveyController(mockConfig, mockMCC)

  "The FeedbackSurveyController" when {

    "redirecting to the feedback survey" should {

      "successfully redirect to the feedback survey" in {

        val result = feedbackSurveyController.redirectExitSurvey(fakeRequest)
        status(result) shouldBe 303
      }
    }
  }
}
