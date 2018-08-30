/*
 * Copyright 2018 HM Revenue & Customs
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

import controllers.predicates.ValidActiveSession
import play.api.mvc.{Action, AnyContent}
import common.Dates._
import java.time._

import common.KeystoreKeys
import config.{AppConfig, ApplicationConfig}
import connectors.{CalculatorConnector, SessionCacheConnector}
import models.resident.DisposalDateModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object WhatNextSAController extends WhatNextSAController {
  override lazy val sessionCacheConnector = SessionCacheConnector
  override lazy val appConfig = ApplicationConfig
}

trait WhatNextSAController extends ValidActiveSession {

  val sessionCacheConnector: SessionCacheConnector
  val appConfig: AppConfig

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
