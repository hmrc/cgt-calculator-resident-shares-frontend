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

package controllers

import java.time.{LocalDate, ZoneId}
import java.util.UUID

import common.KeystoreKeys.{ResidentShareKeys => keystoreKeys}
import common.{Dates, TaxDates}
import config.ApplicationConfig
import connectors.{CalculatorConnector, SessionCacheConnector}
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
import javax.inject.Inject
import models.resident._
import models.resident.shares.OwnerBeforeLegislationStartModel
import models.resident.shares.gain.{DidYouInheritThemModel, ValueBeforeLegislationStartModel}
import play.api.Application
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import services.SessionCacheService
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.{calculation => commonViews}
import views.html.calculation.{gain => views}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class GainController @Inject()(calcConnector: CalculatorConnector,
                               sessionCacheService: SessionCacheService,
                               sessionCacheConnector: SessionCacheConnector,
                               mcc: MessagesControllerComponents)
                               (implicit val appConfig: ApplicationConfig,
                                implicit val application: Application)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  def navTitle(implicit request : Request[_]): String = Messages("calc.base.resident.shares.home")(mcc.messagesApi.preferred(request))
  override val homeLink = controllers.routes.GainController.disposalDate().url
  override val sessionTimeoutUrl = homeLink

  //################# Disposal Date Actions ####################
  def disposalDate: Action[AnyContent] = Action.async { implicit request =>
    if (request.session.get(SessionKeys.sessionId).isEmpty) {
      val sessionId = UUID.randomUUID.toString
      Future.successful(Ok(views.disposalDate(disposalDateForm(), homeLink)).withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    }
    else {
      sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate).map {
        case Some(data) => Ok(views.disposalDate(disposalDateForm().fill(data), homeLink))
        case None => Ok(views.disposalDate(disposalDateForm(), homeLink))
      }
    }
  }

  def submitDisposalDate: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(taxYearResult: Option[TaxYearModel]): Future[Result] = {
      if (taxYearResult.isDefined && !taxYearResult.get.isValidYear) Future.successful(Redirect(routes.GainController.outsideTaxYears()))
      else Future.successful(Redirect(routes.GainController.sellForLess()))
    }

    def bindForm(minimumDate: LocalDate) = {
      disposalDateForm(minimumDate.atStartOfDay(ZoneId.of("Europe/London"))).bindFromRequest.fold(
        errors => {
          Future.successful(
          BadRequest(
            views.disposalDate(errors.copy(errors = errors.errors.map { error =>
              if (error.key == "") error.copy(key = "disposalDateDay") else error
            }), homeLink)
          )
        )},
        success => {
          (for {
            save <- sessionCacheConnector.saveFormData(keystoreKeys.disposalDate, success)
            taxYearResult <- calcConnector.getTaxYear(s"${success.year}-${success.month}-${success.day}")
            route <- routeRequest(taxYearResult)
          } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
        }
      )
    }

    for {
      minimumDate <- calcConnector.getMinimumDate()
      result <- bindForm(minimumDate)
    } yield result
  }

  //################ Sell for Less Actions ######################
  private def sellForLessBackLink()(implicit hc: HeaderCarrier): Future[String] = {
    for {
      date <- sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${date.get.year}-${date.get.month}-${date.get.day}")
    } yield if (taxYear.get.isValidYear) routes.GainController.disposalDate().url else routes.GainController.outsideTaxYears().url
  }

  val sellForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(model: Option[SellForLessModel], backLink: String) = {
      val view = model match {
        case Some(data) => views.sellForLess(sellForLessForm.fill(data), homeLink, backLink)
        case None => views.sellForLess(sellForLessForm, homeLink, backLink)
      }

      Future.successful(Ok(view))
    }

    (for {
      model <- sessionCacheConnector.fetchAndGetFormData[SellForLessModel](keystoreKeys.sellForLess)
      backLink <- sellForLessBackLink()
      route <- routeRequest(model, backLink)
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)

  }

  def submitSellForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sellForLessForm.bindFromRequest.fold(
      errors => {
        (for {
          backLink <- sellForLessBackLink()
          response <- Future.successful(BadRequest(views.sellForLess(errors, homeLink, backLink)))
        } yield response).recoverToStart(homeLink, sessionTimeoutUrl)
      },
      success => {
        sessionCacheConnector.saveFormData[SellForLessModel](keystoreKeys.sellForLess, success).flatMap(
          _ =>success.sellForLess match {
            case true => Future.successful(Redirect(routes.GainController.worthWhenSoldForLess()))
            case _ => Future.successful(Redirect(routes.GainController.disposalValue()))
          }
        )
      }
    )
  }

  //################ Worth When Sold Actions ######################
  def worthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenSoldForLessModel](keystoreKeys.worthWhenSoldForLess).map {
      case Some(data) => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm.fill(data), homeLink))
      case _ => Ok(views.worthWhenSoldForLess(worthWhenSoldForLessForm, homeLink))
    }
  }

  def submitWorthWhenSoldForLess: Action[AnyContent] = ValidateSession.async { implicit request =>

    worthWhenSoldForLessForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenSoldForLess(errors, homeLink))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.worthWhenSoldForLess, success).flatMap(
          _=>Future.successful(Redirect(routes.GainController.disposalCosts()))
        )
      }
    )
  }


  //################ Outside Tax Years Actions ######################
  def outsideTaxYears: Action[AnyContent] = ValidateSession.async { implicit request =>
    (for {
      disposalDate <- sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](keystoreKeys.disposalDate)
      taxYear <- calcConnector.getTaxYear(s"${disposalDate.get.year}-${disposalDate.get.month}-${disposalDate.get.day}")
    } yield {
      Ok(commonViews.outsideTaxYear(
        taxYear = taxYear.get,
        isAfterApril15 = TaxDates.dateAfterStart(Dates.constructDate(disposalDate.get.day, disposalDate.get.month, disposalDate.get.year)),
        isProperty = false,
        navBackLink = routes.GainController.disposalDate().url,
        navHomeLink = homeLink,
        continueUrl = routes.GainController.sellForLess().url,
        navTitle = navTitle
      ))
    }).recoverToStart(homeLink, sessionTimeoutUrl)
  }

  //################ Disposal Value Actions ######################
  def disposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[DisposalValueModel](keystoreKeys.disposalValue).map {
      case Some(data) => Ok(views.disposalValue(disposalValueForm.fill(data), homeLink))
      case None => Ok(views.disposalValue(disposalValueForm, homeLink))
    }
  }

  def submitDisposalValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    disposalValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalValue(errors, homeLink))),
      success => {
        sessionCacheConnector.saveFormData[DisposalValueModel](keystoreKeys.disposalValue, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.disposalCosts()))
        )
      }
    )
  }

  //################# Disposal Costs Actions ########################
  def disposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[DisposalCostsModel](keystoreKeys.disposalCosts).map {
      case Some(data) => Ok(views.disposalCosts(disposalCostsForm.fill(data), homeLink))
      case None => Ok(views.disposalCosts(disposalCostsForm, homeLink))
    }
  }

  def submitDisposalCosts: Action[AnyContent] = ValidateSession.async { implicit request =>
    disposalCostsForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.disposalCosts(errors, homeLink))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.disposalCosts, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.ownerBeforeLegislationStart()))
        )
      }
    )
  }


  //################# Owned Before 1982 Actions ########################
  private val ownerBeforeLegislationStartBackLink = Some(controllers.routes.GainController.disposalCosts().url)

  def ownerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart).map {
      case Some(data) => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm.fill(data), homeLink, ownerBeforeLegislationStartBackLink))
      case None => Ok(views.ownerBeforeLegislationStart(ownerBeforeLegislationStartForm, homeLink, ownerBeforeLegislationStartBackLink))
    }
  }

  def submitOwnerBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    ownerBeforeLegislationStartForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.ownerBeforeLegislationStart(errors, homeLink, ownerBeforeLegislationStartBackLink))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.ownerBeforeLegislationStart, success).flatMap(
          _ => success.ownerBeforeLegislationStart match {
            case true => Future.successful(Redirect(routes.GainController.valueBeforeLegislationStart()))
            case _ => Future.successful(Redirect(routes.GainController.didYouInheritThem()))
          }
        )
      }
    )
  }

  //################# What were they worth on 31 March 1982 Actions ########################
  def valueBeforeLegislationStart: Action[AnyContent] =  ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart).map {
      case Some(data) => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm.fill(data)))
      case None => Ok(views.valueBeforeLegislationStart(valueBeforeLegislationStartForm))
    }
  }

  def submitValueBeforeLegislationStart: Action[AnyContent] = ValidateSession.async { implicit request =>
    valueBeforeLegislationStartForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.valueBeforeLegislationStart(errors))),
      success => {
        sessionCacheConnector.saveFormData[ValueBeforeLegislationStartModel](keystoreKeys.valueBeforeLegislationStart, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.acquisitionCosts()))
        )
      }
    )
  }

  //################# Did you Inherit the Shares Actions ########################
  def didYouInheritThem: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem).map {
      case Some(data) => Ok(views.didYouInheritThem(didYouInheritThemForm.fill(data)))
      case None => Ok(views.didYouInheritThem(didYouInheritThemForm))
    }
  }

  def submitDidYouInheritThem: Action[AnyContent] = ValidateSession.async { implicit request =>
    didYouInheritThemForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.didYouInheritThem(errors))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.didYouInheritThem, success).flatMap(
          _ => if (success.wereInherited) Future.successful(Redirect(routes.GainController.worthWhenInherited()))
          else Future.successful(Redirect(routes.GainController.acquisitionValue()))
        )
      }
    )
  }

  //################# Worth when Inherited Actions ########################
  def worthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[WorthWhenInheritedModel](keystoreKeys.worthWhenInherited).map {
      case Some(data) => Ok(views.worthWhenInherited(worthWhenInheritedForm.fill(data)))
      case None => Ok(views.worthWhenInherited(worthWhenInheritedForm))
    }
  }

  def submitWorthWhenInherited: Action[AnyContent] = ValidateSession.async { implicit request =>
    worthWhenInheritedForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.worthWhenInherited(errors))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.worthWhenInherited, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.acquisitionCosts()))
        )
      }
    )
  }

  //################# Acquisition Value Actions ########################
  def acquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    sessionCacheConnector.fetchAndGetFormData[AcquisitionValueModel](keystoreKeys.acquisitionValue).map {
      case Some(data) => Ok(views.acquisitionValue(acquisitionValueForm.fill(data), homeLink))
      case None => Ok(views.acquisitionValue(acquisitionValueForm, homeLink))
    }
  }

  def submitAcquisitionValue: Action[AnyContent] = ValidateSession.async { implicit request =>
    acquisitionValueForm.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.acquisitionValue(errors, homeLink))),
      success => {
        sessionCacheConnector.saveFormData(keystoreKeys.acquisitionValue, success).flatMap(
          _ => Future.successful(Redirect(routes.GainController.acquisitionCosts()))
        )
      }
    )
  }

  //################# Acquisition Costs Actions ########################
  private def acquisitionCostsBackLink: (OwnerBeforeLegislationStartModel, Option[DidYouInheritThemModel]) => String = {
    case (x,_) if x.ownerBeforeLegislationStart => routes.GainController.valueBeforeLegislationStart().url
    case (_,y) if y.get.wereInherited => routes.GainController.worthWhenInherited().url
    case (_,_) => routes.GainController.acquisitionValue().url
  }

  def acquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def routeRequest(backLink: String): Future[Result] = {
      sessionCacheConnector.fetchAndGetFormData[AcquisitionCostsModel](keystoreKeys.acquisitionCosts).map {
        case Some(data) => Ok(views.acquisitionCosts(acquisitionCostsForm.fill(data), Some(backLink), homeLink))
        case None => Ok(views.acquisitionCosts(acquisitionCostsForm, Some(backLink), homeLink))
      }
    }

    (for {
      ownedBeforeTax <- sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      didYouInheritThem <- sessionCacheConnector.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem)
      route <- routeRequest(acquisitionCostsBackLink(ownedBeforeTax.get, didYouInheritThem))
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)

  }

  def submitAcquisitionCosts: Action[AnyContent] = ValidateSession.async { implicit request =>

    def errorAction(errors: Form[AcquisitionCostsModel], backLink: String) = {
      Future.successful(BadRequest(views.acquisitionCosts(errors, Some(backLink), homeLink)))
    }

    def successAction(success: AcquisitionCostsModel) = {
      for {
        save <- sessionCacheConnector.saveFormData(keystoreKeys.acquisitionCosts, success)
        answers <- sessionCacheService.getShareGainAnswers
        grossGain <- calcConnector.calculateRttShareGrossGain(answers)
      } yield grossGain match {
        case x if x > 0 => Redirect(routes.DeductionsController.lossesBroughtForward())
        case _ => Redirect(routes.ReviewAnswersController.reviewGainAnswers())
      }
    }

    def routeRequest(backLink: String): Future[Result] = {
      acquisitionCostsForm.bindFromRequest.fold(
        errors => errorAction(errors, backLink),
        success => successAction(success)
      )
    }

    (for {
      ownedBeforeTax <- sessionCacheConnector.fetchAndGetFormData[OwnerBeforeLegislationStartModel](keystoreKeys.ownerBeforeLegislationStart)
      didYouInheritThem <- sessionCacheConnector.fetchAndGetFormData[DidYouInheritThemModel](keystoreKeys.didYouInheritThem)
      route <- routeRequest(acquisitionCostsBackLink(ownedBeforeTax.get, didYouInheritThem))
    } yield route).recoverToStart(homeLink, sessionTimeoutUrl)
  }
}

