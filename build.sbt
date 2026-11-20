import Dependencies._


Docker / maintainer           := "Brian Schlining <brian@mbari.org>"
Docker / packageName          := "mbari/raziel"
onChangedBuildSource := ReloadOnSourceChanges

Compile / doc / scalacOptions ++= Seq(
    "-groups",
    "-project-footer", "Monterey Bay Aquarium Research Institute",
    "-siteroot", "src/docs",
    "-doc-root-content", "./src/docs/index.md"
    )
Test / fork                   := true
Test / testOptions ++= Seq(
    Tests.Argument(TestFrameworks.MUnit, "-b"),
    Tests.Argument(TestFrameworks.MUnit, "--log=debug")
)
licenses          := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")))
organization      := "org.mbari"
organizationName  := "MBARI"
scalaVersion      := "3.10.1"
startYear         := Some(2021)
// ThisBuild / version           := "0.0.1"
versionScheme     := Some("semver-spec")

lazy val root = project
  .in(file("."))
  .enablePlugins(
    AutomateHeaderPlugin,
    DockerPlugin,
    GitBranchPrompt,
    GitVersioning,
    JavaAppPackaging)
  .settings(
    name               := "raziel",
    dockerBaseImage    := "eclipse-temurin:27",
    dockerExposedPorts := Seq(8080),
    dockerUpdateLatest := true,
    javacOptions ++= Seq("-target", "27", "-source", "27"),
    // Set version based on git tag. I use "0.0.0" format (no leading "v", which is the default)
    // Use `show gitCurrentTags` in sbt to update/see the tags
    git.gitTagToVersionNumber := { (gitTag: String) =>
          if (gitTag.matches("[0-9]+\\..*")) Some(gitTag)
          else None
      },
    git.useGitDescribe := true,
    // sbt 2.x moves `target` under target/out/jvm/scala-<ver>/<project>. Pin the staging
    // directory to its sbt 1.x location so the Dockerfile's COPY target/universal/stage still works.
    // Universal / com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stagingDirectory := baseDirectory.value / "target" / "universal" / "stage",
    libraryDependencies ++= Seq(
      auth0,
      circeCore,
      circeGeneric,
      circeParser,
      jansi   % Runtime,
      logback % Runtime,
      methanol,
      munit   % Test,
      picocli,
      slf4jApi,
      tapirStubServer % Test,
      tapirSwagger,
      tapirCirce,
      tapirCirceClient % Test,
      tapirVertx,
      typesafeConfig
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
