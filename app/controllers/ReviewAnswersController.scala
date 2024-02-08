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

import common.Dates
import common.Dates.requestFormatter
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import models.resident.{LossesBroughtForwardModel, TaxYearModel}
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.checkYourAnswers.checkYourAnswers

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReviewAnswersController @Inject()(calculatorConnector: CalculatorConnector,
                                        sessionCacheService: SessionCacheService,
                                        mcc: MessagesControllerComponents,
                                        checkYourAnswersView: checkYourAnswers)(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def getTaxYear(disposalDate: LocalDate)(implicit request: Request[_]): Future[TaxYearModel] =
    calculatorConnector.getTaxYear(disposalDate.format(requestFormatter)).map {
      _.get
    }

  def getGainAnswers(implicit request: Request[_]): Future[GainAnswersModel] = sessionCacheService.getShareGainAnswers

  def getDeductionsAnswers(implicit request: Request[_]): Future[DeductionGainAnswersModel] = sessionCacheService.getShareDeductionAnswers

  private def languageRequest(body : Lang => Future[Result])(implicit request: Request[_]): Future[Result] =
    body(mcc.messagesApi.preferred(request).lang)

  val reviewGainAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      languageRequest { implicit lang =>
        getGainAnswers.map { answers =>
          Ok(checkYourAnswersView(routes.SummaryController.summary, controllers.routes.GainController.acquisitionCosts.url, answers, None, None))
        }
      }
  }

  val reviewDeductionsAnswers: Action[AnyContent] = ValidateSession.async {
    def generateBackUrl(deductionGainAnswers: DeductionGainAnswersModel): Future[String] = {
      if (deductionGainAnswers.broughtForwardModel.getOrElse(LossesBroughtForwardModel(false)).option) {
        Future.successful(routes.DeductionsController.lossesBroughtForwardValue.url)
      } else {
        Future.successful(routes.DeductionsController.lossesBroughtForward.url)
      }
    }

    implicit request =>
      languageRequest { implicit lang =>
        for {
          gainAnswers <- getGainAnswers
          deductionsAnswers <- getDeductionsAnswers
          taxYear <- getTaxYear(gainAnswers.disposalDate)
          url <- generateBackUrl(deductionsAnswers)
        } yield Ok(checkYourAnswersView(routes.SummaryController.summary, url, gainAnswers, Some(deductionsAnswers), Some(taxYear)))
      }
  }

  val reviewFinalAnswers: Action[AnyContent] = ValidateSession.async {
    implicit request =>
      languageRequest { implicit lang =>
        val getCurrentTaxYear = Dates.getCurrentTaxYear
        val getIncomeAnswers = sessionCacheService.getShareIncomeAnswers

        for {
          gainAnswers <- getGainAnswers
          deductionsAnswers <- getDeductionsAnswers
          incomeAnswers <- getIncomeAnswers
          taxYear <- getTaxYear(gainAnswers.disposalDate)
          currentTaxYear = getCurrentTaxYear
        } yield Ok(checkYourAnswersView(routes.SummaryController.summary, routes.IncomeController.personalAllowance.url, gainAnswers,
          Some(deductionsAnswers), Some(taxYear), Some(incomeAnswers), taxYear.taxYearSupplied == currentTaxYear))
      }
  }
}
