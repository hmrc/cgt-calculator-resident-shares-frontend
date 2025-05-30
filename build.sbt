lazy val appName = "cgt-calculator-resident-shares-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings *)
  .settings(majorVersion := 1)
  .settings(PlayKeys.playDefaultPort := 9704)
  .settings(
    scalaVersion := "3.7.0",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test()
  )
  .settings(
    scalacOptions.+=("-Wconf:src=html/.*:s"), //suppresses warnings in twirl files and routes.
    scalacOptions.+=("-Wconf:src=routes/.*:s"), //these warnings are loud and inconsequential.
    scalacOptions += "-Wconf:msg=Flag.*repeatedly:s",

    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
    ),
    Test / testOptions -= Tests.Argument("-o", "-u", "target/test-reports", "-h", "target/test-reports/html-report"),
    // Suppress successful events in Scalatest in standard output (-o)
    // Options described here: https://www.scalatest.org/user_guide/using_scalatest_with_sbt
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u", "target/test-reports",
      "-h", "target/test-reports/html-report")
  )
