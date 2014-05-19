name := "articles"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.reactivemongo" % "play2-reactivemongo_2.10" % "0.10.2"
)     

play.Project.playScalaSettings

scalariformSettings
