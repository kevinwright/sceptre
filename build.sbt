organization := "net.thecoda"

name := "avalon-sidekick"

version := "0.0.1-SNAPSHOT"

description := "A telnet proxy for avalon-rpg.  Able to support clients with extra goodies that the Game hides from you."

scalaVersion := "2.11.4"

libraryDependencies ++= {
  object V {
    lazy val akka = "2.3.8"
    lazy val akkaStreams = "1.0-M2"
    //lazy val jetty = "9.2.3.v20140905" //latest
    //lazy val jetty = "8.1.13.v20130916" //best for jetty
    lazy val unfiltered = "0.8.2"
    //lazy val jetty = "9.2.1.v20140609" //best for scalatra
    //lazy val scalatra = "2.3.0"
  }
  Seq(
    "com.typesafe.akka"      %% "akka-actor"               % V.akka,
    "com.typesafe.akka"      %% "akka-stream-experimental" % V.akkaStreams,
    //  "com.github.nscala-time" %% "nscala-time"             % "1.4.0",
    //  "nl.grons"               %% "metrics-scala"           % "3.3.0_a2.3",
    //  "org.neo4j"              %  "neo4j"                   % "2.1.5",
    //  "javax.servlet"          % "javax.servlet-api"        % "3.1.0",
    //  "org.eclipse.jetty" %  "jetty-server"            % jettyVersion,
    //  "org.eclipse.jetty" %  "jetty-servlet"           % jettyVersion,
    //  "org.eclipse.jetty" %  "jetty-webapp"            % jettyVersion,
    //  "org.scalatra.scalate"   %% "scalate-core"            % "1.7.0",
    //  "net.databinder"         %% "unfiltered"              % unfilteredVersion,
    //  "net.databinder"         %% "unfiltered-jetty"        % unfilteredVersion,
    //  "net.databinder"         %% "unfiltered-filter"       % unfilteredVersion,
    //  "net.databinder"         %% "unfiltered-filter-async" % unfilteredVersion,
    "junit"                  %  "junit"                   % "4.11"              % "test",
    "org.scalatest"          %% "scalatest"               % "2.2.0"             % "test",
    //  "net.databinder"         %% "unfiltered-scalatest"    % unfilteredVersion   % "test",
    "org.mockito"            %  "mockito-all"             % "1.9.5"             % "test",
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


