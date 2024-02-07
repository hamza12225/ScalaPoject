ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaProgram",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "16.0.0-R25",
      "org.openjfx" % "javafx-controls" % "17.0.1",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.0",
      "org.mongodb.scala" %% "mongo-scala-bson" % "4.3.0",
      "org.slf4j" % "slf4j-api" % "1.7.32",
      "ch.qos.logback" % "logback-classic" % "1.2.6
)
  )

