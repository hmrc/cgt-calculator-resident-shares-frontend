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

package controllers

import java.time.LocalDate

import common.Dates
import common.Dates._
import config.ApplicationConfig
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import javax.inject.Inject
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import play.api.Play.current
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, Result}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.calculation.{summary => views}

import scala.concurrent.Future
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

class SummaryController @Inject()(calculatorConnector: CalculatorConnector,
                                  sessionCacheService: SessionCacheService,
                                  mcc: MessagesControllerComponents)(implicit val appConfig: ApplicationConfig)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  override val homeLink = controllers.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  val summary = ValidateSession.async { implicit request =>


    def getMaxAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
      calculatorConnector.getFullAEA(taxYear)
    }

    def taxYearStringToInteger(taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def getChargeableGain(grossGain: BigDecimal,
                          totalGainAnswers: GainAnswersModel,
                          deductionGainAnswers: DeductionGainAnswersModel,
                          maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
      if (grossGain > 0) calculatorConnector.calculateRttShareChargeableGain(totalGainAnswers, deductionGainAnswers, maxAEA)
      else Future.successful(None)
    }

    def buildDeductionsSummaryBackUrl(deductionGainAnswers: DeductionGainAnswersModel)(implicit hc: HeaderCarrier): Future[String] = {
      Future.successful(routes.ReviewAnswersController.reviewDeductionsAnswers().url)
    }

    def getTotalTaxableGain(chargeableGain: Option[ChargeableGainResultModel] = None,
                            totalGainAnswers: GainAnswersModel, deductionGainAnswers: DeductionGainAnswersModel,
                            incomeAnswersModel: IncomeAnswersModel,
                            maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[TotalGainAndTaxOwedModel]] = {
      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswersModel.personalAllowanceModel.isDefined && incomeAnswersModel.currentIncomeModel.isDefined) {
        calculatorConnector.calculateRttShareTotalGainAndTax(totalGainAnswers, deductionGainAnswers, maxAEA, incomeAnswersModel)
      }
      else Future.successful(None)
    }

    def getTaxYear(disposalDate: LocalDate): Future[Option[TaxYearModel]] = calculatorConnector.getTaxYear(disposalDate.format(requestFormatter))

    def routeRequest(totalGainAnswers: GainAnswersModel,
                     grossGain: BigDecimal,
                     deductionGainAnswers: DeductionGainAnswersModel,
                     chargeableGain: Option[ChargeableGainResultModel],
                     incomeAnswers: IncomeAnswersModel,
                     totalGainAndTax: Option[TotalGainAndTaxOwedModel],
                     backUrl: String,
                     taxYear: Option[TaxYearModel],
                     currentTaxYear: String,
                     totalCosts: BigDecimal,
                     maxAea: BigDecimal,
                     showUserResearchPanel: Boolean)(implicit hc: HeaderCarrier): Future[Result] = {

      if (chargeableGain.isDefined && chargeableGain.get.chargeableGain > 0 &&
        incomeAnswers.personalAllowanceModel.isDefined && incomeAnswers.currentIncomeModel.isDefined) Future.successful(
        Ok(views.finalSummary(totalGainAnswers, deductionGainAnswers,
          totalGainAndTax.get, routes.ReviewAnswersController.reviewFinalAnswers().url, taxYear.get, homeLink, totalCosts, chargeableGain.get.deductions, showUserResearchPanel = false)))

      else if (grossGain > 0) Future.successful(Ok(views.deductionsSummary(totalGainAnswers, deductionGainAnswers,
        chargeableGain.get, backUrl, taxYear.get, homeLink, totalCosts, showUserResearchPanel)))
      else Future.successful(Ok(views.gainSummary(totalGainAnswers, grossGain, taxYear.get, homeLink, totalCosts, maxAea, showUserResearchPanel)))
    }

    val showUserResearchPanel = setURPanelFlag

    (for {
      answers <- sessionCacheService.getShareGainAnswers
      totalCosts <- calculatorConnector.getSharesTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)
      grossGain <- calculatorConnector.calculateRttShareGrossGain(answers)
      deductionAnswers <- sessionCacheService.getShareDeductionAnswers
      backLink <- buildDeductionsSummaryBackUrl(deductionAnswers)
      chargeableGain <- getChargeableGain(grossGain, answers, deductionAnswers, maxAEA.get)
      incomeAnswers <- sessionCacheService.getShareIncomeAnswers
      totalGain <- getTotalTaxableGain(chargeableGain, answers, deductionAnswers, incomeAnswers, maxAEA.get)
      currentTaxYear <- Dates.getCurrentTaxYear
      routeRequest <- routeRequest(answers, grossGain, deductionAnswers, chargeableGain, incomeAnswers, totalGain,
                                   backLink, taxYear, currentTaxYear, totalCosts, maxAEA.get, showUserResearchPanel = showUserResearchPanel)
    } yield routeRequest).recoverToStart(homeLink, homeLink)

  }

  private[controllers] def setURPanelFlag(implicit hc: HeaderCarrier): Boolean = {
    val random = new Random()
    val seed = getLongFromSessionID(hc)
    random.setSeed(seed)
    random.nextInt(3) == 0
  }

  private[controllers] def getLongFromSessionID(hc: HeaderCarrier): Long = {
    val session = hc.sessionId.map(_.value).getOrElse("0")
    val numericSessionValues = session.replaceAll("[^0-9]", "") match {
      case "" => "0"
      case num => num
    }
    numericSessionValues.takeRight(10).toLong

  }
}
