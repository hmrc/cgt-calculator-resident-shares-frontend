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

import java.time._

import common.Dates._
import common.KeystoreKeys
import config.ApplicationConfig
import connectors.SessionCacheConnector
import controllers.predicates.ValidActiveSession
import javax.inject.Inject
import models.resident.DisposalDateModel
import play.api.Application
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WhatNextSAController @Inject()(sessionCacheConnector: SessionCacheConnector,
                                     mcc: MessagesControllerComponents)
                                    (implicit val appConfig: ApplicationConfig, implicit val application: Application)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val backLink: String = controllers.routes.SaUserController.saUser().url
  lazy val iFormUrl: String = appConfig.residentIFormUrl

  def fetchAndParseDateToLocalDate()(implicit hc: HeaderCarrier): Future[LocalDate] = {
    sessionCacheConnector.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.ResidentShareKeys.disposalDate).map {
      data => LocalDate.of(data.get.year, data.get.month, data.get.day)
    }
  }

  val whatNextSAOverFourTimesAEA: Action[AnyContent] = ValidateSession.async { implicit request =>
    Future.successful(Ok(views.html.calculation.whatNext.whatNextSAFourTimesAEA(backLink)))
  }

  val whatNextSANoGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    fetchAndParseDateToLocalDate() map {
      date => Ok(views.html.calculation.whatNext.whatNextSANoGain(backLink, iFormUrl, taxYearOfDateLongHand(date)))
    }
  }

  val whatNextSAGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    fetchAndParseDateToLocalDate() map {
      date => Ok(views.html.calculation.whatNext.whatNextSAGain(backLink, iFormUrl, taxYearOfDateLongHand(date)))
    }
  }
}
