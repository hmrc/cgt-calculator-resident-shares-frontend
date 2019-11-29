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

package controllers

import config.ApplicationConfig
import controllers.helpers.FakeRequestHelper
import org.scalatest.mockito.MockitoSugar
import play.api.http.FileMimeTypes
import play.api.i18n.{Lang, Langs, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext

class CgtLanguageControllerSpec extends UnitSpec with FakeRequestHelper with MockitoSugar with WithFakeApplication {

  val mockMCC: MessagesControllerComponents = new MessagesControllerComponents {

    private val mcc = fakeApplication.injector.instanceOf[MessagesControllerComponents]

    override def messagesActionBuilder: MessagesActionBuilder = mcc.messagesActionBuilder

    override def actionBuilder: ActionBuilder[Request, AnyContent] = mcc.actionBuilder

    override def parsers: PlayBodyParsers = mcc.parsers

    override def messagesApi: MessagesApi = mcc.messagesApi

    override def langs: Langs = new Langs {

      override def availables: Seq[Lang] = mcc.langs.availables

      override def preferred(candidates: Seq[Lang]): Lang = mcc.langs.availables.head
    }

    override def fileMimeTypes: FileMimeTypes = mcc.fileMimeTypes

    override def executionContext: ExecutionContext = mcc.executionContext
  }

  "The CgtLanguageController" when {

    "the language is switched to english" should {

      val mockConfig: ApplicationConfig = fakeApplication.injector.instanceOf[ApplicationConfig]
      val cgtLanguageController = new CgtLanguageController(mockMCC, mockConfig)

      "use the fallback URL when there's no referrer in the request header" in {

        val result = cgtLanguageController.switchToLanguage("english")(fakeRequest)
        status(result) shouldBe 303
      }
    }
  }

}
