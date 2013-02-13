name := "crowd4s"

organization := "org.nisshiee"

version := "1.1.0-SNAPSHOT"

scalaVersion := "2.10.0"

libraryDependencies := Seq(
   "org.scalaz" %% "scalaz-core" % "7.0.0-M7"
  ,"net.databinder.dispatch" %% "dispatch-core" % "0.9.5"
  ,"org.json4s" %% "json4s-jackson" % "3.1.0"
  ,"com.typesafe" % "config" % "1.0.0"
  ,"org.specs2" %% "specs2" % "1.13" % "test"
  ,"org.mockito" % "mockito-all" % "1.9.0" % "test"
  ,"junit" % "junit" % "4.10" % "test"
  ,"org.pegdown" % "pegdown" % "1.1.0" % "test"
)

testOptions in (Test, test) += Tests.Argument("console", "html", "junitxml")

initialCommands := """
import scalaz._
import Scalaz._
import org.nisshiee.crowd4s._
"""

cleanupCommands := """
dispatch.Http.shutdown
"""

// ========== for sonatype oss publish ==========

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/nisshiee/crowd4s</url>
  <licenses>
    <license>
      <name>The MIT License (MIT)</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:nisshiee/crowd4s.git</url>
    <connection>scm:git:git@github.com:nisshiee/crowd4s.git</connection>
  </scm>
  <developers>
    <developer>
      <id>nisshiee</id>
      <name>Hirokazu Nishioka</name>
      <url>http://nisshiee.github.com/</url>
    </developer>
  </developers>)


// ========== for scaladoc ==========

// scaladocOptions in (Compile, doc) <++= (baseDirectory in LocalProject("core")).map {

scalacOptions in (Compile, doc) <++= baseDirectory.map {
  bd => Seq("-sourcepath", bd.getAbsolutePath,
            "-doc-source-url", "https://github.com/nisshiee/crowd4s/blob/master/core€{FILE_PATH}.scala")
}


