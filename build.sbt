sbtPlugin := true

name := "ensime-sbt-cmd"

organization := "org.ensime"

version := "0.0.3"

libraryDependencies += "net.liftweb" %% "lift-json" % "2.4-M4"

scalacOptions := Seq("-deprecation", "-unchecked")

publishTo <<= version { (v: String) =>
  val nexus = "http://nexus.scala-tools.org/content/repositories/"
  if(v endsWith "-SNAPSHOT") Some("Scala Tools Nexus" at nexus + "snapshots/")
  else Some("Scala Tools Nexus" at nexus + "releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false

publishMavenStyle := true