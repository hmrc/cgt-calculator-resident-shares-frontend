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

package config

import config.FrontendGlobal.{onBadRequest, onError, onHandlerNotFound}
import models.CGTClientException
import play.api.Logger
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.http.HttpErrorHandler
import play.api.http.Status._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.play.frontend.exceptions.ApplicationException

import scala.concurrent.Future

class CgtErrorHandler extends HttpErrorHandler {
  val homeLink = controllers.routes.GainController.disposalDate().url

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    statusCode match {
      case BAD_REQUEST => onBadRequest(request, message)
      case NOT_FOUND => onHandlerNotFound(request)
      case _ => onError(request, new CGTClientException(s"Client Error Occurred with Status $statusCode and message $message"))
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case ApplicationException(_, result, _) =>
        Logger.warn(s"Key-store None.get handled from: ${request.uri}")
        Future.successful(result.withHeaders(CACHE_CONTROL -> "no-cache,no-store,max-age=0"))
      case e => onError(request, e)
    }
  }
}
