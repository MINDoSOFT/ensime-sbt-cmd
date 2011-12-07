package org.ensime

object EnsimeCommand {
  import sbt._
  import Keys._
  import complete.Parser
  import complete.DefaultParsers._
  import CommandSupport.logger
  
  val ensimeCommand = "ensime"
  val ensimeBrief = (ensimeCommand + " <action>", "Performs the ENSIME related action specified by <action>.")
  val ensimeDetailed = ensimeCommand + """ <action>
	If action is 'dump', print detailed metadata about the current project.
"""
  
  def ensime = Command(ensimeCommand, ensimeBrief, ensimeDetailed)(ensimeParser) { case (s, (sk)) =>
    // see http://harrah.github.com/xsbt/latest/sxr/Main.scala.html
    implicit val show = Project.showContextKey(s)
    val json = Ensime(Project.structure(s), Project.session(s).current.build, sk, 0).toJson
    logger(s).info(json)
    s
  }
  
  lazy val ensimeParser = (s: State) => BuiltinCommands.spacedKeyParser(s)
}
