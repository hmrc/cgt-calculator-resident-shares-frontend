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

import config.{AppConfig, ApplicationConfig}
import controllers.predicates.ValidActiveSession
import play.api.mvc.{Action, AnyContent}
import views.html.calculation.{whatNext => views}

import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object WhatNextNonSaController extends WhatNextNonSaController {
  override val applicationConfig: AppConfig = ApplicationConfig
}

trait WhatNextNonSaController extends ValidActiveSession {

  val applicationConfig: AppConfig

  val whatNextNonSaGain: Action[AnyContent] = ValidateSession.async { implicit request =>
    Future.successful(Ok(views.whatNextNonSaGain(applicationConfig.residentIFormUrl)))
  }

  val whatNextNonSaLoss: Action[AnyContent] = ValidateSession.async { implicit request =>
    Future.successful(Ok(views.whatNextNonSaLoss(applicationConfig.residentIFormUrl)))
  }
}
