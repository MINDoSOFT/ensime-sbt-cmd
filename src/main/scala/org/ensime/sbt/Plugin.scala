package org.ensime.sbt
import sbt._
import Keys._
import util.{KeywordAtom, SExp}

object Plugin extends sbt.Plugin {

  override lazy val settings = Seq(
    Keys.commands += EnsimeCommand.ensime,
    EnsimeCommand.ensimeConfig := Map[KeywordAtom,SExp]()
  )

}
