/**
*  Copyright (c) 2010, Aemon Cannon
*  All rights reserved.
*  
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of ENSIME nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*  
*  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
*  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL Aemon Cannon BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.ensime.sbt
import util._
import util.SExp._
import Plugin.Settings._

object EnsimeCommand {
  import java.io.{File => JavaFile}
  import sbt._
  import Keys._
  import CommandSupport.logger
  import net.liftweb.json.JsonAST
  import net.liftweb.json.Printer._
  import net.liftweb.json.JsonDSL._

  val ensimeCommand = "ensime"
  val ensimeBrief = (ensimeCommand + " dump <project> <outputFile>", 
    "Dump project for <project> information to <outputFile>.")
  val ensimeDetailed = ""

  def ensime = Command.args(ensimeCommand, ensimeBrief, ensimeDetailed, "huh?"){
    case (state,"generate"::rest) =>  {

      def logInfo(message: String) {
	logger(state).info(message);
      }

      def logErrorAndFail(errorMessage: String): Nothing = {
	logger(state).error(errorMessage);
	throw new IllegalArgumentException()
      }

      logInfo("Gathering project information...")      

      val initX = Project extract state

      val projs = initX.structure.allProjects.map{
	proj =>

	import Compat._

	implicit val s = state

	implicit val show:Show[ScopedKey[_]] = Project.showContextKey(s)

	implicit val projRef = ProjectRef(s.configuration.baseDirectory, proj.id)
	logInfo("Processing project: " + projRef + "...")

	implicit val x = Extracted(initX.structure, initX.session, projRef)
	implicit val buildStruct = x.structure
	val session = x.session


	val name = optSetting(Keys.name)
	val org = optSetting(organization)
	val projectVersion = optSetting(version)
	val buildScalaVersion = optSetting(scalaVersion)
	
	val compileDeps = (
       	  taskFiles(unmanagedClasspath in Compile) ++ 
       	  taskFiles(managedClasspath in Compile) ++ 
       	  taskFiles(internalDependencyClasspath in Compile)
	)
	val testDeps = (
       	  taskFiles(unmanagedClasspath in Test) ++
       	  taskFiles(managedClasspath in Test) ++ 
       	  taskFiles(internalDependencyClasspath in Test) ++ 
       	  taskFiles(exportedProducts in Test)
	)
	val runtimeDeps = (
       	  taskFiles(unmanagedClasspath in Runtime) ++
       	  taskFiles(managedClasspath in Runtime) ++
       	  taskFiles(internalDependencyClasspath in Runtime) ++ 
       	  taskFiles(exportedProducts in Runtime)
	)

	val sourceRoots =  (
       	  settingFiles(sourceDirectories in Compile) ++
       	  settingFiles(sourceDirectories in Test)
	)

	val target = optSetting(classDirectory in Compile).map(_.getCanonicalPath)

	val extras = optSetting(ensimeConfig).getOrElse(SExpList(List[SExp]()))
	logger(s).info(" User configuration = " + extras.toReadableString)

	extras.toKeywordMap ++ Map[KeywordAtom,SExp](
	  key(":name") -> name.map(SExp.apply).getOrElse(NilAtom()),
	  key(":package") -> org.map(SExp.apply).getOrElse(NilAtom()),
	  key(":version") -> projectVersion.map(SExp.apply).getOrElse(NilAtom()),
	  key(":compile-deps") -> SExp(compileDeps.map(SExp.apply)),
	  key(":runtime-deps") -> SExp(runtimeDeps.map(SExp.apply)),
	  key(":test-deps") -> SExp(testDeps.map(SExp.apply)),
	  key(":source-roots") -> SExp(sourceRoots.map(SExp.apply)),
	  key(":target") -> target.map(SExp.apply).getOrElse(NilAtom()))
      }

      val result = SExp(Map(
	  key(":subprojects") -> SExp(projs.map{p => SExp(p)})
	)).toPPReadableString

      val file = rest.headOption.getOrElse(".ensime")
      IO.write(new JavaFile(file), result)
      logger(state).info("Wrote configuration to " + file)
      state
    }
    case (state,args) => {
      logger(state).info(ensimeBrief._1)
      state
    }
  }
}
