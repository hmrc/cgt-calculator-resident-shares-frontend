/*
 * Copyright 2017 HM Revenue & Customs
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

import java.time.LocalDate

import common.Dates
import common.Dates.requestFormatter
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import models.resident.{LossesBroughtForwardModel, TaxYearModel}
import play.api.mvc.{Action, AnyContent}
import views.html.calculation.checkYourAnswers.checkYourAnswers
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object ReviewAnswersController extends ReviewAnswersController {
  val calculatorConnector = CalculatorConnector
}

trait ReviewAnswersController extends ValidActiveSession {

  val calculatorConnector: CalculatorConnector

  def getTaxYear(disposalDate: LocalDate)(implicit hc: HeaderCarrier): Future[TaxYearModel] =
    calculatorConnector.getTaxYear(disposalDate.format(requestFormatter)).map {
      _.get
    }

  def getGainAnswers(implicit hc: HeaderCarrier): Future[GainAnswersModel] = calculatorConnector.getShareGainAnswers

  def getDeductionsAnswers(implicit hc: HeaderCarrier): Future[DeductionGainAnswersModel] = calculatorConnector.getShareDeductionAnswers

  val reviewGainAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      getGainAnswers.map { answers =>
        Ok(checkYourAnswers(routes.SummaryController.summary(), controllers.routes.GainController.acquisitionCosts().url, answers, None, None))
      }
  }

  val reviewDeductionsAnswers: Action[AnyContent] = ValidateSession.async {
    def generateBackUrl(deductionGainAnswers: DeductionGainAnswersModel): Future[String] = {
      if (deductionGainAnswers.broughtForwardModel.getOrElse(LossesBroughtForwardModel(false)).option) {
        Future.successful(routes.DeductionsController.lossesBroughtForwardValue().url)
      } else {
        Future.successful(routes.DeductionsController.lossesBroughtForward().url)
      }
    }

    implicit request =>
      for {
        gainAnswers <- getGainAnswers
        deductionsAnswers <- getDeductionsAnswers
        taxYear <- getTaxYear(gainAnswers.disposalDate)
        url <- generateBackUrl(deductionsAnswers)
      } yield Ok(checkYourAnswers(routes.SummaryController.summary(), url, gainAnswers, Some(deductionsAnswers), Some(taxYear)))
  }

  val reviewFinalAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      val getCurrentTaxYear = Dates.getCurrentTaxYear
      val getIncomeAnswers = calculatorConnector.getShareIncomeAnswers

      for {
        gainAnswers <- getGainAnswers
        deductionsAnswers <- getDeductionsAnswers
        incomeAnswers <- getIncomeAnswers
        taxYear <- getTaxYear(gainAnswers.disposalDate)
        currentTaxYear <- getCurrentTaxYear
      } yield Ok(checkYourAnswers(routes.SummaryController.summary(), routes.IncomeController.personalAllowance().url, gainAnswers,
        Some(deductionsAnswers), Some(taxYear), Some(incomeAnswers), taxYear.taxYearSupplied == currentTaxYear))
  }
}
