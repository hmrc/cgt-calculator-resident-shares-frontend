/*
 * Copyright 2020 HM Revenue & Customs
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
import it.innove.play.pdf.PdfGenerator
import javax.inject.Inject
import models.resident.TaxYearModel
import play.api.i18n.{I18nSupport, Lang, Messages}
import play.api.mvc.{MessagesControllerComponents, RequestHeader}
import play.api.{Application, Configuration}
import services.SessionCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.calculation.{report => views}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReportController @Inject()(config: Configuration,
                                 calcConnector: CalculatorConnector,
                                 sessionCacheService: SessionCacheService,
                                 mcc: MessagesControllerComponents,
                                 pdfGenerator: PdfGenerator)
                                (implicit val appConfig: ApplicationConfig, implicit val application: Application)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  lazy val platformHost: Option[String] = config.getOptional[String]("platform.frontend.host")

  def host(implicit request: RequestHeader): String =
    if (platformHost.isDefined) s"https://${request.host}" else s"http://${request.host}"

  def getTaxYear(disposalDate: LocalDate)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] =
    calcConnector.getTaxYear(disposalDate.format(requestFormatter))

  def getMaxAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    calcConnector.getFullAEA(taxYear)
  }

  def taxYearStringToInteger (taxYear: String): Future[Int] = {
    Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
  }

  //###### Gain Summary Report ########\\
  val gainSummaryReport = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    (for {
      answers <- sessionCacheService.getShareGainAnswers
      costs <- calcConnector.getSharesTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      grossGain <- calcConnector.calculateRttShareGrossGain(answers)
    } yield {pdfGenerator.ok(views.gainSummaryReport(answers, grossGain, taxYear.get, costs), host).asScala()
      .withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")}).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //#####Deductions summary actions#####\\
  val deductionsReport = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    (for {
      answers <- sessionCacheService.getShareGainAnswers
      costs <- calcConnector.getSharesTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)
      deductionAnswers <- sessionCacheService.getShareDeductionAnswers
      grossGain <- calcConnector.calculateRttShareGrossGain(answers)
      chargeableGain <- calcConnector.calculateRttShareChargeableGain(answers, deductionAnswers, maxAEA.get)
    } yield
      {pdfGenerator.ok(views.deductionsSummaryReport(answers, deductionAnswers, chargeableGain.get, taxYear.get, costs), host).asScala()
      .withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")}).recoverToStart(homeLink, sessionTimeoutUrl)

  }

  //#####Final summary actions#####\\

  val finalSummaryReport = ValidateSession.async { implicit request =>
    implicit val lang: Lang = messagesApi.preferred(request).lang
    (for {
      answers <- sessionCacheService.getShareGainAnswers
      totalCosts <- calcConnector.getSharesTotalCosts(answers)
      taxYear <- getTaxYear(answers.disposalDate)
      taxYearInt <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(taxYearInt)
      grossGain <- calcConnector.calculateRttShareGrossGain(answers)
      deductionAnswers <- sessionCacheService.getShareDeductionAnswers
      chargeableGain <- calcConnector.calculateRttShareChargeableGain(answers, deductionAnswers, maxAEA.get)
      incomeAnswers <- sessionCacheService.getShareIncomeAnswers
      currentTaxYear = Dates.getCurrentTaxYear
      totalGain <- calcConnector.calculateRttShareTotalGainAndTax(answers, deductionAnswers, maxAEA.get, incomeAnswers)
    } yield {
      pdfGenerator.ok(views.finalSummaryReport(answers,
        deductionAnswers,
        incomeAnswers,
        totalGain.get,
        taxYear.get,
        taxYear.get.taxYearSupplied == currentTaxYear,
        totalCosts,
        chargeableGain.get.deductions
      ),
        host).asScala().withHeaders("Content-Disposition" -> s"""attachment; filename="${Messages("calc.resident.summary.title")}.pdf"""")
    }).recoverToStart(homeLink, sessionTimeoutUrl)
  }
}
