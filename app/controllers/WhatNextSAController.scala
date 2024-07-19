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

package controllers

import common.Dates._
import common.KeystoreKeys
import config.AppConfig
import controllers.predicates.ValidActiveSession
import controllers.utils.RecoverableFuture
import models.resident.DisposalDateModel
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.calculation.whatNext.{whatNextSAFourTimesAEA, whatNextSAGain, whatNextSANoGain}

import java.time._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatNextSAController @Inject()(sessionCacheService: SessionCacheService,
                                     mcc: MessagesControllerComponents,
                                     appConfig: AppConfig,
                                     whatNextSAFourTimesAEAView: whatNextSAFourTimesAEA,
                                     whatNextSAGainView: whatNextSAGain,
                                     whatNextSANoGainView: whatNextSANoGain)
                                    (implicit ec: ExecutionContext)
  extends FrontendController(mcc) with ValidActiveSession with I18nSupport {

  val backLink: String = controllers.routes.SaUserController.saUser.url
  lazy val iFormUrl: String = appConfig.residentIFormUrl

  private def getDisposalDate(implicit request: Request[_]) =
    sessionCacheService.fetchAndGetFormData[DisposalDateModel](KeystoreKeys.ResidentShareKeys.disposalDate) map(
      _.get match { case DisposalDateModel(day, month, year) => taxYearOfDateLongHand(LocalDate.of(year, month, day)) }
    )

  def whatNextSAOverFourTimesAEA: Action[AnyContent] = ValidateSession.async { implicit request =>
    Future.successful(Ok(whatNextSAFourTimesAEAView(backLink)))
  }

  def whatNextSANoGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    getDisposalDate.map(date => Ok(whatNextSANoGainView(backLink, iFormUrl, date))).recoverToStart()
  }

  def whatNextSAGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    getDisposalDate.map(date => Ok(whatNextSAGainView(backLink, iFormUrl, date))).recoverToStart()
  }
}
