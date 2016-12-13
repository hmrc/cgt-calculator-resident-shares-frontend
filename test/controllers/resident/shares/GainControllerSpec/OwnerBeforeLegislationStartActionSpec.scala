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

import assets.MessageLookup.Resident.Shares.{OwnerBeforeLegislationStart => Messages}
import common.KeystoreKeys.{ResidentShareKeys => keyStoreKeys}
import connectors.CalculatorConnector
import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.GainController
import models.resident.shares.OwnerBeforeLegislationStartModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class OwnerBeforeLegislationStartActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[OwnerBeforeLegislationStartModel]): GainController= {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](Matchers.eq(keyStoreKeys.ownerBeforeLegislationStart))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[OwnerBeforeLegislationStartModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .ownedBeforeEightyTwo from the resident GainController" when {

    "request has a valid session and no keystore value" should {

      lazy val target = setupTarget(None)
      lazy val result = target.ownerBeforeLegislationStart(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      s"return some html with title of ${Messages.title}" in {
        contentType(result) shouldBe Some("text/html")
        Jsoup.parse(bodyOf(result)).title shouldEqual Messages.title
      }
    }

    "request has a valid session and some keystore value" should {

      lazy val target = setupTarget(Some(OwnerBeforeLegislationStartModel(true)))
      lazy val result = target.ownerBeforeLegislationStart(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

    }

    "request has an invalid session" should {

      lazy val target = setupTarget(None)
      lazy val result = target.ownerBeforeLegislationStart(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitOwnedBeforeEightyTwo from the resident GainCalculator" when {

    "a valid form with the answer 'Yes' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownerBeforeLegislationStart", "Yes"))
      lazy val result = target.submitOwnerBeforeLegislationStart(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the valueBeforeLegislationStart page" in {
        redirectLocation(result) shouldBe Some(controllers.resident.shares.routes.GainController.valueBeforeLegislationStart().toString)
      }
    }

    "a valid form with the answer 'No' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownerBeforeLegislationStart", "No"))
      lazy val result = target.submitOwnerBeforeLegislationStart(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the disposal value page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/did-you-inherit-them")
      }
    }

    "an invalid form with the answer '' is submitted" should {

      lazy val target = setupTarget(None)
      lazy val request = fakeRequestToPOSTWithSession(("ownerBeforeLegislationStart", ""))
      lazy val result = target.submitOwnerBeforeLegislationStart(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "render the Sell For Less page" in {
        doc.title() shouldEqual Messages.title
      }
    }
  }
}
