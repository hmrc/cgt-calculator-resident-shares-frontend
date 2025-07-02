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

import assets.MessageLookup
import com.codahale.metrics.SharedMetricRegistries
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import models.resident.DisposalDateModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.MessagesControllerComponents
import play.api.mvc.Results.Redirect
import play.api.test.Helpers.*
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException
import views.html.calculation.whatNext.{whatNextSAFourTimesAEA, whatNextSAGain, whatNextSANoGain}

import java.time.*
import scala.concurrent.Future

class WhatNextSaControllerSpec extends CommonPlaySpec with FakeRequestHelper with MockitoSugar with WithCommonFakeApplication {

  val date: LocalDate = LocalDate.of(2016, 5, 8)
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val whatNextSAFourTimesAEAView: whatNextSAFourTimesAEA = fakeApplication.injector.instanceOf[whatNextSAFourTimesAEA]
  val whatNextSAGainView: whatNextSAGain = fakeApplication.injector.instanceOf[whatNextSAGain]
  val whatNextSANoGainView: whatNextSANoGain = fakeApplication.injector.instanceOf[whatNextSANoGain]

  def setupController(disposalDate: DisposalDateModel): WhatNextSAController = {
    SharedMetricRegistries.clear()

    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel]
      (ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(disposalDate)))

    new WhatNextSAController(mockSessionCacheService, mockMCC, mockConfig, whatNextSAFourTimesAEAView,
      whatNextSAGainView, whatNextSANoGainView)
  }

  def setupControllerNoSession(): WhatNextSAController = {
    SharedMetricRegistries.clear()

    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel]
      (ArgumentMatchers.any())(using ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(None))

    new WhatNextSAController(mockSessionCacheService, mockMCC, mockConfig, whatNextSAFourTimesAEAView,
      whatNextSAGainView, whatNextSANoGainView)
  }

  "Calling .whatNextSAOverFourTimesAEA" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAOverFourTimesAEA(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAOverFourTimesAEA(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the WhatNextFourTimesAEA page" in {
        Jsoup.parse(contentAsString(result)).select("#main-content > div > div > p:nth-child(2)").text should include(MessageLookup.WhatNextPages.FourTimesAEA.paragraphOne)
      }

      "have a back link to the confirm-sa page" in {
        Jsoup.parse(contentAsString(result)).getElementsByClass("govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .whatNextSANoGain" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSANoGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSANoGain(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the WhatNextFourTimesAEA page" in {
        Jsoup.parse(contentAsString(result)).select("#main-content").text should include(MessageLookup.WhatNextPages.WhatNextNoGain.bulletPointTitle)
      }

      "have a back link to the confirm-sa page" in {
        Jsoup.parse(contentAsString(result)).select("a.govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Calling .whatNextSAGain" when {

    "provided with an invalid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController(DisposalDateModel(8, 5, 2016))
      lazy val result = controller.whatNextSAGain(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the WhatNextFourTimesAEA page" in {
        Jsoup.parse(contentAsString(result)).select("#main-content").text should include(MessageLookup.WhatNextPages.WhatNextGain.bulletPointTitle)
      }

      "have a back link to the confirm-sa page" in {
        Jsoup.parse(contentAsString(result)).select("a.govuk-back-link").attr("href") shouldEqual "#"
      }
    }
  }

  "Empty controller" when {
    "called with .whatNextSaGain" should {
      "redirect to start" in {
        lazy val controller = setupControllerNoSession()
        val nextSaGainController = controller.whatNextSAGain(fakeRequestWithSession)
        val result = intercept[ApplicationException](await(nextSaGainController))
        result.result shouldBe Redirect("/calculate-your-capital-gains/resident/shares/session-timeout", 303)
      }
    }

    "called with .whatNextSaNoGain" should {
      "redirect to start" in {
        lazy val controller = setupControllerNoSession()
        val nextSaNoGainController = controller.whatNextSANoGain(fakeRequestWithSession)
        val result = intercept[ApplicationException](await(nextSaNoGainController))
        result.result shouldBe Redirect("/calculate-your-capital-gains/resident/shares/session-timeout", 303)
      }
    }
  }
}
