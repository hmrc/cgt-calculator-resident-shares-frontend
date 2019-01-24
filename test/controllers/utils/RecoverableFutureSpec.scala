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

package controllers.utils

import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.http.Status
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.http.ApplicationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecoverableFutureSpec extends WordSpec with ScalaFutures with Matchers with IntegrationPatience with Status {

  ".recoverToStart" should {
    "convert a `NoSuchElementException` into an `ApplicationException`" in {

      implicit val request: Request[AnyContent] = FakeRequest()
      val homeLink = controllers.routes.GainController.disposalDate().url
      val sessionTimeoutUrl = homeLink

      val future: Future[Result] = Future.failed(new NoSuchElementException("test message")).recoverToStart(homeLink, sessionTimeoutUrl)
      val url = controllers.utils.routes.TimeoutController.timeout(homeLink, sessionTimeoutUrl).url

      whenReady(future.failed) {
        case ApplicationException(appName, result, message) =>
          appName should equal("cgt-calculator-resident-shares-frontend")
          result.header.headers should contain("Location" -> url)
          result.header.status shouldBe SEE_OTHER
          message should equal("test message")
      }
    }

    "not convert any other exception into an `ApplicationException`" in {

      implicit val request: Request[AnyContent] = FakeRequest()
      val homeLink = controllers.routes.GainController.disposalDate().url
      val sessionTimeoutUrl = homeLink
      val ex = new IllegalArgumentException("test message")

      val future: Future[Result] = Future.failed(ex).recoverToStart(homeLink, sessionTimeoutUrl)

      whenReady(future.failed) {
        _ shouldBe ex
      }
    }
  }
}
