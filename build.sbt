organization := "net.thecoda"

name := "avalon-sidekick"

version := "0.0.1-SNAPSHOT"

description := "A telnet proxy for avalon-rpg.  Able to support clients with extra goodies that the Game hides from you."

scalaVersion := "2.11.4"

libraryDependencies ++= {
  object V {
    val akka = "2.3.8"
    val akkaStreams = "1.0-M2"
    //val jetty = "9.2.3.v20140905" //latest
    //val jetty = "8.1.13.v20130916" //best for jetty
    val unfiltered = "0.8.3"
    //val jetty = "9.2.1.v20140609" //best for scalatra
    //val scalatra = "2.3.0"
    val netty = "4.0.24.Final"
    val scalatest = "2.2.0"
    val mockito = "1.9.5"
    val rxscala = "0.23.0"
    val rxnetty = "0.4.4"
    val logback = "1.1.2"
    val slf4j = "1.7.10"
  }
  Seq(
        "com.typesafe.akka"      %% "akka-actor"               % V.akka,
    //  "io.reactivex"           %  "rxnetty"                  % V.rxnetty,
    //  "com.typesafe.akka"      %% "akka-stream-experimental" % V.akkaStreams,
    //  "com.github.nscala-time" %% "nscala-time"             % "1.4.0",
    //  "nl.grons"               %% "metrics-scala"           % "3.3.0_a2.3",
    //  "org.neo4j"              %  "neo4j"                   % "2.1.5",
    //  "javax.servlet"          % "javax.servlet-api"        % "3.1.0",
    //  "org.eclipse.jetty" %  "jetty-server"            % jettyVersion,
    //  "org.eclipse.jetty" %  "jetty-servlet"           % jettyVersion,
    //  "org.eclipse.jetty" %  "jetty-webapp"            % jettyVersion,
    //  "org.scalatra.scalate"   %% "scalate-core"            % "1.7.0",
    //  "io.netty"               %  "netty-all"               % V.netty,
    //  "net.databinder"         %% "unfiltered"              % V.unfiltered,
        "io.reactivex"           %% "rxscala"                 % V.rxscala,
    //  "net.databinder"         %% "unfiltered-jetty"        % V.unfiltered,
    //  "net.databinder"         %% "unfiltered-filter"       % V.unfiltered,
    //  "net.databinder"         %% "unfiltered-filter-async" % V.unfiltered,
        "ch.qos.logback"         %  "logback-core"            % V.logback,
        "ch.qos.logback"         %  "logback-classic"         % V.logback,
        "org.slf4j"              %  "slf4j-api"               % V.slf4j,
      //  "junit"                  %  "junit"                   % "4.11"         % "test",
        "org.scalatest"          %% "scalatest"               % V.scalatest    % "test",
        "net.databinder"         %% "unfiltered-scalatest"    % V.unfiltered   % "test",
        "org.mockito"            %  "mockito-all"             % V.mockito      % "test",
        "com.typesafe.akka"      %% "akka-testkit"            % V.akka         % "test"
  )
}

javacOptions ++= Seq("-Xmx512m", "-Xms128m", "-Xss10m")

javaOptions ++= Seq("-Xmx512m", "-Djava.awt.headless=true")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))


