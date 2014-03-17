name := "dtg"

version := "0.0.1"

scalaVersion := "2.10.2"

libraryDependencies ++= List(
  "com.typesafe.slick" %% "slick" % "2.0.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.h2database" % "h2" % "1.3.166",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.2"
  )