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
import connectors.{CalculatorConnector, SessionCacheConnector}
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.LossesBroughtForwardForm._
import forms.LossesBroughtForwardValueForm._
import javax.inject.Inject
import models.resident._
import models.resident.shares.GainAnswersModel
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Call, MessagesControllerComponents, Request, Result}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.deductions.{lossesBroughtForward, lossesBroughtForwardValue}

import scala.concurrent.{ExecutionContext, Future}

class DeductionsController @Inject()(calcConnector: CalculatorConnector,
                                     sessionCacheConnector: SessionCacheConnector,
                                     sessionCacheService: SessionCacheService,
                                     mcc: MessagesControllerComponents,
                                     lossesBroughtForwardView: lossesBroughtForward,
                                     lossesBroughtForwardValueView: lossesBroughtForwardValue)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  override val homeLink = routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink
  def navTitle(implicit request : Request[_]): String = Messages("calc.base.resident.shares.home")(mcc.messagesApi.preferred(request))


  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  def totalGain(answerSummary: GainAnswersModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttShareGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[GainAnswersModel] = sessionCacheService.getShareGainAnswers(hc)

  def taxYearStringToInteger(taxYear: String): Future[Int] = {
    Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
  }

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- sessionCacheService.getShareGainAnswers
      chargeableGainAnswers <- sessionCacheService.getShareDeductionAnswers
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- calcConnector.getFullAEA(year)
      chargeableGain <- calcConnector.calculateRttShareChargeableGain(gainAnswers, chargeableGainAnswers, maxAEA.get).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  //################# Brought Forward Losses Actions ##############################

  private val lossesBroughtForwardPostAction = controllers.routes.DeductionsController.submitLossesBroughtForward()

  val lossesBroughtForwardBackUrl: String = controllers.routes.GainController.acquisitionCosts().url

  val lossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel): Future[Result] = {
      sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(lossesBroughtForwardView(lossesBroughtForwardForm.fill(data), lossesBroughtForwardPostAction,
          backLinkUrl, taxYear, homeLink, navTitle))
        case _ => Ok(lossesBroughtForwardView(lossesBroughtForwardForm, lossesBroughtForwardPostAction, backLinkUrl, taxYear,
          homeLink, navTitle))
      }
    }

    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(lossesBroughtForwardBackUrl, taxYear.get)
    } yield finalResult).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitLossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel): Future[Result] = {
      lossesBroughtForwardForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(lossesBroughtForwardView(errors, lossesBroughtForwardPostAction, backUrl, taxYearModel,
          homeLink, navTitle))),
        success => {
          sessionCacheConnector.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success).flatMap(
            _ =>if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue()))
            else {
              positiveChargeableGainCheck.map { positiveChargeableGain =>
                if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
                else Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers())
              }
            }
          )
        }
      )
    }

    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(lossesBroughtForwardBackUrl, taxYear.get)
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)

  }

  //################# Brought Forward Losses Value Actions ##############################

  private val lossesBroughtForwardValueBackLink: String = routes.DeductionsController.lossesBroughtForward().url
  private val lossesBroughtForwardValuePostAction: Call = routes.DeductionsController.submitLossesBroughtForwardValue()

  val lossesBroughtForwardValue = ValidateSession.async { implicit request =>

    def retrieveKeystoreData(): Future[Form[LossesBroughtForwardValueModel]] = {
      sessionCacheConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue).map {
        case Some(data) => lossesBroughtForwardValueForm.fill(data)
        case _ => lossesBroughtForwardValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[LossesBroughtForwardValueModel]): Future[Result] = {
      Future.successful(Ok(lossesBroughtForwardValueView(
        formData,
        taxYear,
        navBackLink = lossesBroughtForwardValueBackLink,
        navHomeLink = homeLink,
        postAction = lossesBroughtForwardValuePostAction,
        navTitle
      )))
    }
    (for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- retrieveKeystoreData()
      route <- routeRequest(taxYear.get, formData)
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  val submitLossesBroughtForwardValue = ValidateSession.async { implicit request =>
    lossesBroughtForwardValueForm.bindFromRequest.fold(
      errors => {
        for {
          disposalDate <- getDisposalDate
          disposalDateString <- formatDisposalDate(disposalDate.get)
          taxYear <- calcConnector.getTaxYear(disposalDateString)
        } yield {
          BadRequest(lossesBroughtForwardValueView(
            errors,
            taxYear.get,
            navBackLink = lossesBroughtForwardValueBackLink,
            navHomeLink = homeLink,
            postAction = lossesBroughtForwardValuePostAction,
            navTitle = navTitle))
        }
      },
      success => {
        sessionCacheConnector.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success).flatMap(
          _ => positiveChargeableGainCheck.map { positiveChargeableGain =>
            if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
            else Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers())
          }
        )
      }
    )
  }
}
