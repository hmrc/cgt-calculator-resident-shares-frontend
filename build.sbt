import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

lazy val appName = "cgt-calculator-resident-shares-frontend"

lazy val appDependencies : Seq[ModuleID] = ???
lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.helpers.*;uk.gov.hmrc.BuildInfo;app.*;nr.*;res.*;prod.*;config.*;controllers.SessionCacheController;testOnlyDoNotUseInAppConf.*",
    ScoverageKeys.coverageMinimum := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins : _*)
  .settings(scoverageSettings : _*)
  .settings(majorVersion := 1)
  .settings(playSettings : _*)
  .settings(PlayKeys.playDefaultPort := 9704)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    targetJvm := "jvm-1.8",
    scalaVersion := "2.11.12",
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    pipelineStages in Assets := Seq(digest),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))
  .settings(integrationTestSettings())

fork in run := true
