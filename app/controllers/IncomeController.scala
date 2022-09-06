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

import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.resident.JourneyKeys
import common.{Dates, TaxDates}
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.CurrentIncomeForm._
import forms.PersonalAllowanceForm._
import javax.inject.Inject
import models.resident._
import models.resident.income._
import play.api.data.Form
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.income.{currentIncome, personalAllowance}

import scala.concurrent.{ExecutionContext, Future}

class IncomeController @Inject()(calcConnector: CalculatorConnector,
                                 sessionCacheConnector: SessionCacheConnector,
                                 mcc: MessagesControllerComponents,
                                 personalAllowanceView: personalAllowance,
                                 currentIncomeView: currentIncome)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def navTitle(implicit request : Request[_]): String = Messages("calc.base.resident.shares.home")(mcc.messagesApi.preferred(request))
  override val homeLink = controllers.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  def lossesBroughtForwardResponse(implicit hc: HeaderCarrier): Future[Boolean] = {
    sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
      case Some(LossesBroughtForwardModel(response)) => response
      case None => false
    }
  }

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  //################################# Current Income Actions ##########################################
  def buildCurrentIncomeBackUrl(implicit hc: HeaderCarrier): Future[String] = {
    lossesBroughtForwardResponse.map { response =>
      if (response) routes.DeductionsController.lossesBroughtForwardValue().url
      else routes.DeductionsController.lossesBroughtForward().url
    }
  }

  def currentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: String, taxYear: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYear.taxYearSupplied == currentTaxYear
      implicit val lang: Lang = messagesApi.preferred(request).lang
      sessionCacheConnector.fetchAndGetFormData[CurrentIncomeModel](keystoreKeys.currentIncome).map {
        case Some(data) => Ok(currentIncomeView(currentIncomeForm.fill(data), backUrl, taxYear, inCurrentTaxYear))
        case None => Ok(currentIncomeView(currentIncomeForm, backUrl, taxYear, inCurrentTaxYear))
      }
    }

    (for {
      backUrl <- buildCurrentIncomeBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      currentTaxYear = Dates.getCurrentTaxYear
      finalResult <- routeRequest(backUrl, taxYear.get, currentTaxYear)
    } yield finalResult).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  def submitCurrentIncome: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {

      val inCurrentTaxYear = taxYearModel.taxYearSupplied == currentTaxYear
      implicit val lang: Lang = messagesApi.preferred(request).lang
      currentIncomeForm.bindFromRequest.fold(
        errors => buildCurrentIncomeBackUrl.flatMap(url => Future.successful(BadRequest(currentIncomeView(errors, url,
          taxYearModel, inCurrentTaxYear)))),
        success => {
          sessionCacheConnector.saveFormData[CurrentIncomeModel](keystoreKeys.currentIncome, success).flatMap(
            _ => Future.successful(Redirect(routes.IncomeController.personalAllowance()))
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
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }


  //################################# Personal Allowance Actions ##########################################
  def getStandardPA(year: Int, hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getPA(year)(hc)
  }

  def taxYearValue(taxYear: String): Future[Int] = {
    Future.successful(TaxDates.taxYearStringToInteger(taxYear))
  }

  private val backLinkPersonalAllowance = Some(controllers.routes.IncomeController.currentIncome().toString)
  private val postActionPersonalAllowance = controllers.routes.IncomeController.submitPersonalAllowance()

  def personalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def fetchStoredPersonalAllowance(): Future[Form[PersonalAllowanceModel]] = {
      sessionCacheConnector.fetchAndGetFormData[PersonalAllowanceModel](keystoreKeys.personalAllowance).map {
        case Some(data) => personalAllowanceForm().fill(data)
        case _ => personalAllowanceForm()
      }
    }

    def routeRequest(taxYearModel: TaxYearModel, standardPA: BigDecimal, formData: Form[PersonalAllowanceModel], currentTaxYear: String):
    Future[Result] = {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      Future.successful(Ok(personalAllowanceView(formData, taxYearModel, standardPA, homeLink,
        postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.shares, navTitle, currentTaxYear)))
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearValue(taxYear.get.calculationTaxYear)
      standardPA <- getStandardPA(year, hc)
      formData <- fetchStoredPersonalAllowance()
      currentTaxYear = Dates.getCurrentTaxYear
      route <- routeRequest(taxYear.get, standardPA.get, formData, currentTaxYear)
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  def submitPersonalAllowance: Action[AnyContent] = ValidateSession.async { implicit request =>

    def getMaxPA(year: Int): Future[Option[BigDecimal]] = {
      calcConnector.getPA(year, isEligibleBlindPersonsAllowance = true, isEligibleMarriageAllowance = true )
    }


    def routeRequest(maxPA: BigDecimal, standardPA: BigDecimal, taxYearModel: TaxYearModel, currentTaxYear: String): Future[Result] = {
      implicit val lang: Lang = messagesApi.preferred(request).lang
      personalAllowanceForm(maxPA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(personalAllowanceView(errors, taxYearModel, standardPA, homeLink,
          postActionPersonalAllowance, backLinkPersonalAllowance, JourneyKeys.shares, navTitle, currentTaxYear))),
        success => {
          sessionCacheConnector.saveFormData(keystoreKeys.personalAllowance, success).flatMap(
            _ => Future.successful(Redirect(routes.ReviewAnswersController.reviewFinalAnswers()))
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
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }
}
