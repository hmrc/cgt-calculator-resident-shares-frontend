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

package common

import org.apache.pekko.stream.Materializer
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.Helpers.running
import play.api.{Application, Play}

import scala.concurrent.ExecutionContext

trait WithCommonFakeApplication extends BeforeAndAfterAll {
  this: Suite =>

  lazy val fakeApplication: Application = new GuiceApplicationBuilder().bindings(bindModules:_*).build()

  implicit val ec: ExecutionContext = fakeApplication.injector.instanceOf[ExecutionContext]
  implicit lazy val materializer = mock[Materializer]

  def bindModules: Seq[GuiceableModule] = Seq()

  override def beforeAll(): Unit = {
    super.beforeAll()
    Play.start(fakeApplication)
  }

  override def afterAll(): Unit =  {
    super.afterAll()
    Play.stop(fakeApplication)
  }

  def evaluateUsingPlay[T](block: => T): T = {
    running(fakeApplication) {
      block
    }
  }

} 