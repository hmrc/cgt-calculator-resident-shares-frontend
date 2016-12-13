/*
 * Copyright 2016 HM Revenue & Customs
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
import constructors.resident.shares
import models._
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CalculatorConnector extends CalculatorConnector with ServicesConfig {
  override val sessionCache = CalculatorSessionCache
  override val http = WSHttp
  override val serviceUrl = baseUrl("capital-gains-calculator")
}

trait CalculatorConnector {

  val sessionCache: SessionCache
  val http: HttpGet
  val serviceUrl: String

  implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")

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

  def getTaxYear(taxYear: String)(implicit hc: HeaderCarrier): Future[Option[resident.TaxYearModel]] = {
    http.GET[Option[resident.TaxYearModel]](s"$serviceUrl/capital-gains-calculator/tax-year?date=$taxYear")
  }

  def clearKeystore(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    sessionCache.remove()
  }

  //Rtt share calculation methods
  //scalastyle:off
  def getShareGainAnswers(implicit hc: HeaderCarrier): Future[resident.shares.GainAnswersModel] = {
    val disposalDate = fetchAndGetFormData[resident.DisposalDateModel](ResidentShareKeys.disposalDate).map(formData =>
      constructDate(formData.get.day, formData.get.month, formData.get.year))
    val soldForLessThanWorth = fetchAndGetFormData[resident.SellForLessModel](ResidentShareKeys.sellForLess).map(_.get.sellForLess)
    val disposalValue = fetchAndGetFormData[resident.DisposalValueModel](ResidentShareKeys.disposalValue).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val worthWhenSoldForLess = fetchAndGetFormData[resident.WorthWhenSoldForLessModel](ResidentShareKeys.worthWhenSoldForLess).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val disposalCosts = fetchAndGetFormData[resident.DisposalCostsModel](ResidentShareKeys.disposalCosts).map(_.get.amount)
    val ownedBeforeTaxStartDate = fetchAndGetFormData[resident.shares.OwnerBeforeLegislationStartModel](ResidentShareKeys.ownerBeforeLegislationStart).map(_.get.ownerBeforeLegislationStart)
    val valueBeforeLegislationStart = fetchAndGetFormData[resident.shares.gain.ValueBeforeLegislationStartModel](ResidentShareKeys.valueBeforeLegislationStart).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val inheritedTheShares = fetchAndGetFormData[resident.shares.gain.DidYouInheritThemModel](ResidentShareKeys.didYouInheritThem).map {
      case Some(data) => Some(data.wereInherited)
      case _ => None
    }
    val worthWhenInherited = fetchAndGetFormData[resident.WorthWhenInheritedModel](ResidentShareKeys.worthWhenInherited).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val acquisitionValue = fetchAndGetFormData[resident.AcquisitionValueModel](ResidentShareKeys.acquisitionValue).map {
      case Some(data) => Some(data.amount)
      case _ => None
    }
    val acquisitionCosts = fetchAndGetFormData[resident.AcquisitionCostsModel](ResidentShareKeys.acquisitionCosts).map(_.get.amount)

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
    } yield resident.shares.GainAnswersModel(
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
  }

  //scalastyle:on

  def getShareDeductionAnswers(implicit hc: HeaderCarrier): Future[resident.shares.DeductionGainAnswersModel] = {
    val otherPropertiesModel = fetchAndGetFormData[resident.OtherPropertiesModel](ResidentShareKeys.otherProperties)
    val allowableLossesModel = fetchAndGetFormData[resident.AllowableLossesModel](ResidentShareKeys.allowableLosses)
    val allowableLossesValueModel = fetchAndGetFormData[resident.AllowableLossesValueModel](ResidentShareKeys.allowableLossesValue)
    val broughtForwardModel = fetchAndGetFormData[resident.LossesBroughtForwardModel](ResidentShareKeys.lossesBroughtForward)
    val broughtForwardValueModel = fetchAndGetFormData[resident.LossesBroughtForwardValueModel](ResidentShareKeys.lossesBroughtForwardValue)
    val annualExemptAmountModel = fetchAndGetFormData[resident.AnnualExemptAmountModel](ResidentShareKeys.annualExemptAmount)

    for {
      otherProperties <- otherPropertiesModel
      allowableLosses <- allowableLossesModel
      allowableLossesValue <- allowableLossesValueModel
      broughtForward <- broughtForwardModel
      broughtForwardValue <- broughtForwardValueModel
      annualExemptAmount <- annualExemptAmountModel
    } yield {
      resident.shares.DeductionGainAnswersModel(
        otherProperties,
        allowableLosses,
        allowableLossesValue,
        broughtForward,
        broughtForwardValue,
        annualExemptAmount)
    }
  }

  def getShareIncomeAnswers(implicit hc: HeaderCarrier): Future[resident.IncomeAnswersModel] = {
    val previousTaxableGainsModel = fetchAndGetFormData[resident.income.PreviousTaxableGainsModel](ResidentShareKeys.previousTaxableGains)
    val currentIncomeModel = fetchAndGetFormData[resident.income.CurrentIncomeModel](ResidentShareKeys.currentIncome)
    val personalAllowanceModel = fetchAndGetFormData[resident.income.PersonalAllowanceModel](ResidentShareKeys.personalAllowance)

    for {
      previousGains <- previousTaxableGainsModel
      currentIncome <- currentIncomeModel
      personalAllowance <- personalAllowanceModel
    } yield {
      resident.IncomeAnswersModel(previousGains, currentIncome, personalAllowance)
    }
  }

  def calculateRttShareGrossGain(input: resident.shares.GainAnswersModel)(implicit hc: HeaderCarrier): Future[BigDecimal] = {
    http.GET[BigDecimal](s"$serviceUrl/capital-gains-calculator/shares/calculate-total-gain" +
      shares.CalculateRequestConstructor.totalGainRequestString(input)
    )
  }

  def calculateRttShareChargeableGain(totalGainInput: resident.shares.GainAnswersModel,
                                      chargeableGainInput: resident.shares.DeductionGainAnswersModel,
                                      maxAEA: BigDecimal)(implicit hc: HeaderCarrier): Future[Option[resident.ChargeableGainResultModel]] = {
    http.GET[Option[resident.ChargeableGainResultModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-chargeable-gain" +
      shares.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      shares.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA)

    )
  }

  def calculateRttShareTotalGainAndTax(totalGainInput: resident.shares.GainAnswersModel,
                                       chargeableGainInput: resident.shares.DeductionGainAnswersModel,
                                       maxAEA: BigDecimal,
                                       incomeAnswers: resident.IncomeAnswersModel)(implicit hc: HeaderCarrier):
  Future[Option[resident.TotalGainAndTaxOwedModel]] = {
    http.GET[Option[resident.TotalGainAndTaxOwedModel]](s"$serviceUrl/capital-gains-calculator/shares/calculate-resident-capital-gains-tax" +
      shares.CalculateRequestConstructor.totalGainRequestString(totalGainInput) +
      shares.CalculateRequestConstructor.chargeableGainRequestString(chargeableGainInput, maxAEA) +
      shares.CalculateRequestConstructor.incomeAnswersRequestString(chargeableGainInput, incomeAnswers)
    )
  }
}