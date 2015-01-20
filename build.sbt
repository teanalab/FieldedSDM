val anduin = "ru.ksu.niimm.cll" %% "anduin" % "0.3.1"
val lucene = "org.apache.lucene" % "lucene-core" % "4.8.1"
val luceneAnalyzers = "org.apache.lucene" % "lucene-analyzers-common" % "4.8.1"
val tools = "org.scala-tools.testing" %% "specs" % "1.6.9" % "test"
val junit = "junit" % "junit" % "4.8" % "test"

lazy val commonSettings = Seq(
  organization := "edu.wayne",
  version := "0.1.0",
  scalaVersion := "2.10.4"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "predicate-features",
    libraryDependencies += anduin,
    libraryDependencies += lucene,
    libraryDependencies += luceneAnalyzers,
    libraryDependencies += tools,
    libraryDependencies += junit
  )

assemblyMergeStrategy in assembly := {
  case s if s.endsWith(".class") => MergeStrategy.last
  case s if s.endsWith(".xsd") => MergeStrategy.last
  case s if s.endsWith(".dtd") => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
