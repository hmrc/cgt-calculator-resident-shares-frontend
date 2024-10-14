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

  val headers: (String, String) = "Accept" -> "application/vnd.hmrc.1.0+json"

  val serviceUrl: String = appConfig.baseUrl

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def getMinimumDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    http.get(url"$serviceUrl/capital-gains-calculator/minimum-date").transform(_.addHttpHeaders(headers)).execute[LocalDate]
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear").transform(_.addHttpHeaders(headers)).execute[Option[BigDecimal]]
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear").transform(_.addHttpHeaders(headers)).execute[Option[BigDecimal]]
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false,
            isEligibleMarriageAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    val param1 = {
      if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
      else ""
    }

    val param2 = {
      if (isEligibleMarriageAllowance) s"&isEligibleMarriageAllowance=true"
      else ""
    }

    http.get(url"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear+$param1+$param2")
  .transform(_.addHttpHeaders(headers)).execute[Option[BigDecimal]]
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.get(url"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear").transform(_.addHttpHeaders(headers)).execute[Option[resident.TaxYearModel]]
  }

  def calculateRttShareGrossGain(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    val totalGainRequestStringValue = CalculateRequestConstructor.totalGainRequestString(input)

    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-total-gain?$totalGainRequestStringValue").transform(_.addHttpHeaders(headers)).execute[BigDecimal]
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
    val totalGainInputStringValue = CalculateRequestConstructor.totalGainRequestString(totalGainInput)
    val chargeableGainInputStringValue = CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-chargeable-gain?$totalGainInputStringValue+$chargeableGainInputStringValue").transform(_.addHttpHeaders(headers)).execute[Option[resident.ChargeableGainResultModel]]
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

    val totalGainRequestStringValue = CalculateRequestConstructor.totalGainRequestString(totalGainInput)
    val chargeableGainRequestStringValue = CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)
    val incomeAnswersRequestStringValue = CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)


    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-resident-capital-gains-tax?$totalGainRequestStringValue+$chargeableGainRequestStringValue+$incomeAnswersRequestStringValue").transform(_.addHttpHeaders(headers)).execute[Option[resident.TotalGainAndTaxOwedModel]]
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()),
        e.getMessage
      )
  }

  def getSharesTotalCosts(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    val totalGainRequestStringValue = CalculateRequestConstructor.totalGainRequestString(input)
    http.get(url"$serviceUrl/capital-gains-calculator/shares/calculate-total-costs?$totalGainRequestStringValue").transform(_.addHttpHeaders(headers)).execute[BigDecimal]
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout()),
        e.getMessage
      )
  }
}
