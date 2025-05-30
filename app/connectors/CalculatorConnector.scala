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

package connectors

import config.ApplicationConfig
import constructors.CalculateRequestConstructor
import models._
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import play.api.mvc.Results._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatorConnector @Inject()(http: HttpClientV2,
                                    appConfig: ApplicationConfig)(implicit ec: ExecutionContext) {

  private val headers = "Accept" -> "application/vnd.hmrc.1.0+json"

  private val serviceUrl = appConfig.baseUrl

  def getMinimumDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    http.get(url"$serviceUrl/capital-gains-calculator/minimum-date").transform(_.addHttpHeaders(headers)).execute[LocalDate]
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear").transform(_.addHttpHeaders(headers)).execute[Option[BigDecimal]]
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false,
            isEligibleMarriageAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {

    val blindPersonAllowanceParams = if (isEligibleBlindPersonsAllowance) Seq("isEligibleBlindPersonsAllowance" -> true) else Nil
    val eligibleMarriageAllowanceParams = if (isEligibleMarriageAllowance) Seq("sEligibleMarriageAllowance" -> true) else Nil

    val params = Seq("taxYear" -> taxYear) ++ blindPersonAllowanceParams ++ eligibleMarriageAllowanceParams

    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?$params")
      .transform(_.addHttpHeaders(headers)).execute[Option[BigDecimal]]
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear").transform(_.addHttpHeaders(headers)).execute[Option[resident.TaxYearModel]]
  }

  def calculateRttShareGrossGain(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    val totalGainReq = CalculateRequestConstructor.totalGainRequest(input)

    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-total-gain?$totalGainReq").transform(_.addHttpHeaders(headers)).execute[BigDecimal]
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()),
        e.getMessage
      )
  }

  def calculateRttShareChargeableGain(totalGainInput: GainAnswersModel,
                                      chargeableGainInput: DeductionGainAnswersModel,
                                      maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
    val totalGainReq = CalculateRequestConstructor.totalGainRequest(totalGainInput)
    val chargeableGainReq = CalculateRequestConstructor.chargeableGainRequest(chargeableGainInput, maxAEA)

    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-chargeable-gain?${totalGainReq ++ chargeableGainReq}").transform(_.addHttpHeaders(headers)).execute[Option[resident.ChargeableGainResultModel]]
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()),
        e.getMessage
      )
  }

  def calculateRttShareTotalGainAndTax(totalGainInput: GainAnswersModel,
                                       chargeableGainInput: DeductionGainAnswersModel,
                                       maxAEA: BigDecimal,
                                       incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier):
  Future[Option[resident.TotalGainAndTaxOwedModel]] = {

    val totalGainReq = CalculateRequestConstructor.totalGainRequest(totalGainInput)
    val chargeableGainReq = CalculateRequestConstructor.chargeableGainRequest(chargeableGainInput, maxAEA)
    val incomeAnswersReq = CalculateRequestConstructor.incomeAnswersRequest(incomeAnswers)


    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-resident-capital-gains-tax?${totalGainReq ++ chargeableGainReq ++ incomeAnswersReq}").transform(_.addHttpHeaders(headers)).execute[Option[resident.TotalGainAndTaxOwedModel]]
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()),
        e.getMessage
      )
  }

  def getSharesTotalCosts(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    val totalGainReq = CalculateRequestConstructor.totalGainRequest(input)
    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-total-costs?$totalGainReq").transform(_.addHttpHeaders(headers)).execute[BigDecimal]
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()),
        e.getMessage
      )
  }
}
