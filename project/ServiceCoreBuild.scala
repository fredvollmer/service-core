import sbt._
import sbt.Keys._

object ServiceCoreBuild extends Build {

  lazy val servicecore = Project(
    id = "service-core",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "service-core",
      organization := "io.torchbearer",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.11.8",
      javaOptions ++= Seq(
        "-Xdebug",
        "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
      ),
      libraryDependencies ++= Seq(
        "mysql" % "mysql-connector-java" % "5.1.3",
        "org.scalikejdbc" %% "scalikejdbc" % "2.4.2",
        "org.scalikejdbc" %% "scalikejdbc-config" % "2.4.2",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
        "com.amazonaws" % "aws-java-sdk-osgi" % "1.11.48" withSources(),
        "commons-codec" % "commons-codec" % "1.10",
        "org.json4s" %% "json4s-jackson" % "3.5.0"
      )
    )
  )
}
