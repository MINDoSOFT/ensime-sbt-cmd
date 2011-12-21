package org.ensime.sbt
import sbt._
import Keys._
import util.{SExpList}

object Plugin extends sbt.Plugin {

  override lazy val settings = Seq(
    Keys.commands += EnsimeCommand.ensime
  )

  object Settings{
    val ensimeConfig = SettingKey[SExpList]("ensime-config")
  }

}
