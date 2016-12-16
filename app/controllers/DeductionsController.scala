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

package controllers

import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.resident.JourneyKeys
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import forms.AllowableLossesForm._
import forms.AllowableLossesValueForm._
import forms.AnnualExemptAmountForm._
import forms.LossesBroughtForwardForm._
import forms.LossesBroughtForwardValueForm._
import forms.OtherPropertiesForm._
import models.resident.shares.GainAnswersModel
import models.resident.{AllowableLossesValueModel, _}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Call, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.{calculation => commonViews}
import views.html.calculation.{deductions => views}

import scala.concurrent.Future

object DeductionsController extends DeductionsController {
  val calcConnector = CalculatorConnector
}

trait DeductionsController extends ValidActiveSession {

  val calcConnector: CalculatorConnector
  override val homeLink = routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink
  val navTitle = Messages("calc.base.resident.shares.home")

  def getDisposalDate(implicit hc: HeaderCarrier): Future[Option[DisposalDateModel]] = {
    calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
  }

  def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
    Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
  }

  def totalGain(answerSummary: GainAnswersModel, hc: HeaderCarrier): Future[BigDecimal] = calcConnector.calculateRttShareGrossGain(answerSummary)(hc)

  def answerSummary(hc: HeaderCarrier): Future[GainAnswersModel] = calcConnector.getShareGainAnswers(hc)


  def positiveAEACheck(model: AnnualExemptAmountModel)(implicit hc: HeaderCarrier): Future[Boolean] = {
    Future(model.amount > 0)
  }

  def taxYearStringToInteger(taxYear: String): Future[Int] = {
    Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
  }

  def positiveChargeableGainCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      gainAnswers <- calcConnector.getShareGainAnswers
      chargeableGainAnswers <- calcConnector.getShareDeductionAnswers
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- calcConnector.getFullAEA(year)
      chargeableGain <- calcConnector.calculateRttShareChargeableGain(gainAnswers, chargeableGainAnswers, maxAEA.get).map(_.get.chargeableGain)
    } yield chargeableGain

    match {
      case result if result.>(0) => true
      case _ => false
    }
  }

  //################# Other Disposal Actions #########################
  val otherDisposals = ValidateSession.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
        case Some(data) => Ok(views.otherDisposals(otherPropertiesForm.fill(data), taxYear, homeLink))
        case None => Ok(views.otherDisposals(otherPropertiesForm, taxYear, homeLink))
      }
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  val submitOtherDisposals = ValidateSession.async { implicit request =>

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      otherPropertiesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(views.otherDisposals(errors, taxYear, homeLink))),
        success => {
          calcConnector.saveFormData[OtherPropertiesModel](keystoreKeys.otherProperties, success)
          if (success.hasOtherProperties) {
            Future.successful(Redirect(routes.DeductionsController.allowableLosses()))
          } else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(taxYear.get)
    } yield route
  }

  //################# Brought Forward Losses Actions ##############################

  private val lossesBroughtForwardPostAction = controllers.routes.DeductionsController.submitLossesBroughtForward()

  def otherPropertiesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[OtherPropertiesModel](keystoreKeys.otherProperties).map {
      case Some(data) => data.hasOtherProperties
      case None => false
    }
  }

  def allowableLossesCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
      case Some(data) => data.isClaiming
      case None => false
    }
  }

  def displayAnnualExemptAmountCheck(claimedOtherProperties: Boolean, claimedAllowableLosses: Boolean)(implicit hc: HeaderCarrier): Future[Boolean] = {
    calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
      case Some(result) if claimedAllowableLosses && claimedOtherProperties => result.amount == 0
      case _ if claimedOtherProperties && !claimedAllowableLosses => true
      case _ => false
    }
  }

  def displayAnnualExemptAmountCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      disposedOtherProperties <- otherPropertiesCheck
      claimedAllowableLosses <- allowableLossesCheck
      displayAnnualExemptAmount <- displayAnnualExemptAmountCheck(disposedOtherProperties, claimedAllowableLosses)
    } yield displayAnnualExemptAmount
  }

  def lossesBroughtForwardBackUrl(implicit hc: HeaderCarrier): Future[String] = {

    for {
      otherPropertiesClaimed <- otherPropertiesCheck
      allowableLossesClaimed <- allowableLossesCheck
    } yield (otherPropertiesClaimed, allowableLossesClaimed)

    match {
      case (false, _) => routes.DeductionsController.otherDisposals().url
      case (true, false) => routes.DeductionsController.allowableLosses().url
      case (true, true) => routes.DeductionsController.allowableLossesValue().url
    }
  }

  val lossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backLinkUrl: String, taxYear: TaxYearModel, otherPropertiesClaimed: Boolean): Future[Result] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
        case Some(data) => Ok(commonViews.deductions.lossesBroughtForward(lossesBroughtForwardForm.fill(data), lossesBroughtForwardPostAction,
          backLinkUrl, taxYear, otherPropertiesClaimed, homeLink, navTitle))
        case _ => Ok(commonViews.deductions.lossesBroughtForward(lossesBroughtForwardForm, lossesBroughtForwardPostAction, backLinkUrl, taxYear,
          otherPropertiesClaimed, homeLink, navTitle))
      }
    }

    for {
      backLinkUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      otherPropertiesClaimed <- otherPropertiesCheck
      finalResult <- routeRequest(backLinkUrl, taxYear.get, otherPropertiesClaimed)
    } yield finalResult

  }


  val submitLossesBroughtForward = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String, taxYearModel: TaxYearModel, otherPropertiesClaimed: Boolean): Future[Result] = {
      lossesBroughtForwardForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.deductions.lossesBroughtForward(errors, lossesBroughtForwardPostAction, backUrl, taxYearModel,
          otherPropertiesClaimed, homeLink, navTitle))),
        success => {
          calcConnector.saveFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward, success)

          if (success.option) Future.successful(Redirect(routes.DeductionsController.lossesBroughtForwardValue()))
          else {
            displayAnnualExemptAmountCheck.flatMap { displayAnnualExemptAmount =>
              if (displayAnnualExemptAmount) Future.successful(Redirect(routes.DeductionsController.annualExemptAmount()))
              else {
                positiveChargeableGainCheck.map { positiveChargeableGain =>
                  if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
                  else Redirect(routes.SummaryController.summary())
                }
              }
            }
          }
        }
      )
    }

    for {
      backUrl <- lossesBroughtForwardBackUrl
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      otherPropertiesClaimed <- otherPropertiesCheck
      route <- routeRequest(backUrl, taxYear.get, otherPropertiesClaimed)
    } yield route

  }

  //################# Brought Forward Losses Value Actions ##############################

  private val lossesBroughtForwardValueBackLink: String = routes.DeductionsController.lossesBroughtForward().url
  private val lossesBroughtForwardValuePostAction: Call = routes.DeductionsController.submitLossesBroughtForwardValue()

  val lossesBroughtForwardValue = ValidateSession.async { implicit request =>

    def retrieveKeystoreData(): Future[Form[LossesBroughtForwardValueModel]] = {
      calcConnector.fetchAndGetFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue).map {
        case Some(data) => lossesBroughtForwardValueForm.fill(data)
        case _ => lossesBroughtForwardValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[LossesBroughtForwardValueModel]): Future[Result] = {
      Future.successful(Ok(commonViews.deductions.lossesBroughtForwardValue(
        formData,
        taxYear,
        navBackLink = lossesBroughtForwardValueBackLink,
        navHomeLink = homeLink,
        postAction = lossesBroughtForwardValuePostAction,
        navTitle
      )))
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- retrieveKeystoreData()
      route <- routeRequest(taxYear.get, formData)
    } yield route
  }

  val submitLossesBroughtForwardValue = ValidateSession.async { implicit request =>
    lossesBroughtForwardValueForm.bindFromRequest.fold(
      errors => {
        for {
          disposalDate <- getDisposalDate
          disposalDateString <- formatDisposalDate(disposalDate.get)
          taxYear <- calcConnector.getTaxYear(disposalDateString)
        } yield {
          BadRequest(commonViews.deductions.lossesBroughtForwardValue(
            errors,
            taxYear.get,
            navBackLink = lossesBroughtForwardValueBackLink,
            navHomeLink = homeLink,
            postAction = lossesBroughtForwardValuePostAction,
            navTitle = navTitle))
        }
      },
      success => {
        calcConnector.saveFormData[LossesBroughtForwardValueModel](keystoreKeys.lossesBroughtForwardValue, success)
        displayAnnualExemptAmountCheck.flatMap { displayAnnualExemptAmount =>
          if (displayAnnualExemptAmount) Future.successful(Redirect(routes.DeductionsController.annualExemptAmount()))
          else {
            positiveChargeableGainCheck.map { positiveChargeableGain =>
              if (positiveChargeableGain) Redirect(routes.IncomeController.currentIncome())
              else Redirect(routes.SummaryController.summary())
            }
          }
        }
      }
    )
  }

  //################# Allowable Losses Actions #########################

  val allowableLosses = ValidateSession.async { implicit request =>

    val postAction = controllers.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.routes.DeductionsController.otherDisposals().toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      calcConnector.fetchAndGetFormData[AllowableLossesModel](keystoreKeys.allowableLosses).map {
        case Some(data) => Ok(commonViews.deductions.allowableLosses(allowableLossesForm.fill(data), taxYear, postAction, backLink, homeLink, navTitle))
        case None => Ok(commonViews.deductions.allowableLosses(allowableLossesForm, taxYear, postAction, backLink, homeLink, navTitle))
      }
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  val submitAllowableLosses = ValidateSession.async { implicit request =>

    val postAction = controllers.routes.DeductionsController.submitAllowableLosses()
    val backLink = Some(controllers.routes.DeductionsController.otherDisposals().toString())

    def routeRequest(taxYear: TaxYearModel): Future[Result] = {
      allowableLossesForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.deductions.allowableLosses(errors, taxYear, postAction, backLink, homeLink, navTitle))),
        success => {
          calcConnector.saveFormData[AllowableLossesModel](keystoreKeys.allowableLosses, success)
          if (success.isClaiming) {
            Future.successful(Redirect(routes.DeductionsController.allowableLossesValue()))
          }
          else {
            Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
          }
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      finalResult <- routeRequest(taxYear.get)
    } yield finalResult
  }

  //################# Allowable Losses Value Actions ############################

  private val allowableLossesValueHomeLink = controllers.routes.GainController.disposalDate().toString
  private val allowableLossesValuePostAction = controllers.routes.DeductionsController.submitAllowableLossesValue()
  private val allowableLossesValueBackLink = Some(controllers.routes.DeductionsController.allowableLosses().toString)

  val allowableLossesValue = ValidateSession.async { implicit request =>

    def fetchStoredAllowableLosses(): Future[Form[AllowableLossesValueModel]]  = {
      calcConnector.fetchAndGetFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue).map {
        case Some(data) => allowableLossesValueForm.fill(data)
        case _ => allowableLossesValueForm
      }
    }

    def routeRequest(taxYear: TaxYearModel, formData: Form[AllowableLossesValueModel]): Future[Result] = {
      Future.successful(Ok(commonViews.deductions.allowableLossesValue(formData, taxYear,
        allowableLossesValueHomeLink,
        allowableLossesValuePostAction,
        allowableLossesValueBackLink,
        navTitle)))
    }

    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      formData <- fetchStoredAllowableLosses()
      finalResult <- routeRequest(taxYear.get, formData)
    } yield finalResult
  }

  val submitAllowableLossesValue = ValidateSession.async { implicit request =>

    def routeRequest(taxYearModel: TaxYearModel): Future[Result] = {
      allowableLossesValueForm.bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.deductions.allowableLossesValue(errors, taxYearModel,
          allowableLossesValueHomeLink,
          allowableLossesValuePostAction,
          allowableLossesValueBackLink,
          navTitle))),
        success => {
          calcConnector.saveFormData[AllowableLossesValueModel](keystoreKeys.allowableLossesValue, success)
          Future.successful(Redirect(routes.DeductionsController.lossesBroughtForward()))
        }
      )
    }
    for {
      disposalDate <- getDisposalDate
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      route <- routeRequest(taxYear.get)
    } yield route
  }


  //################# Annual Exempt Amount Input Actions #############################

  private def annualExemptAmountBackLink(implicit hc: HeaderCarrier): Future[Option[String]] = calcConnector
    .fetchAndGetFormData[LossesBroughtForwardModel](keystoreKeys.lossesBroughtForward).map {
    case Some(LossesBroughtForwardModel(true)) =>
      Some(controllers.routes.DeductionsController.lossesBroughtForwardValue().toString)
    case _ =>
      Some(controllers.routes.DeductionsController.lossesBroughtForward().toString)
  }

  private val annualExemptAmountPostAction = controllers.routes.DeductionsController.submitAnnualExemptAmount()

  val annualExemptAmount = ValidateSession.async { implicit request =>
    def routeRequest(backLink: Option[String]) = {
      calcConnector.fetchAndGetFormData[AnnualExemptAmountModel](keystoreKeys.annualExemptAmount).map {
        case Some(data) => Ok(commonViews.deductions.annualExemptAmount(annualExemptAmountForm().fill(data), backLink,
          annualExemptAmountPostAction, homeLink, JourneyKeys.shares, navTitle))
        case None => Ok(commonViews.deductions.annualExemptAmount(annualExemptAmountForm(), backLink,
          annualExemptAmountPostAction, homeLink, JourneyKeys.shares, navTitle))
      }
    }

    for {
      backLink <- annualExemptAmountBackLink(hc)
      result <- routeRequest(backLink)
    } yield result
  }

  val submitAnnualExemptAmount = ValidateSession.async { implicit request =>

    def taxYearStringToInteger (taxYear: String): Future[Int] = {
      Future.successful((taxYear.take(2) + taxYear.takeRight(2)).toInt)
    }

    def formatDisposalDate(disposalDateModel: DisposalDateModel): Future[String] = {
      Future.successful(s"${disposalDateModel.year}-${disposalDateModel.month}-${disposalDateModel.day}")
    }

    def getMaxAEA(taxYear: Int): Future[Option[BigDecimal]] = {
      calcConnector.getFullAEA(taxYear)
    }

    def routeRequest(maxAEA: BigDecimal, backLink: Option[String]): Future[Result] = {
      annualExemptAmountForm(maxAEA).bindFromRequest.fold(
        errors => Future.successful(BadRequest(commonViews.deductions.annualExemptAmount(errors, backLink,
          annualExemptAmountPostAction, homeLink, JourneyKeys.shares, navTitle))),
        success => {
          for {
            save <- calcConnector.saveFormData(keystoreKeys.annualExemptAmount, success)
            positiveAEA <- positiveAEACheck(success)
            positiveChargeableGain <- positiveChargeableGainCheck
          } yield (positiveAEA, positiveChargeableGain)

          match {
            case (false, true) => Redirect(routes.IncomeController.previousTaxableGains())
            case (_, false) => Redirect(routes.SummaryController.summary())
            case _ => Redirect(routes.IncomeController.currentIncome())
          }
        }
      )
    }
    for {
      disposalDate <- calcConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      disposalDateString <- formatDisposalDate(disposalDate.get)
      taxYear <- calcConnector.getTaxYear(disposalDateString)
      year <- taxYearStringToInteger(taxYear.get.calculationTaxYear)
      maxAEA <- getMaxAEA(year)
      backLink <- annualExemptAmountBackLink(hc)
      route <- routeRequest(maxAEA.get, backLink)
    } yield route
  }
}