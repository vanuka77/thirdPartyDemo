name := """mongodb_silhouette_example"""
organization := "com.sysgears"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
resolvers += Resolver.jcenterRepo
resolvers += "SonatlibraryDependencies += guice\nlibraryDependencies += \"org.scalatestplus.play\" %% \"scalatestplus-play\" % \"5.0.0\" % Testype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"


val playSilhouetteVersion = "7.0.0"
scalaVersion := "2.13.6"


libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % playSilhouetteVersion,
  "com.mohiva" %% "play-silhouette-totp" % playSilhouetteVersion,
  "com.iheart" %% "ficus" % "1.4.7",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  //  "org.reactivemongo" %% "play2-reactivemongo"                % "0.20.13-play28",
  //  "org.reactivemongo" %% "reactivemongo-play-json-compat"     % "1.0.1-play28",
  //  "org.reactivemongo" %% "reactivemongo-bson-compat"          % "0.20.13",
  "com.typesafe.play" %% "play-json-joda" % "2.9.2",
  "joda-time" % "joda-time" % "2.10.6",
  "org.reactivemongo" %% "play2-reactivemongo" % "1.0.1-play28",
  "com.typesafe.play" %% "play-json-joda" % "2.7.4",
  guice,
  filters,
  ehcache
)
