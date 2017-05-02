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

package controllers

import common.Dates
import connectors.CalculatorConnector
import controllers.predicates.ValidActiveSession
import models.resident.shares.GainAnswersModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Call}
import views.html.calculation.checkYourAnswers.checkYourAnswers

import scala.concurrent.Future

object ReviewAnswersController extends ReviewAnswersController {
  val calculatorConnector = CalculatorConnector
}

trait ReviewAnswersController extends ValidActiveSession {


  private val dummyGainAnswers = GainAnswersModel(
    disposalDate = Dates.constructDate(10, 10, 2016),
    soldForLessThanWorth = false,
    disposalValue = Some(200000),
    worthWhenSoldForLess = None,
    disposalCosts = 0,
    ownerBeforeLegislationStart = false,
    valueBeforeLegislationStart = None,
    inheritedTheShares = Some(false),
    worthWhenInherited = None,
    acquisitionValue = Some(0),
    acquisitionCosts = 0)


  val calculatorConnector: CalculatorConnector

  val reviewGainAnswers: Action[AnyContent] = TODO

  val reviewDeductionsAnswers: Action[AnyContent] = TODO

  val reviewFinalAnswers: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(checkYourAnswers(Call("POST", "test-route"), "back-url", dummyGainAnswers)))
  }

}
