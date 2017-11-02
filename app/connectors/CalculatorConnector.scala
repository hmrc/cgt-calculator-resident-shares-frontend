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

package connectors

import common.Dates._
import common.KeystoreKeys.ResidentShareKeys
import config.{CalculatorSessionCache, WSHttp}
import constructors.CalculateRequestConstructor
import models._
import models.resident._
import models.resident.income.{CurrentIncomeModel, PersonalAllowanceModel}
import models.resident.shares.gain.{DidYouInheritThemModel, ValueBeforeLegislationStartModel}
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel, OwnerBeforeLegislationStartModel}
import org.joda.time.{DateTime}
import java.time.LocalDate
import play.api.libs.json.Format
import play.api.mvc.Results._
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}
import uk.gov.hmrc.play.frontend.exceptions.ApplicationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpResponse}

object CalculatorConnector extends CalculatorConnector with ServicesConfig with AppName {
  override val sessionCache = CalculatorSessionCache
  override val http = WSHttp
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet
  val serviceUrl: String
  val homeLink = controllers.routes.GainController.disposalDate().url

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

  def getMinimumDate()(implicit  hc : HeaderCarrier): Future[LocalDate] = {
    http.GET[DateTime](s"$serviceUrl/capital-gains-calculator/minimum-date").map { date =>
      LocalDate.of(date.getYear, date.getMonthOfYear, date.getDayOfMonth)
    }
  }

  def saveFormData[T](key: String, data: T)(implicit hc: HeaderCarrier, formats: Format[T]): Future[CacheMap] = {
    sessionCache.cache(key, data)
  }

  def fetchAndGetFormData[T](key: String)(implicit hc: HeaderCarrier, formats: Format[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key)
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

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }

  //Rtt share calculation methods
  //scalastyle:off
  def getShareGainAnswers(implicit hc: HeaderCarrier): Future[GainAnswersModel] = {

    val disposalDate = fetchAndGetFormData[DisposalDateModel](ResidentShareKeys.disposalDate)
      .map(formData => constructDate(formData.get.day, formData.get.month, formData.get.year))

    val soldForLessThanWorth = fetchAndGetFormData[SellForLessModel](ResidentShareKeys.sellForLess)
      .map(_.get.sellForLess)

    val disposalValue = fetchAndGetFormData[DisposalValueModel](ResidentShareKeys.disposalValue)
      .map(_.map(_.amount))

    val worthWhenSoldForLess = fetchAndGetFormData[WorthWhenSoldForLessModel](ResidentShareKeys.worthWhenSoldForLess)
      .map(_.map(_.amount))

    val disposalCosts = fetchAndGetFormData[DisposalCostsModel](ResidentShareKeys.disposalCosts)
      .map(_.get.amount)

    val ownedBeforeTaxStartDate = fetchAndGetFormData[OwnerBeforeLegislationStartModel](ResidentShareKeys.ownerBeforeLegislationStart)
      .map(_.get.ownerBeforeLegislationStart)

    val valueBeforeLegislationStart = fetchAndGetFormData[ValueBeforeLegislationStartModel](ResidentShareKeys.valueBeforeLegislationStart)
      .map(_.map(_.amount))

    val inheritedTheShares = fetchAndGetFormData[DidYouInheritThemModel](ResidentShareKeys.didYouInheritThem)
      .map(_.map(_.wereInherited))

    val worthWhenInherited = fetchAndGetFormData[WorthWhenInheritedModel](ResidentShareKeys.worthWhenInherited)
      .map(_.map(_.amount))

    val acquisitionValue = fetchAndGetFormData[AcquisitionValueModel](ResidentShareKeys.acquisitionValue)
      .map(_.map(_.amount))

    val acquisitionCosts = fetchAndGetFormData[AcquisitionCostsModel](ResidentShareKeys.acquisitionCosts)
      .map(_.get.amount)

    for {
      disposalDate <- disposalDate
      soldForLessThanWorth <- soldForLessThanWorth
      disposalValue <- disposalValue
      worthWhenSoldForLess <- worthWhenSoldForLess
      disposalCosts <- disposalCosts
      ownedBeforeTaxStartDate <- ownedBeforeTaxStartDate
      valueBeforeLegislationStart <- valueBeforeLegislationStart
      inheritedTheShares <- inheritedTheShares
      worthWhenInherited <- worthWhenInherited
      acquisitionValue <- acquisitionValue
      acquisitionCosts <- acquisitionCosts
    } yield GainAnswersModel(
      disposalDate,
      soldForLessThanWorth,
      disposalValue,
      worthWhenSoldForLess,
      disposalCosts,
      ownedBeforeTaxStartDate,
      valueBeforeLegislationStart,
      inheritedTheShares,
      worthWhenInherited,
      acquisitionValue,
      acquisitionCosts
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  //scalastyle:on

  def getShareDeductionAnswers(implicit hc: HeaderCarrier): Future[DeductionGainAnswersModel] = {
    val broughtForwardModel = fetchAndGetFormData[LossesBroughtForwardModel](ResidentShareKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[LossesBroughtForwardValueModel](ResidentShareKeys.lossesBroughtForwardValue)

    for {
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
    } yield {
      resident.shares.DeductionGainAnswersModel(
        broughtForward,
        broughtForwardValue)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getShareIncomeAnswers(implicit hc: HeaderCarrier): Future[IncomeAnswersModel] = {
    val currentIncomeModel = fetchAndGetFormData[CurrentIncomeModel](ResidentShareKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[PersonalAllowanceModel](ResidentShareKeys.personalAllowance)

    for {
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(currentIncome, personalAllowance)
    }
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def calculateRttShareGrossGain(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-gain" +
      CalculateRequestConstructor.totalGainRequestString(input)
    )
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
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
        "cgt-calc-resident-shares-fe",
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
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }

  def getSharesTotalCosts(input: GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-costs" + CalculateRequestConstructor.totalGainRequestString(input))
  }.recover {
    case e: NoSuchElementException =>
      throw ApplicationException(
        "cgt-calc-resident-shares-fe",
        Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)),
        e.getMessage
      )
  }
}
