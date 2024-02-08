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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CalculatorConnector @Inject()(http: DefaultHttpClient,
                                    appConfig: ApplicationConfig)(implicit ec: ExecutionContext) {

  val serviceUrl: String = appConfig.baseUrl

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def getMinimumDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    http.GET[LocalDate](s"$serviceUrl/capital-gains-calculator/minimum-date")
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false,
            isEligibleMarriageAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
        else ""
      }" +
      s"${
        if (isEligibleMarriageAllowance) s"&isEligibleMarriageAllowance=true"
        else ""
      }"
    )
  }

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[TaxYearModel]] = {
    http.GET[Option[resident.TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }

  def calculateRttShareGrossGain(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-gain" +
      CalculateRequestConstructor.totalGainRequestString(input)
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def calculateRttShareChargeableGain(totalGainInput: GainAnswersModel,
                                      chargeableGainInput: DeductionGainAnswersModel,
                                      maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-chargeable-gain" +
      CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def calculateRttShareTotalGainAndTax(totalGainInput: GainAnswersModel,
                                       chargeableGainInput: DeductionGainAnswersModel,
                                       maxAEA: BigDecimal,
                                       incomeAnswers: IncomeAnswersModel)(implicit hc: HeaderCarrier):
  Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-resident-capital-gains-tax" +
      CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }

  def getSharesTotalCosts(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-costs" + CalculateRequestConstructor.totalGainRequestString(input))
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout),
        e.getMessage
      )
  }
}
