import sbt._

object Dependencies {

  lazy val auth0 = "com.auth0" % "java-jwt" % "3.16.0"

  private val circeVersion = "0.14.1"
  lazy val circeCore       = "io.circe" %% "circe-core"    % circeVersion
  lazy val circeGeneric    = "io.circe" %% "circe-generic" % circeVersion
  lazy val circeParser     = "io.circe" %% "circe-parser"  % circeVersion

  private val jettyVersion = "9.4.44.v20210927"
  lazy val jettyClient     = "org.eclipse.jetty" % "jetty-client"   % jettyVersion
  lazy val jettyProxy      = "org.eclipse.jetty" % "jetty-proxy"    % jettyVersion
  lazy val jettyServer     = "org.eclipse.jetty" % "jetty-server"   % jettyVersion
  lazy val jettyServlets   = "org.eclipse.jetty" % "jetty-servlets" % jettyVersion
  lazy val jettyUtil       = "org.eclipse.jetty" % "jetty-util"     % jettyVersion
  lazy val jettyWebapp     = "org.eclipse.jetty" % "jetty-webapp"   % jettyVersion

  lazy val jansi    = "org.fusesource.jansi"         % "jansi"           % "2.4.0"
  lazy val jasypt   = "org.jasypt"                   % "jasypt"          % "1.9.3"
  lazy val logback  = "ch.qos.logback"               % "logback-classic" % "1.3.0-alpha10"
  lazy val methanol = "com.github.mizosoft.methanol" % "methanol"        % "1.6.0"
  lazy val munit    = "org.scalameta"               %% "munit"           % "1.0.0-M1"
  lazy val picocli  = "info.picocli"                 % "picocli"         % "4.6.1"

  lazy val slf4jVersion = "2.0.0-alpha5"
  lazy val slf4jApi     = "org.slf4j" % "slf4j-api"    % slf4jVersion
  lazy val slf4jJul     = "org.slf4j" % "jul-to-slf4j" % slf4jVersion

  private val scalatraVersion = "2.8.2"
  lazy val scalatra           =
    ("org.scalatra" %% "scalatra" % scalatraVersion).cross(CrossVersion.for3Use2_13)
    
  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.1"
  lazy val zio            = "dev.zio"     %% "zio"    % "1.0.12"
}