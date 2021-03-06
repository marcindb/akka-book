import sbt._
import sbt.Keys._

object AkkaBookBuild extends Build {

  lazy val akkaBook = Project(
    id = "akka-book",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Akka Book",
      organization := "zzz.akka",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.0",
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      libraryDependencies ++= Seq(
          "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
          "com.typesafe.akka" %% "akka-testkit" % "2.1.2",
          "com.typesafe.akka" %% "akka-actor" % "2.1.2")
    )
  )
}
