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

package controllers.resident.shares.DeductionsControllerSpec

import controllers.helpers.FakeRequestHelper
import controllers.resident.shares.DeductionsController
import org.jsoup.Jsoup
import play.api.test.Helpers._
import assets.MessageLookup.{AllowableLossesValue => messages}
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import connectors.CalculatorConnector
import models.resident.{TaxYearModel, AllowableLossesValueModel, DisposalDateModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AllowableLossesValueActionSpec extends UnitSpec with WithFakeApplication with FakeRequestHelper with MockitoSugar {

  def setupTarget(getData: Option[AllowableLossesValueModel],
                  disposalDate: Option[DisposalDateModel],
                  taxYear: Option[TaxYearModel]): DeductionsController = {

    val mockCalcConnector = mock[CalculatorConnector]

    when(mockCalcConnector.fetchAndGetFormData[AllowableLossesValueModel](Matchers.eq(keystoreKeys.allowableLossesValue))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.saveFormData[AllowableLossesValueModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    when(mockCalcConnector.fetchAndGetFormData[DisposalDateModel](Matchers.eq(keystoreKeys.disposalDate))(Matchers.any(), Matchers.any()))
      .thenReturn(disposalDate)

    when(mockCalcConnector.getTaxYear(Matchers.any())(Matchers.any()))
      .thenReturn(taxYear)

    new DeductionsController {
      override val calcConnector: CalculatorConnector = mockCalcConnector
    }
  }

  "Calling .allowableLossesValue from the DeductionsController" when {

    "there is no keystore data" should {

      lazy val target = setupTarget(None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.allowableLossesValue(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))
      lazy val form = doc.getElementsByTag("form")

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      s"have a title of ${messages.title("2015/16")}" in {
        doc.title() shouldBe messages.title("2015/16")
      }

      "have a back link to the properties allowable losses page" in {
        doc.select("#back-link").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/allowable-losses"
      }

      "have a home link to the properties disposal date" in {
        doc.select("#homeNavHref").attr("href") shouldEqual "/calculate-your-capital-gains/resident/shares/disposal-date"
      }

      s"has the action '${controllers.resident.shares.routes.DeductionsController.submitAllowableLossesValue().toString}'" in {
        form.attr("action") shouldBe controllers.resident.shares.routes.DeductionsController.submitAllowableLossesValue().toString
      }

      "has the method of POST" in {
        form.attr("method") shouldBe "POST"
      }
    }

    "there is some keystore data" should {

      lazy val target = setupTarget(Some(AllowableLossesValueModel(1000)), Some(DisposalDateModel(10, 10, 2015)),
        Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val result = target.allowableLossesValue(fakeRequestWithSession)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Allowable Losses Value view" in {
        doc.title shouldBe messages.title("2015/16")
      }

      "have 1000 pre-populated in the amount input field" in {
        doc.select("input#amount").attr("value") shouldBe "1000"
      }
    }

    "request has an invalid session" should {

      lazy val result = DeductionsController.allowableLossesValue(fakeRequest)

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "return you to the session timeout page" in {
        redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
      }
    }
  }

  "Calling .submitAllowableLossesValue from the DeductionsController" when {

    "a valid form is submitted" should {

      lazy val target = setupTarget(None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000"))
      lazy val result = target.submitAllowableLossesValue(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the brought forward losses page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/losses-brought-forward")
      }
    }

    "an invalid form is submitted" should {

      lazy val target = setupTarget(None, Some(DisposalDateModel(10, 10, 2015)), Some(TaxYearModel("2015/16", true, "2015/16")))
      lazy val request = fakeRequestToPOSTWithSession(("amount", ""))
      lazy val result = target.submitAllowableLossesValue(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the Allowable Losses Value page" in {
        doc.title() shouldEqual messages.title("2015/16")
      }
    }
  }
}
