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

import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.resident.JourneyKeys
import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.{CurrentIncomeForm, PersonalAllowanceForm}
import models.resident._
import models.resident.income._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.income.{currentIncome, personalAllowance}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IncomeController @Inject()(calcConnector: CalculatorConnector,
                                 sessionCacheService: SessionCacheService,
                                 mcc: MessagesControllerComponents,
                                 personalAllowanceForm: PersonalAllowanceForm,
                                 personalAllowanceView: personalAllowance,
                                 currentIncomeForm: CurrentIncomeForm,
                                 currentIncomeView: currentIncome)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {
  private def lossesBroughtForwardResponse(implicit request: Request[?]) = {
    sessionCacheService.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
      case None => false
    }
  }

  private def getDisposalDate(implicit request: Request[?]) = {
    sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  private def formatDisposalDate(disposalDateModel: DisposalDateModel) = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  //################################# Current Income Actions ##########################################
  private def buildCurrentIncomeBackUrl(implicit request: Request[?]) = {
    lossesBroughtForwardResponse.map { response =>
      if (response) routes.DeductionsController.lossesBroughtForwardValue.url
      else routes.DeductionsController.lossesBroughtForward.url
    }
  }

  def currentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String, taxYear: TaxYearModel, currentTaxYear: String): Future[Result] = {
      val inCurrentTaxYear = taxYear.taxYearSupplied == currentTaxYear
      implicit val lang: Lang = messagesApi.preferred(request).lang

      val form: Form[CurrentIncomeModel] = currentIncomeForm(TaxYearModel.convertWithWelsh(taxYear.taxYearSupplied))

      sessionCacheService.fetchAndGetFormData[CurrentIncomeModel](keystoreKeys.currentIncome).map {
        case Some(data) => Ok(currentIncomeView(form.fill(data), backUrl, taxYear, inCurrentTaxYear))
        case None => Ok(currentIncomeView(form, backUrl, taxYear, inCurrentTaxYear))
      }
    }

    (for {
      backUrl <- buildCurrentIncomeBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear = Dates.getCurrentTaxYear
      finalResult <- routeRequest(backUrl, taxYear.get, currentTaxYear)
    } yield finalResult).recoverToStart()
  }

  def submitCurrentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {
      val inCurrentTaxYear = taxYearModel.taxYearSupplied == currentTaxYear
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val form: Form[CurrentIncomeModel] = currentIncomeForm(TaxYearModel.convertWithWelsh(taxYearModel.taxYearSupplied))
      form.bindFromRequest().fold(
        errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(currentIncomeView(errors, url,
          taxYearModel, inCurrentTaxYear)))),
        success => {
          sessionCacheService.saveFormData[CurrentIncomeModel](keystoreKeys.currentIncome, success).flatMap(
            _ => Future.successful(Redirect(routes.IncomeController.personalAllowance))
          )
        }
      )
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear = Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, currentTaxYear)
    } yield route).recoverToStart()
  }

  //################################# Personal Allowance Actions ##########################################
  private def getStandardPA(year: Int, hc: HeaderCarrier) = {
    calcConnector.getPA(year)(using hc)
  }

  private def taxYearValue(taxYear: String) = {
    Future.successful(TaxDates.taxYearStringToInteger(taxYear))
  }

  private val backLinkPersonalAllowance = Some(controllers.routes.IncomeController.currentIncome.toString)
  private val postActionPersonalAllowance = controllers.routes.IncomeController.submitPersonalAllowance

  def personalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>
    def fetchStoredPersonalAllowance(maxPA: BigDecimal, taxYear: TaxYearModel): Future[Form[PersonalAllowanceModel]] = {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val form: Form[PersonalAllowanceModel] = personalAllowanceForm(maxPA, TaxYearModel.convertWithWelsh(taxYear.taxYearSupplied), lang)

      sessionCacheService.fetchAndGetFormData[PersonalAllowanceModel](keystoreKeys.personalAllowance).map {
        case Some(data) => form.fill(data)
        case _ => form
      }
    }

    def routeRequest(taxYearModel: TaxYearModel, standardPA: BigDecimal, formData: Form[PersonalAllowanceModel], currentTaxYear: String):
    Future[Result] = {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      Future.successful(Ok(personalAllowanceView(formData, taxYearModel, standardPA,
        postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.shares, currentTaxYear)))
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      formData <- fetchStoredPersonalAllowance(standardPA.get, taxYear.get)
      currentTaxYear = Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, standardPA.get, formData, currentTaxYear)
    } yield route).recoverToStart()
  }

  def submitPersonalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>
    def getMaxPA(year: Int): Future[Option[BigDecimal]] = {
      calcConnector.getPA(year, isEligibleBlindPersonsAllowance = true, isEligibleMarriageAllowance = true )
    }

    def routeRequest(maxPA: BigDecimal, standardPA: BigDecimal, taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      val form: Form[PersonalAllowanceModel] = personalAllowanceForm(maxPA, TaxYearModel.convertWithWelsh(taxYearModel.taxYearSupplied), lang)

      form.bindFromRequest().fold(
        errors => Future.successful(BadRequest(personalAllowanceView(errors, taxYearModel, standardPA,
          postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.shares, currentTaxYear))),
        success => {
          sessionCacheService.saveFormData(keystoreKeys.personalAllowance, success).flatMap(
            _ => Future.successful(Redirect(routes.ReviewAnswersController.reviewFinalAnswers))
          )
        }
      )
    }

    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      maxPA <- getMaxPA(year)
      currentTaxYear = Dates.getCurrentTaxYear
      route <- routeRequest(maxPA.get, standardPA.get, taxYear.get, currentTaxYear)
    } yield route).recoverToStart()
  }
}
