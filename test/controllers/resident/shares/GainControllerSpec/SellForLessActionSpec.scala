/*
 * Copyright 2016 HM Revenue & Customs
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

package controllers.resident.shares.GainControllerSpec

import assets.MessageLookup.Resident.Shares.{SellForLess => messages}
import common.KeystoreKeys.{ResidentShareKeys => keyStoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.{GainController, routes}
import models.resident.{DisposalDateModel, SellForLessModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SellForLessActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[SellForLessModel], knownTaxYear: Boolean = true): GainController= {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(TaxYearModel("year", knownTaxYear, "year"))))

    when(mockCalcConnector.fetchAndGetFormData[SellForLessModel](Matchers.eq(keyStoreKeys.sellForLess))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keyStoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(DisposalDateModel(1, 1, 1))))

    when(mockCalcConnector.saveFormData[SellForLessModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .sellForLess from the resident shares GainController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual messages.title
      }
    }

    "in a known tax year" should {
      lazy val target = setupTarget(None)
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "have a back link to the disposal date page" in {
        Jsoup.parse(bodyOf(result)).getElementById("back-link").attr("href") shouldEqual routes.GainController.disposalDate().url
      }
    }

    "outside a known tax year" should {
      lazy val target = setupTarget(None, false)
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "have a back link to the outside tax years page" in {
        Jsoup.parse(bodyOf(result)).getElementById("back-link").attr("href") shouldEqual routes.GainController.outsideTaxYears().url
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(SellForLessModel(true)))
      lazy val result = target.sellForLess(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.sellForLess(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitSellForLess from the resident shares GainCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("sellForLess", "Yes"))
      lazy val result = target.submitSellForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the worth when sold page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/worth-when-sold-for-less")
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("sellForLess", "No"))
      lazy val result = target.submitSellForLess(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the disposal value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/disposal-value")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("sellForLess", ""))
      lazy val result = target.submitSellForLess(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Sell For Less page" in {
        doc.title() shouldEqual messages.title
      }
    }
  }
}
