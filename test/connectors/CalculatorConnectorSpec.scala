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

package connectors

import java.time.LocalDate
import java.util.UUID

import common.KeystoreKeys
import models.resident
import models.resident.IncomeAnswersModel
import models.resident.shares.{DeductionGainAnswersModel, GainAnswersModel}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.http.logging.SessionId

class CalculatorConnectorSpec extends UnitSpec with MockitoSugar {

  val mockHttp = mock[HttpGet]
  val mockSessionCacheConnector = mock[SessionCacheConnector]
  val sessionId = UUID.randomUUID.toString

  object TargetCalculatorConnector extends CalculatorConnector {
    override val sessionCacheConnector = mockSessionCacheConnector
    override val http = mockHttp
    override val serviceUrl = "dummy"
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

    "Calling .getMinimumDate" should {
      def mockDate(result: Future[DateTime]): OngoingStubbing[Future[DateTime]] =
          when(mockHttp.GET[DateTime](ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(result)

        "return a DateTime which matches the returned LocalDate" in {
          mockDate(Future.successful(DateTime.parse("2015-06-04")))
          await(TargetCalculatorConnector.getMinimumDate()) shouldBe LocalDate.parse("2015-06-04")
        }

        "return a failure if one occurs" in {
          mockDate(Future.failed(new Exception("error message")))
          the[Exception] thrownBy await(TargetCalculatorConnector.getMinimumDate()) should have message "error message"
        }
    }
}
