
name := """thirdPartyDemo"""
organization := "com.sysgears"
herokuAppName in Compile := "agile-retreat-06891"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
resolvers += Resolver.jcenterRepo
resolvers += "SonatlibraryDependencies += guice\nlibraryDependencies += \"org.scalatestplus.play\" %% \"scalatestplus-play\" % \"5.0.0\" % Testype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

val playSilhouetteVersion = "7.0.0"
scalaVersion := "2.13.6"


libraryDependencies ++= Seq(
  "com.mohiva"        %% "play-silhouette"                 % playSilhouetteVersion,
  "com.mohiva"        %% "play-silhouette-password-bcrypt" % playSilhouetteVersion,
  "com.mohiva"        %% "play-silhouette-persistence"     % playSilhouetteVersion,
  "com.mohiva"        %% "play-silhouette-crypto-jca"      % playSilhouetteVersion,
  "com.mohiva"        %% "play-silhouette-totp"            % playSilhouetteVersion,
  "com.iheart"        %% "ficus"                           % "1.4.7",
  "net.codingwell"    %% "scala-guice"                     % "4.2.6",
  "com.typesafe.play" %% "play-json-joda"                  % "2.9.2",
  "joda-time"         % "joda-time"                        % "2.10.6",
  "org.reactivemongo" %% "play2-reactivemongo"             % "1.0.1-play28",
  "com.typesafe.play" %% "play-json-joda"                  % "2.7.4",
  "joda-time"         % "joda-time"                        % "2.10.10",
  "com.adrianhurt"    %% "play-bootstrap"                  % "1.6.1-P28-B4",
  "com.warrenstrange" % "googleauth"                       % "1.5.0",
  "com.google.zxing"  % "core"                             % "3.4.1",
  "com.google.zxing"  % "javase"                           % "3.4.1",
  guice,
  filters,
  ehcache,
  jdbc,
  ws
)
