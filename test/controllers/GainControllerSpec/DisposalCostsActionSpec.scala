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

package controllers.GainControllerSpec

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import assets.MessageLookup.{SharesDisposalCosts => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.helpers.FakeRequestHelper
import controllers.GainController
import models.resident.DisposalCostsModel
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.test.Helpers._
import services.SessionCacheService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DisposalCostsActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()

  def setupTarget(getData: Option[DisposalCostsModel]): GainController = {

    val mockCalcConnector = mock[CalculatorConnector]
    val mockSessionCacheConnector = mock[SessionCacheConnector]
    val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]

    when(mockSessionCacheConnector.fetchAndGetFormData[DisposalCostsModel](ArgumentMatchers.eq(keystoreKeys.disposalCosts))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockSessionCacheConnector.saveFormData[DisposalCostsModel](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new GainController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
      override val sessionCacheConnector: SessionCacheConnector = mockSessionCacheConnector
      override val sessionCacheService: SessionCacheService = mockSessionCacheService
    }
  }

  "Calling .disposalCosts from the GainCalculationController with session" when {

    "supplied with no pre-existing stored data" should {

      lazy val target = setupTarget(None)
      lazy val result = target.disposalCosts(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Disposal Costs view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "supplied with pre-existing stored data" should {

      lazy val target = setupTarget(Some(DisposalCostsModel(100.99)))
      lazy val result = target.disposalCosts(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "have the amount 100.99 pre-populated into the input field" in {
        doc.getElementById("amount").attr("value") shouldBe "100.99"
      }
    }
  }

  "Calling .disposalCosts from the GainCalculationController with no session" should {

    lazy val target = setupTarget(None)
    lazy val result = target.disposalCosts(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout view" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }

  "calling .submitDisposalCosts from the GainCalculationController" when {

    "given a valid form should" should {

      lazy val target = setupTarget(Some(DisposalCostsModel(100.99)))
      lazy val request = fakeRequestToPOSTWithSession(("amount", "100"))
      lazy val result = target.submitDisposalCosts(request)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      s"redirect to '${controllers.routes.GainController.acquisitionValue().toString}'" in {
        redirectLocation(result).get shouldBe controllers.routes.GainController.ownerBeforeLegislationStart().toString
      }
    }

    "given an invalid form" should {

      lazy val target = setupTarget(Some(DisposalCostsModel(100.99)))
      lazy val request = fakeRequestToPOSTWithSession(("amount", "-100"))
      lazy val result = target.submitDisposalCosts(request)

      "return a status of 400" in {
        status(result) shouldBe 400
      }

    }

  }
}
