import Dependencies._

import laika.ast
import laika.helium.config.HeliumIcon
import laika.helium.config.IconLink
import laika.theme.config.Color
import laika.helium.Helium

Docker / maintainer           := "Brian Schlining <brian@mbari.org>"
Docker / packageName          := "mbari/raziel"
Global / onChangedBuildSource := ReloadOnSourceChanges
// Laika / sourceDirectories     := Seq(baseDirectory.value / "src" / "docs")
Test / fork                   := true
ThisBuild / licenses          := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")))
ThisBuild / organization      := "org.mbari"
ThisBuild / organizationName  := "MBARI"
ThisBuild / scalaVersion      := "3.1.3"
ThisBuild / startYear         := Some(2021)
// ThisBuild / version           := "0.0.1"
ThisBuild / versionScheme     := Some("semver-spec")

lazy val root = project
  .in(file("."))
  .enablePlugins(
    AutomateHeaderPlugin, 
    DockerPlugin, 
    GitBranchPrompt, 
    GitVersioning, 
    JavaAppPackaging, 
    LaikaPlugin)
  .settings(
    name               := "raziel",
    dockerBaseImage    := "openjdk:17",
    dockerExposedPorts := Seq(8080),
    dockerUpdateLatest := true,
    javacOptions ++= Seq("-target", "17", "-source", "17"),
    // Set version based on git tag. I use "0.0.0" format (no leading "v", which is the default)
    // Use `show gitCurrentTags` in sbt to update/see the tags
    git.gitTagToVersionNumber := { tag: String =>
      if(tag matches "[0-9]+\\..*") Some(tag)
      else None
    },
    git.useGitDescribe := true,
    laikaTheme := Helium.defaults
      .site
      .topNavigationBar(
        navLinks = Seq(
          IconLink.external("https://github.com/mbari-org/raziel", HeliumIcon.github),
          IconLink.internal(ast.Path.Root / "api" / "index.html", HeliumIcon.api)
        )
      ).build, 
    laikaExtensions    := Seq(
      laika.markdown.github.GitHubFlavor,
      laika.parse.code.SyntaxHighlighting
    ),
    laikaIncludeAPI    := true,
    resolvers ++= Seq(
      Resolver.githubPackages("mbari-org", "maven")
    ),
    libraryDependencies ++= Seq(
      auth0,
      circeCore,
      circeGeneric,
      circeParser,
      jasypt,
      jansi   % Runtime,
      // jettyClient,
      // jettyProxy,
      // jettyServer,
      // jettyServlets,
      // jettyUtil,
      // jettyWebapp,
      logback % Runtime,
      methanol,
      munit   % Test,
      picocli,
      // scalatra,
      slf4jApi,
      tapirStubServer % Test,
      tapirSwagger,
      tapirCirce,
      tapirCirceClient % Test,
      tapirVertx,
      typesafeConfig,
      zio
    ),
    maintainer         := "brian@mbari.org",
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-encoding",
      "UTF-8",        // yes, this is 2 args. Specify character encoding used by source files.
      "-feature",     // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-indent",
      "-rewrite",
      "-unchecked"
    )
  )

// https://stackoverflow.com/questions/22772812/using-sbt-native-packager-how-can-i-simply-prepend-a-directory-to-my-bash-scrip
bashScriptExtraDefines ++= Seq(
  """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
  """addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml""""
)
batScriptExtraDefines ++= Seq(
  """call :add_java "-Dconfig.file=%APP_HOME%\conf\application.conf"""",
  """call :add_java "-Dlogback.configurationFile=%APP_HOME%\conf\logback.xml""""
)
