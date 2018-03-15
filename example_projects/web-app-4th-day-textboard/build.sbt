lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

val scalikejdbcVersion = "3.0.0"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  filters,
  "org.scalikejdbc"        %% "scalikejdbc"                  % scalikejdbcVersion,
  "org.scalikejdbc"        %% "scalikejdbc-config"           % scalikejdbcVersion,
  "org.scalikejdbc"        %% "scalikejdbc-play-initializer" % "2.5.1",
  "org.scalatestplus.play" %% "scalatestplus-play"           % "2.0.0" % Test
)
