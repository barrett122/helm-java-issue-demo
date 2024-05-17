ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.12.18"


lazy val root = (project in file("."))
  .settings(
    name := "helm-java-issue-demo",
    assembly / mainClass := Some("example.Application"),
  )

libraryDependencies ++= Seq(
  "com.marcnuri.helm-java" % "helm-java" % "0.0.7" notTransitive(),
  "com.marcnuri.helm-java" % "lib-api" % "0.0.7" ,
  "com.marcnuri.helm-java" % "linux-amd64" % "0.0.7",
  "tools.profiler" % "async-profiler" % "3.0",
)

assemblyMergeStrategy in assembly := {
  case x if x.endsWith("module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
