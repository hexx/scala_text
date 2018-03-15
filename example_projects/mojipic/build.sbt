name := """mojipic"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val scalikejdbcVersion = "2.4.0"

libraryDependencies ++= Seq(
  cache,
  jdbc,
  evolutions,
  "org.twitter4j" % "twitter4j-core" % "4.0.4",
  "com.rabbitmq" % "amqp-client" % "3.6.1",
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-jsr310" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.5.1",
  "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
  "org.im4java" % "im4java" % "1.4.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.0" % "test"
)

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" :: 
  "-feature" :: 
  "-Xlint" :: 
  "-language:implicitConversions" :: 
  "-language:higherKinds" :: 
  "-language:existentials" ::
  "-Yno-adapted-args" ::
  Nil
)
