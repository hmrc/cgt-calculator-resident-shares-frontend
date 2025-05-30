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

import common.{CommonPlaySpec, WithCommonFakeApplication}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results._
import play.api.mvc.{DefaultActionBuilder, Request, Result, Results}
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.Future


class CgtErrorHandlerSpec extends CommonPlaySpec with WithCommonFakeApplication {

  def routeWithError[A](app: Application, request: Request[A])
                       (implicit writeable: Writeable[A]): Option[Future[Result]] = {
    route(app, request).map {
      _.recoverWith {
        case e =>
          app.errorHandler.onServerError(request, e)
      }
    }
  }

  lazy val actionBuilder = fakeApplication.injector.instanceOf[DefaultActionBuilder]

  val routerForTest: Router = {
    import play.api.routing.sird._

    Router.from {
      case GET(p"/ok") => actionBuilder.async { _ =>
        Future.successful(Results.Ok("OK"))
      }
      case GET(p"/application-exception") => actionBuilder.async { _ =>
        throw new ApplicationException(Redirect(controllers.utils.routes.TimeoutController.timeout()), "Test exception thrown")
      }
      case GET(p"/other-error") => actionBuilder.async { _ =>
        throw new IllegalArgumentException("Other Exception Thrown")
      }
    }
  }

  implicit lazy val app: Application = new GuiceApplicationBuilder().router(routerForTest).build()

  "Application returns OK for no exception" in {
    val request = FakeRequest("GET", "/ok")
    val response = routeWithError(app, request).get
    status(response) should equal(OK)
  }

  "Application returns 303 and redirects user to start of journey for none.get, rather than technical difficulties" in {
    val request = FakeRequest("GET", "/application-exception")
    val response = routeWithError(app, request).get
    status(response) should equal(SEE_OTHER)
    redirectLocation(response) shouldBe Some(controllers.utils.routes.TimeoutController.timeout().url)
  }

  "Application throws other exception and logs error" in {
    val request = FakeRequest("GET", "/other-error")
    val response = routeWithError(app, request).get
    status(response) should equal(INTERNAL_SERVER_ERROR)
  }

  "Application returns 404 for non-existent endpoint" in {
    val request = FakeRequest("GET", "/non-existent-end-point")
    val response = routeWithError(app, request).get
    status(response) shouldBe (NOT_FOUND)
  }
}
