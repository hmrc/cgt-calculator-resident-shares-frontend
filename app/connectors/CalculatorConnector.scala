/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate

import config.ApplicationConfig
import constructors.CalculateRequestConstructor
import javax.inject.Inject
import models._
import models.resident._
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.joda.time.DateTime
import play.api.libs.json.JodaReads._
import play.api.mvc.Results._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CalculatorConnector @Inject()(http: DefaultHttpClient,
                                    appConfig: ApplicationConfig) {

  val serviceUrl: String = appConfig.baseUrl

  val homeLink: String = controllers.routes.GainController.disposalDate().url

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def getMinimumDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    http.GET[DateTime](s"$serviceUrl/capital-gains-calculator/minimum-date").map { date =>
      LocalDate.of(date.getYear, date.getMonthOfYear, date.getDayOfMonth)
    }
  }

  def getFullAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-full-aea?taxYear=$taxYear")
  }

  def getPartialAEA(taxYear: Int)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-partial-aea?taxYear=$taxYear")
  }

  def getPA(taxYear: Int, isEligibleBlindPersonsAllowance: Boolean = false)(implicit hc: HeaderCarrier): Future[Option[BigDecimal]] = {
    http.GET[Option[BigDecimal]](s"$serviceUrl/capital-gains-calculator/tax-rates-and-bands/max-pa?taxYear=$taxYear" +
      s"${
        if (isEligibleBlindPersonsAllowance) s"&isEligibleBlindPersonsAllowance=true"
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
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
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
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
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
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getSharesTotalCosts(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-costs" + CalculateRequestConstructor.totalGainRequestString(input))
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }
}
