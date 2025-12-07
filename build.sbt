val Http4sVersion = "0.23.30"
val CirceVersion = "0.14.14"
val MunitVersion = "1.1.1"
val LogbackVersion = "1.5.18"
val MunitCatsEffectVersion = "2.1.0"
val DoobieVersion = "1.0.0-RC8"

lazy val root = (project in file("."))
  .settings(
    organization := "com.recipes",
    name := "recipes",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.7.4",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"      %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.xerial"       % "sqlite-jdbc"         % "3.34.0",
      "org.tpolecat"    %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"    %% "doobie-hikari"       % DoobieVersion,
      "org.tpolecat"    %% "doobie-specs2"       % DoobieVersion,
      "org.scalameta"   %% "munit"               % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect"   % MunitCatsEffectVersion % Test,
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion         % Runtime,
    ),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )
