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

package controllers

import akka.stream.Materializer
import akka.util.Timeout
import assets.MessageLookup
import com.codahale.metrics.SharedMetricRegistries
import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers.redirectLocation
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration.Duration

class WhatNextNonSaControllerSpec extends UnitSpec with FakeRequestHelper with OneAppPerSuite with MockitoSugar {

  implicit val timeout: Timeout = new Timeout(Duration.create(20, "seconds"))
  lazy val materializer = mock[Materializer]


  def setupController(): WhatNextNonSaController = {
    SharedMetricRegistries.clear()
    implicit val mockAppConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
//      new AppConfig {
//      override val assetsPrefix: String = ""
//      override val residentIFormUrl: String = "iform-url"
//      override val reportAProblemNonJSUrl: String = ""
//      override val contactFrontendPartialBaseUrl: String = ""
//      override val analyticsHost: String = ""
//      override val analyticsToken: String = ""
//      override val reportAProblemPartialUrl: String = ""
//      override val contactFormServiceIdentifier: String = ""
//      override val urBannerLink: String = ""
//      override val feedbackSurvey: String = ""
//    }
    new WhatNextNonSaController()
  }
  "Calling .whatNextNonSaGain" when {

    "provided with an invalid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaGain(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaGain(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the What Next Non-SA Gain page" in {
        doc.title() shouldBe MessageLookup.WhatNextNonSaGain.title
      }

      "have a link to the iForm from app config" in {
        doc.select("#report-now > a").attr("href") shouldBe "https://www.tax.service.gov.uk/forms/form/capital-gains-tax-real-time-transaction-return/new"
      }
    }
  }

  "Calling .whatNextNonSaLoss" should {

    "provided with an invalid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaLoss(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout view" in {
        redirectLocation(result).get should include("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }

    "provided with a valid session" should {
      lazy val controller = setupController()
      lazy val result = controller.whatNextNonSaLoss(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result)(materializer))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "load the What Next Non-SA Loss page" in {
        doc.title() shouldBe MessageLookup.WhatNextNonSaLoss.title
      }

      "have a link to the iForm from app config" in {
        doc.select("#report-now > a").attr("href") shouldBe "https://www.tax.service.gov.uk/forms/form/capital-gains-tax-real-time-transaction-return/new"
      }
    }
  }
}
