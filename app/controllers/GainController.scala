/*
 * Copyright 2023 HM Revenue & Customs
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
import common.{Dates, TaxDates}
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import forms.AcquisitionCostsForm._
import forms.AcquisitionValueForm._
import forms.DidYouInheritThemForm._
import forms.DisposalCostsForm._
import forms.DisposalDateForm._
import forms.DisposalValueForm._
import forms.OwnerBeforeLegislationStartForm._
import forms.SellForLessForm._
import forms.ValueBeforeLegislationStartForm._
import forms.WorthWhenInheritedForm._
import forms.WorthWhenSoldForLessForm._
import models.resident._
import models.resident.shares.OwnerBeforeLegislationStartModel
import models.resident.shares.gain.{DidYouInheritThemModel, ValueBeforeLegislationStartModel}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import services.SessionCacheService
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.gain._
import views.html.calculation.outsideTaxYear

import java.time.{LocalDate, ZoneId}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class GainController @Inject()(calcConnector: CalculatorConnector,
                               sessionCacheService: SessionCacheService,
                               mcc: MessagesControllerComponents,
                               acquisitionCostsView: acquisitionCosts,
                               acquisitionValueView: acquisitionValue,
                               disposalCostsView: disposalCosts,
                               disposalDateView: disposalDate,
                               disposalValueView: disposalValue,
                               didYouInheritThemView: didYouInheritThem,
                               ownerBeforeLegislationStartView: ownerBeforeLegislationStart,
                               sellForLessView: sellForLess,
                               valueBeforeLegislationStartView: valueBeforeLegislationStart,
                               worthWhenInheritedView: worthWhenInherited,
                               worthWhenSoldForLessView: worthWhenSoldForLess,
                               outsideTaxYearView: outsideTaxYear)
                               (implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def navTitle(implicit request : Request[_]): String = Messages("calc.base.resident.shares.home")(mcc.messagesApi.preferred(request))

  //################# Disposal Date Actions ####################
  def disposalDate: Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(disposalDateView(disposalDateForm())).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(disposalDateView(disposalDateForm().fill(data)))
        case None => Ok(disposalDateView(disposalDateForm()))
      }
    }
  }

  def submitDisposalDate: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears))
      else Future.successful(Redirect(routes.GainController.sellForLess))
    }

    def bindForm(minimumDate: LocalDate) = {
      disposalDateForm(minimumDate.atStartOfDay(ZoneId.of("Europe/London"))).bindFromRequest().fold(
        errors => {
          Future.successful(
          BadRequest(
            disposalDateView(errors.copy(errors = errors.errors.map { error =>
              if (error.key == "") error.copy(key = "disposalDateDay") else error
            }))
          )
        )},
        success => {
          (for {
            save <- sessionCacheService.saveFormData(keystoreKeys.disposalDate, success)
            taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
            route <- routeRequest(taxYearResult)
          } yield route).recoverToStart()
        }
      )
    }

    for {
      minimumDate <- calcConnector.getMinimumDate()
      result <- bindForm(minimumDate)
    } yield result
  }

  //################ Sell for Less Actions ######################
  private def sellForLessBackLink()(implicit request: Request[_]): Future[String] = {
    for {
      date <- sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${date.get.year}-${date.get.month}-${date.get.day}")
    } yield if (taxYear.get.isValidYear) routes.GainController.disposalDate.url else routes.GainController.outsideTaxYears.url
  }

  val sellForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[SellForLessModel], backLink: String) = {
      val view = model match {
        case Some(data) => sellForLessView(sellForLessForm.fill(data), backLink)
        case None => sellForLessView(sellForLessForm, backLink)
      }

      Future.successful(Ok(view))
    }

    (for {
      model <- sessionCacheService.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      backLink <- sellForLessBackLink()
      route <- routeRequest(model, backLink)
    } yield route).recoverToStart()

  }

  def submitSellForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sellForLessForm.bindFromRequest().fold(
      errors => {
        (for {
          backLink <- sellForLessBackLink()
          response <- Future.successful(BadRequest(sellForLessView(errors, backLink)))
        } yield response).recoverToStart()
      },
      success => {
        sessionCacheService.saveFormData[SellForLessModel](keystoreKeys.sellForLess, success).flatMap(
          _ =>success.sellForLess match {
            case true => Future.successful(Redirect(routes.GainController.worthWhenSoldForLess))
            case _ => Future.successful(Redirect(routes.GainController.disposalValue))
          }
        )
      }
    )
  }

  //################ Worth When Sold Actions ######################
  def worthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenSoldForLessModel](keystoreKeys.worthWhenSoldForLess).map {
      case Some(data) => Ok(worthWhenSoldForLessView(worthWhenSoldForLessForm.fill(data)))
      case _ => Ok(worthWhenSoldForLessView(worthWhenSoldForLessForm))
    }
  }

  def submitWorthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    worthWhenSoldForLessForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenSoldForLessView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.worthWhenSoldForLess, success).flatMap(
          _=>Future.successful(Redirect(routes.GainController.disposalCosts))
        )
      }
    )
  }


  //################ Outside Tax Years Actions ######################
  def outsideTaxYears: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      disposalDate <- sessionCacheService.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(outsideTaxYearView(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        isProperty = false,
        navBackLink = routes.GainController.disposalDate.url,
        continueUrl = routes.GainController.sellForLess.url,
      ))
    }).recoverToStart()
  }

  //################ Disposal Value Actions ######################
  def disposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(disposalValueView(disposalValueForm.fill(data)))
      case None => Ok(disposalValueView(disposalValueForm))
    }
  }

  def submitDisposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(disposalValueView(errors))),
      success => {
        sessionCacheService.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.disposalCosts))
        )
      }
    )
  }

  //################# Disposal Costs Actions ########################
  def disposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
      case Some(data) => Ok(disposalCostsView(disposalCostsForm.fill(data)))
      case None => Ok(disposalCostsView(disposalCostsForm))
    }
  }

  def submitDisposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    disposalCostsForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(disposalCostsView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.disposalCosts, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.ownerBeforeLegislationStart))
        )
      }
    )
  }


  //################# Owned Before 1982 Actions ########################
  private val ownerBeforeLegislationStartBackLink = Some(controllers.routes.GainController.disposalCosts.url)

  def ownerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart).map {
      case Some(data) => Ok(ownerBeforeLegislationStartView(ownerBeforeLegislationStartForm.fill(data), ownerBeforeLegislationStartBackLink))
      case None => Ok(ownerBeforeLegislationStartView(ownerBeforeLegislationStartForm, ownerBeforeLegislationStartBackLink))
    }
  }

  def submitOwnerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    ownerBeforeLegislationStartForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(ownerBeforeLegislationStartView(errors, ownerBeforeLegislationStartBackLink))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.ownerBeforeLegislationStart, success).flatMap(
          _ => success.ownerBeforeLegislationStart match {
            case true => Future.successful(Redirect(routes.GainController.valueBeforeLegislationStart))
            case _ => Future.successful(Redirect(routes.GainController.didYouInheritThem))
          }
        )
      }
    )
  }

  //################# What were they worth on 31 March 1982 Actions ########################
  def valueBeforeLegislationStart: Action[AnyContent] =  ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart).map {
      case Some(data) => Ok(valueBeforeLegislationStartView(valueBeforeLegislationStartForm.fill(data)))
      case None => Ok(valueBeforeLegislationStartView(valueBeforeLegislationStartForm))
    }
  }

  def submitValueBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    valueBeforeLegislationStartForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(valueBeforeLegislationStartView(errors))),
      success => {
        sessionCacheService.saveFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.acquisitionCosts))
        )
      }
    )
  }

  //################# Did you Inherit the Shares Actions ########################
  def didYouInheritThem: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem).map {
      case Some(data) => Ok(didYouInheritThemView(didYouInheritThemForm.fill(data)))
      case None => Ok(didYouInheritThemView(didYouInheritThemForm))
    }
  }

  def submitDidYouInheritThem: Action[AnyContent] = ValidateSession.async { implicit request =>
    didYouInheritThemForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(didYouInheritThemView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.didYouInheritThem, success).flatMap(
          _ => if (success.wereInherited) Future.successful(Redirect(routes.GainController.worthWhenInherited))
          else Future.successful(Redirect(routes.GainController.acquisitionValue))
        )
      }
    )
  }

  //################# Worth when Inherited Actions ########################
  def worthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(worthWhenInheritedView(worthWhenInheritedForm.fill(data)))
      case None => Ok(worthWhenInheritedView(worthWhenInheritedForm))
    }
  }

  def submitWorthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenInheritedForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(worthWhenInheritedView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.worthWhenInherited, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.acquisitionCosts))
        )
      }
    )
  }

  //################# Acquisition Value Actions ########################
  def acquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheService.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(acquisitionValueView(acquisitionValueForm.fill(data)))
      case None => Ok(acquisitionValueView(acquisitionValueForm))
    }
  }

  def submitAcquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest().fold(
      errors => Future.successful(BadRequest(acquisitionValueView(errors))),
      success => {
        sessionCacheService.saveFormData(keystoreKeys.acquisitionValue, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.acquisitionCosts))
        )
      }
    )
  }

  //################# Acquisition Costs Actions ########################
  private def acquisitionCostsBackLink: (OwnerBeforeLegislationStartModel, Option[DidYouInheritThemModel]) => String = {
    case (x,_) if x.ownerBeforeLegislationStart => routes.GainController.valueBeforeLegislationStart.url
    case (_,y) if y.get.wereInherited => routes.GainController.worthWhenInherited.url
    case (_,_) => routes.GainController.acquisitionValue.url
  }

  def acquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String): Future[Result] = {
      sessionCacheService.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
        case Some(data) => Ok(acquisitionCostsView(acquisitionCostsForm.fill(data), Some(backLink)))
        case None => Ok(acquisitionCostsView(acquisitionCostsForm, Some(backLink)))
      }
    }

    (for {
      ownedBeforeTax <- sessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      didYouInheritThem <- sessionCacheService.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem)
      route <- routeRequest(acquisitionCostsBackLink(ownedBeforeTax.get, didYouInheritThem))
    } yield route).recoverToStart()

  }

  def submitAcquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[AcquisitionCostsModel], backLink: String) = {
      Future.successful(BadRequest(acquisitionCostsView(errors, Some(backLink))))
    }

    def successAction(success: AcquisitionCostsModel) = {
      for {
        save <- sessionCacheService.saveFormData(keystoreKeys.acquisitionCosts, success)
        answers <- sessionCacheService.getShareGainAnswers
        grossGain <- calcConnector.calculateRttShareGrossGain(answers)
      } yield grossGain match {
        case x if x > 0 => Redirect(routes.DeductionsController.lossesBroughtForward)
        case _ => Redirect(routes.ReviewAnswersController.reviewGainAnswers)
      }
    }

    def routeRequest(backLink: String): Future[Result] = {
      acquisitionCostsForm.bindFromRequest().fold(
        errors => errorAction(errors, backLink),
        success => successAction(success)
      )
    }

    (for {
      ownedBeforeTax <- sessionCacheService.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      didYouInheritThem <- sessionCacheService.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem)
      route <- routeRequest(acquisitionCostsBackLink(ownedBeforeTax.get, didYouInheritThem))
    } yield route).recoverToStart()
  }
}

