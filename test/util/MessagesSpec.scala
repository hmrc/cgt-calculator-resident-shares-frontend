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

package util

import play.api.i18n.Messages
import play.api.i18n.Messages.MessageSource
import uk.gov.hmrc.play.test.UnitSpec

import scala.io.Source

class MessagesSpec extends UnitSpec  {

  private val MatchSingleQuoteOnly = """\w+'{1}\w+""".r
  private val MatchBacktickQuoteOnly = """`+""".r
  private val MatchForwardTickQuoteOnly = """’+""".r

  private val englishMessages = parseMessages("conf/messages.en.en")
  private val welshMessages = parseMessages("conf/messages.en.cy")

  "All message files" should {
    "have the same set of keys" in {
      withClue(describeMismatch(englishMessages.keySet, welshMessages.keySet)) {
        welshMessages.keySet shouldBe englishMessages.keySet
      }
    }

    "have a non-empty message for each key" in {
      assertNonEmpty("English", englishMessages)
      assertNonEmpty("Welsh", welshMessages)
    }

    "have no unescaped single quotes in value" in {
      assertCorrectUseOfQuotes("English", englishMessages)
      assertCorrectUseOfQuotes("Welsh", welshMessages)
    }
    "have a resolvable message for keys which take args" in {
      countMessagesWithArgs(welshMessages).size shouldBe countMessagesWithArgs(englishMessages).size
    }
  }

  private def parseMessages(filename: String): Map[String, String] = {
    Messages.parse(new MessageSource {override def read: String = Source.fromFile(filename).mkString}, filename) match {
      case Right(messages) => messages
      case Left(e)         => throw e
    }
  }

  private def countMessagesWithArgs(messages: Map[String, String]) = messages.values.filter(_.contains("{0}"))

  private def assertNonEmpty(label: String, messages: Map[String, String]) = messages.foreach { case (key: String, value: String) =>
    withClue(s"In $label, there is an empty value for the key:[$key][$value]") {
      value.trim.isEmpty shouldBe false
    }
  }

  private def assertCorrectUseOfQuotes(label: String, messages: Map[String, String]) = messages.foreach { case (key: String, value: String) =>
    withClue(s"In $label, there is an unescaped or invalid quote:[$key][$value]") {
      MatchSingleQuoteOnly.findFirstIn(value).isDefined shouldBe false
      MatchBacktickQuoteOnly.findFirstIn(value).isDefined shouldBe false
      MatchForwardTickQuoteOnly.findFirstIn(value).isDefined shouldBe false
    }
  }

  private def listMissingMessageKeys(header: String, missingKeys: Set[String]) = {
    val displayLine = "\n" + ("@" * 42) + "\n"
    missingKeys.toList.sorted.mkString(header + displayLine, "\n", displayLine)
  }

  private def describeMismatch(englishKeySet: Set[String], welshKeySet: Set[String]) =
    if (englishKeySet.size > welshKeySet.size) listMissingMessageKeys("The following message keys are missing from the Welsh Set:", englishKeySet -- welshKeySet)
    else listMissingMessageKeys("The following message keys are missing from the English Set:", welshKeySet -- englishKeySet)
}