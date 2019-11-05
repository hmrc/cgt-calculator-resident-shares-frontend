/*
 * Copyright 2016 HM Revenue & Customs
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

import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object FrontendBuild extends Build with MicroService {

  val appName = "cgt-calculator-resident-shares-frontend"

  override lazy val plugins: Seq[Plugins] = Seq(
    SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin
  )

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.39.0",
    "uk.gov.hmrc" %% "govuk-template" % "5.34.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.3.0-play-26",
    "uk.gov.hmrc" %% "play-partials" % "6.9.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client" % "8.3.0",
    "it.innove" % "play2-pdf" % "1.5.2",
    "uk.gov.hmrc" %% "play-language" % "3.4.0",
    "com.typesafe.play" %% "play-java" % "2.6.12",
    "com.typesafe.play" %% "play-json-joda" % "2.7.2",
    nettyServer

  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.9.0-play-26" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
        "org.mockito" % "mockito-core" % "3.1.0" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.11.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test()
}
