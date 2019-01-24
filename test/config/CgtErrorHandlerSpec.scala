/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.MustMatchers._
import play.api.Application
import play.api.http.Writeable
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results._
import play.api.mvc.{Action, Request, Result, Results}
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.http.ApplicationException
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class CgtErrorHandlerSpec extends UnitSpec with WithFakeApplication {

  def routeWithError[A](app: Application, request: Request[A])
                       (implicit writeable: Writeable[A]): Option[Future[Result]] = {
    route(app, request).map {
      _.recoverWith {
        case e =>
          app.errorHandler.onServerError(request, e)
      }
    }
  }

  val homeLink = controllers.routes.GainController.disposalDate().url

  val routerForTest: Router = {
    import play.api.routing.sird._

    Router.from {
      case GET(p"/ok") => Action.async { request =>
        Results.Ok("OK")
      }
      case GET(p"/application-exception") => Action.async { request =>
        throw new ApplicationException("", Redirect(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink)), "Test exception thrown")
      }
      case GET(p"/other-error") => Action.async { request =>
        throw new IllegalArgumentException("Other Exception Thrown")
      }
    }
  }

  implicit override lazy val fakeApplication = new GuiceApplicationBuilder().router(routerForTest).build()

  "Application returns OK for no exception" in {
    val request = FakeRequest("GET", "/ok")
    val response = routeWithError(fakeApplication, request).get
    status(response) must equal(OK)
  }

  "Application returns 303 and redirects user to start of journey for none.get, rather than technical difficulties" in {
    val request = FakeRequest("GET", "/application-exception")
    val response = routeWithError(fakeApplication, request).get
    status(response) must equal(SEE_OTHER)
    redirectLocation(response) shouldBe Some(controllers.utils.routes.TimeoutController.timeout(homeLink, homeLink).url)
  }

  "Application throws other exception and logs error" in {
    val request = FakeRequest("GET", "/other-error")
    val response = routeWithError(fakeApplication, request).get
    status(response) must equal(INTERNAL_SERVER_ERROR)
  }

  "Application returns 404 for non-existent endpoint" in {
    val request = FakeRequest("GET", "/non-existent-end-point")
    val response = routeWithError(fakeApplication, request).get
    status(response) shouldBe (NOT_FOUND)
  }
}
