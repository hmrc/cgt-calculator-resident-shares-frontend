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
import common.TaxDates.taxYearStringToInteger
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.LossesBroughtForwardForm._
import forms.LossesBroughtForwardValueForm
import models.resident._
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.deductions.{lossesBroughtForward, lossesBroughtForwardValue}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeductionsController @Inject()(calcConnector: CalculatorConnector,
                                     sessionCacheService: SessionCacheService,
                                     mcc: MessagesControllerComponents,
                                     lossesBroughtForwardValueForm: LossesBroughtForwardValueForm,
                                     lossesBroughtForwardView: lossesBroughtForward,
                                     lossesBroughtForwardValueView: lossesBroughtForwardValue)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  private def getDisposalDate(implicit request: Request[_]): Future[Option[DisposalDateModel]] =
    sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)

  private def formatDisposalDate(disposalDateModel: DisposalDateModel): String =
    s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}"

  private def getTaxYear(implicit request: Request[_]) =
    for {
      disposalDate <- getDisposalDate
      disposalDateString = formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
    } yield taxYear.get

  private def positiveChargeableGainCheck(implicit request: Request[_]): Future[Boolean] =
    for {
      gainAnswers <- sessionCacheService.getShareGainAnswers
      chargeableGainAnswers <- sessionCacheService.getShareDeductionAnswers
      taxYear <- getTaxYear
      year = taxYearStringToInteger(taxYear.calculationTaxYear)
      maxAEA <- calcConnector.getFullAEA(year)
      result <- calcConnector.calculateRttShareChargeableGain(gainAnswers, chargeableGainAnswers, maxAEA.get).map(_.get.chargeableGain)
    } yield result > 0

  //################# Brought Forward Losses Actions ##############################

  private val lossesBroughtForwardPostAction = controllers.routes.DeductionsController.submitLossesBroughtForward

  private val lossesBroughtForwardBackUrl: String = controllers.routes.GainController.acquisitionCosts.url

  def lossesBroughtForward: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    (for {
      taxYear <- getTaxYear
      formData <- sessionCacheService.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward)
    } yield {
      val form = formData.map(lossesBroughtForwardForm.fill).getOrElse(lossesBroughtForwardForm)
      Ok(lossesBroughtForwardView(form, lossesBroughtForwardPostAction, lossesBroughtForwardBackUrl, taxYear))
    }).recoverToStart()
  }

  private def redirect(implicit request: Request[_]) =
    positiveChargeableGainCheck map {
      case true => Redirect(routes.IncomeController.currentIncome)
      case false => Redirect(routes.ReviewAnswersController.reviewDeductionsAnswers)
    }

  def submitLossesBroughtForward: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    (for {
      taxYear <- getTaxYear
      route <- lossesBroughtForwardForm.bindFromRequest().fold(
        errors =>
          Future.successful(BadRequest(lossesBroughtForwardView(errors, lossesBroughtForwardPostAction, lossesBroughtForwardBackUrl, taxYear))),
        success =>
          for {
            _ <- sessionCacheService.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success)
            result <-
              if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue))
              else redirect
          } yield result
      )
    } yield route).recoverToStart()

  }

  //################# Brought Forward Losses Value Actions ##############################

  private val lossesBroughtForwardValueBackLink: String = routes.DeductionsController.lossesBroughtForward.url
  private val lossesBroughtForwardValuePostAction: Call = routes.DeductionsController.submitLossesBroughtForwardValue

  def lossesBroughtForwardValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    (for {
      taxYear <- getTaxYear
      form = lossesBroughtForwardValueForm(TaxYearModel.convertWithWelsh(taxYear.taxYearSupplied), lang)
      formData <- sessionCacheService.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue)
        .map(_.map(form.fill).getOrElse(form))
    } yield Ok(lossesBroughtForwardValueView(
        formData,
        taxYear,
        navBackLink = lossesBroughtForwardValueBackLink,
        postAction = lossesBroughtForwardValuePostAction
      ))
    ).recoverToStart()
  }

  def submitLossesBroughtForwardValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    for {
      taxYear <- getTaxYear
      form = lossesBroughtForwardValueForm(TaxYearModel.convertWithWelsh(taxYear.taxYearSupplied), lang)
      result <- form.bindFromRequest().fold(
        errors =>
          Future.successful(BadRequest(lossesBroughtForwardValueView(errors, taxYear, navBackLink = lossesBroughtForwardValueBackLink, postAction = lossesBroughtForwardValuePostAction))),
        success =>
          for {
            _ <- sessionCacheService.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success)
            result <- redirect
          } yield result
      )
    } yield result
  }
}
