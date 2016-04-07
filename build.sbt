organization := "io.corbel"

name := "corbel-sdk"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "com.pauldijou" %% "jwt-core" % "0.5.0",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "org.clapper" %% "grizzled-slf4j" % "1.0.2",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.7.0",
  // Test dependencies
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "org.mock-server" % "mockserver-netty" % "3.10.1" % "test" exclude("ch.qos.logback", "logback-classic")
)

//publish settings
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle := true

publishTo := {
  val artifactory = "http://artifacts.devialab.com/artifactory/"
  if (isSnapshot.value)
    Some("snapshots" at artifactory + "devialab-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
  else
    Some("releases"  at artifactory + "devialab-release-local")
}