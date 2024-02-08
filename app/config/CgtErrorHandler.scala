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

package config

import play.api.Logging
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.MessagesApi
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.{ApplicationException, FrontendErrorHandler}
import views.html.error_template

import javax.inject.Inject
import scala.concurrent.Future


class CgtErrorHandler @Inject()(val messagesApi: MessagesApi,
                                errorTemplateView: error_template)
  extends FrontendErrorHandler with Logging {

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit req: Request[_]): Html = {
    errorTemplateView(pageTitle, heading, message)
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case ApplicationException(result, _) =>
        logger.warn(s"Key-store None.get handled from: ${request.uri}")
        Future.successful(result.withHeaders(CACHE_CONTROL -> "no-cache,no-store,max-age=0"))
      case _ => super.onServerError(request, exception)
    }
  }
}
