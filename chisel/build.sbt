// See README.md for license details.

scalaVersion := "2.13.8"
val chiselVersion = "3.5.4"

lazy val root = (project in file("."))
  .settings(
    name := "pua-mips",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3"           % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest"        % "0.5.4" % "test",
      "org.scalameta"    % "semanticdb-scalac" % "4.7.7" cross CrossVersion.full,
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
      "-Yrangepos",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )
