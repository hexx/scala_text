lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test
)
