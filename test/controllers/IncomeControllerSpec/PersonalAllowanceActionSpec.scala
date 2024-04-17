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

package controllers.IncomeControllerSpec

import org.apache.pekko.actor.ActorSystem
import assets.MessageLookup.{PersonalAllowance => messages}
import com.codahale.metrics.SharedMetricRegistries
import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.{CommonPlaySpec, WithCommonFakeApplication}
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.IncomeController
import controllers.helpers.FakeRequestHelper
import forms.{CurrentIncomeForm, PersonalAllowanceForm}
import models.resident.income.PersonalAllowanceModel
import models.resident.{DisposalDateModel, TaxYearModel}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.MessagesControllerComponents
import play.api.test.Helpers._
import services.SessionCacheService
import views.html.calculation.income.{currentIncome, personalAllowance}

import scala.concurrent.Future

class PersonalAllowanceActionSpec extends CommonPlaySpec with WithCommonFakeApplication with FakeRequestHelper with MockitoSugar {

  implicit lazy val actorSystem = ActorSystem()
  val mockCalcConnector: CalculatorConnector = mock[CalculatorConnector]
  val mockSessionCacheService: SessionCacheService = mock[SessionCacheService]
  implicit val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
  implicit val mockApplication: Application = fakeApplication
  val mockMCC: MessagesControllerComponents = fakeApplication.injector.instanceOf[MessagesControllerComponents]
  val personalAllowanceForm: PersonalAllowanceForm = fakeApplication.injector.instanceOf[PersonalAllowanceForm]
  val personalAllowanceView: personalAllowance = fakeApplication.injector.instanceOf[personalAllowance]
  val currentIncomeForm: CurrentIncomeForm = fakeApplication.injector.instanceOf[CurrentIncomeForm]
  val currentIncomeView: currentIncome = fakeApplication.injector.instanceOf[currentIncome]

  def setupTarget(getData: Option[PersonalAllowanceModel],
                  maxPersonalAllowance: Option[BigDecimal] = Some(BigDecimal(11100)),
                  disposalDateModel: DisposalDateModel,
                  taxYearModel: TaxYearModel): IncomeController = {

    SharedMetricRegistries.clear()


    when(mockSessionCacheService.fetchAndGetFormData[PersonalAllowanceModel]
      (ArgumentMatchers.eq(keystoreKeys.personalAllowance))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(getData))

    when(mockCalcConnector.getPA(ArgumentMatchers.any(), ArgumentMatchers.eq(true), ArgumentMatchers.eq(true))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(maxPersonalAllowance))

    when(mockCalcConnector.getPA(ArgumentMatchers.any(), ArgumentMatchers.eq(false), ArgumentMatchers.eq(false))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(maxPersonalAllowance))

    when(mockSessionCacheService.saveFormData[PersonalAllowanceModel]
      (ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful("" -> ""))

    when(mockSessionCacheService.fetchAndGetFormData[DisposalDateModel]
      (ArgumentMatchers.eq(keystoreKeys.disposalDate))(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(disposalDateModel)))

    when(mockCalcConnector.getTaxYear(ArgumentMatchers.any())(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(taxYearModel)))


    new IncomeController(mockCalcConnector, mockSessionCacheService, mockMCC, personalAllowanceForm, personalAllowanceView, currentIncomeForm, currentIncomeView)
  }

  "Calling .personalAllowance from the IncomeController" when {

    "there is no keystore data" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setupTarget(None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val result = target.personalAllowance(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Personal Allowance view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }

    "there is some keystore data" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setupTarget(Some(PersonalAllowanceModel(1000)), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val result = target.personalAllowance(fakeRequestWithSession)

      "return a status of 200" in {
        status(result) shouldBe 200
      }

      "return some html" in {
        contentType(result) shouldBe Some("text/html")
      }

      "display the Personal Allowance view" in {
        Jsoup.parse(bodyOf(result)).title shouldBe messages.title
      }
    }
  }

  "request has an invalid session" should {

    lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
    lazy val taxYearModel = TaxYearModel("2017/18", false, "2016/17")
    lazy val target = setupTarget(None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
    lazy val result = target.personalAllowance(fakeRequest)

    "return a status of 303" in {
      status(result) shouldBe 303
    }

    "return you to the session timeout page" in {
      redirectLocation(result).get should include ("/calculate-your-capital-gains/resident/shares/session-timeout")
    }
  }

  "Calling .submitPersonalAllowance from the IncomeController" when {

    "a valid form is submitted" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val request = fakeRequestToPOSTWithSession(("amount", "1000")).withMethod("POST")
      lazy val target = setupTarget(Some(PersonalAllowanceModel(1000)), disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val result = target.submitPersonalAllowance(request)

      "return a 303" in {
        status(result) shouldBe 303
      }

      "redirect to the summary page" in {
        redirectLocation(result) shouldBe Some("/calculate-your-capital-gains/resident/shares/review-your-answers-final")
      }
    }

    "an invalid form is submitted" should {

      lazy val disposalDateModel = DisposalDateModel(10, 10, 2015)
      lazy val taxYearModel = TaxYearModel("2015/16", true, "2015/16")
      lazy val target = setupTarget(None, disposalDateModel = disposalDateModel, taxYearModel = taxYearModel)
      lazy val request = fakeRequestToPOSTWithSession(("amount", "")).withMethod("POST")
      lazy val result = target.submitPersonalAllowance(request)
      lazy val doc = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "render the personal allowance page" in {
        doc.title() shouldEqual "Error: " + messages.title
      }
    }
  }
}
