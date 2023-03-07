/*
 * Copyright 2023 HM Revenue & Customs
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

import common.Dates.requestFormatter
import connectors.CalculatorConnector
import constructors.CalculateRequestConstructor
import controllers.predicates.ValidActiveSession
import forms.SaUserForm
import javax.inject.Inject
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SessionCacheService

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.whatNext.saUser

class SaUserController @Inject()(calculatorConnector: CalculatorConnector,
                                 sessionCacheService: SessionCacheService,
                                 mcc: MessagesControllerComponents,
                                 saUserView: saUser)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val saUser: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      Future.successful(Ok(saUserView(SaUserForm.saUserForm)))
  }

  val submitSaUser: Action[AnyContent] = ValidateSession.async { implicit request =>

    def chargeableGain(grossGain: BigDecimal,
                       deductionGainAnswers: DeductionGainAnswersModel,
                       chargeableGainAnswers: GainAnswersModel,
                       maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttShareChargeableGain(chargeableGainAnswers, deductionGainAnswers, maxAEA)
      else Future.successful(None)
    }

    def totalTaxableGain(chargeableGain: Option[ChargeableGainResultModel],
                         deductionGainAnswers: DeductionGainAnswersModel,
                         chargeableGainAnswers: GainAnswersModel,
                         incomeAnswersModel: IncomeAnswersModel,
                         maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswersModel.personalAllowanceModel.isDefined && incomeAnswersModel.currentIncomeModel.isDefined) {
        calculatorConnector.calculateRttShareTotalGainAndTax(chargeableGainAnswers, deductionGainAnswers, maxAEA, incomeAnswersModel)
      }
      else Future.successful(None)
    }

    def taxYearStringToInteger(taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def saAction(disposalValue: BigDecimal, taxOwed: Option[BigDecimal], maxAEA: BigDecimal): Future[Result] = {
      taxOwed match {
        case Some(tax) if tax > 0 => Future{Redirect(controllers.routes.WhatNextSAController.whatNextSAGain())}
        case _ if disposalValue >= 4 * maxAEA => Future{Redirect(controllers.routes.WhatNextSAController.whatNextSAOverFourTimesAEA())}
        case _ => Future{Redirect(controllers.routes.WhatNextSAController.whatNextSANoGain())}
      }
    }

    def nonSaAction(taxOwed: Option[BigDecimal]) = {
      taxOwed match {
        case Some(gain) if gain > 0 => Future{Redirect(controllers.routes.WhatNextNonSaController.whatNextNonSaGain())}
        case _ => Future{Redirect(controllers.routes.WhatNextNonSaController.whatNextNonSaLoss())}
      }
    }

    def routeAction(saUserModel: SaUserModel, totalGainAndTaxOwedModel: Option[TotalGainAndTaxOwedModel],
                    maxAEA: BigDecimal, disposalValue: BigDecimal): Future[Result] = {
      val taxOwed = totalGainAndTaxOwedModel.map {_.taxOwed}
      if (saUserModel.isInSa) saAction(disposalValue, taxOwed, maxAEA)
      else nonSaAction(taxOwed)
    }

    def errorAction(form: Form[SaUserModel]) = {
      Future.successful(BadRequest(saUserView(form)))
    }

    def successAction(model: SaUserModel) = {
      for {
        answers <- sessionCacheService.getShareGainAnswers
        grossGain <- calculatorConnector.calculateRttShareGrossGain(answers)
        deductionAnswers <- sessionCacheService.getShareDeductionAnswers
        taxYear <- calculatorConnector.getTaxYear(answers.disposalDate.format(requestFormatter))
        taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
        maxAEA <- calculatorConnector.getFullAEA(taxYearInt)
        chargeableGain <- chargeableGain(grossGain, deductionAnswers, answers, maxAEA.get)
        incomeAnswers <- sessionCacheService.getShareIncomeAnswers
        finalResult <- totalTaxableGain(chargeableGain, deductionAnswers, answers, incomeAnswers, maxAEA.get)
        route <- routeAction(model, finalResult, maxAEA.get, CalculateRequestConstructor.determineDisposalValueToUse(answers))
      } yield route
    }

    SaUserForm.saUserForm.bindFromRequest().fold(errorAction, successAction)
  }
}
