name := "Hipscraper"

version := "0.0.1"

scalaVersion := "2.9.2"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % "2.9.0",
  "org.scalaj" %% "scalaj-time" % "0.6",
  "io.backchat.jerkson" % "jerkson_2.9.2" % "0.7.0",
  "commons-lang" % "commons-lang" % "2.6"
)
