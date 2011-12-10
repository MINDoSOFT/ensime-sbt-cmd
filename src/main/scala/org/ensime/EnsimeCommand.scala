package org.ensime

object EnsimeCommand {
  import sbt._
  import Keys._
  import complete.Parser
  import complete.Parser._
  import complete.DefaultParsers.{some,success,token,Space,ID}
  import CommandSupport.logger
  
  val ensimeCommand = "ensime"
  val ensimeBrief = (ensimeCommand + " <action>", "Performs the ENSIME related action specified by <action>.")
  val ensimeDetailed = ensimeCommand + """ <action>
  If action is 'dump', print detailed metadata about the current project.
  """
  
  def ensime = Command.args(ensimeCommand, ensimeBrief, ensimeDetailed, "huh?"){
    (s,args:Seq[String]) =>
    val extracted = Project extract s
    import extracted.{currentRef, structure, session}
    logger(s).info("Args: " + args)
    s
  }

}
