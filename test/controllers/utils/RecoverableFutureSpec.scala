/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.{HttpEntity, Status}
import play.api.mvc.{AnyContent, Request, ResponseHeader, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.frontend.http.ApplicationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class RecoverableFutureSpec extends AnyWordSpec with ScalaFutures with Matchers with IntegrationPatience with Status {

  ".recoverToStart" should {

    "convert a `NoSuchElementException` into an `ApplicationException`" in {

      implicit val request: Request[AnyContent] = FakeRequest()

      val future: Future[Result] = Future.failed(new NoSuchElementException("test message")).recoverToStart()
      val url = controllers.utils.routes.TimeoutController.timeout.url

      whenReady(future.failed) {
        case ApplicationException(result, message) =>
          result.header.headers should contain("Location" -> url)
          result.header.status shouldBe SEE_OTHER
          message should equal("test message")
        case _ => None
      }
    }

    "not convert any other exception into an `ApplicationException`" in {

      implicit val request: Request[AnyContent] = FakeRequest()
      val ex = new IllegalArgumentException("test message")

      val future: Future[Result] = Future.failed(ex).recoverToStart()

      whenReady(future.failed) {
        _ shouldBe ex
      }
    }
  }

  val result: Result = Result(ResponseHeader.apply(OK, Map.empty), HttpEntity.NoEntity)
  val future: Future[Result] = Future.successful(result)
  val recoverableFuture = new RecoverableFuture(future)

  "RecoverableFuture's onComplete method" should {

    "be equivalent to Future's onComplete method" in {

      var completed = false
      recoverableFuture.onComplete {

        _ => completed = true
      }

      whenReady(recoverableFuture) { _ =>

        completed shouldBe true
      }
    }
  }

  "RecoverableFuture's isCompleted method" should {

    "be equivalent to Future's isCompleted method" in {

      whenReady(recoverableFuture) { _ =>
        recoverableFuture.isCompleted shouldBe true
      }
    }
  }

  "RecoverableFuture's value field" should {

    "be equivalent to Future's value field" in {

      recoverableFuture.value shouldBe future.value
    }
  }

  "RecoverableFuture's result method" should {

    "be equivalent to Future's result method" in {

      Await.result(recoverableFuture, 3.seconds) shouldBe Await.result(future, 3.seconds)
    }
  }

}
