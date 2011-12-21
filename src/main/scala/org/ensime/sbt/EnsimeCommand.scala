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
    case (s,"generate"::rest) =>  {

      def logInfo(message: String) {
	logger(s).info(message);
      }

      def logErrorAndFail(errorMessage: String): Nothing = {
	logger(s).error(errorMessage);
	throw new IllegalArgumentException()
      }

      logInfo("Gathering project information...")      

      val initX = Project extract s
      val buildStruct = initX.structure
      val session = initX.session

      val projs = initX.structure.allProjects.map{ 
	proj =>

	implicit val show:Show[ScopedKey[_]] = Project.showContextKey(s)

	val projRef = ProjectRef(proj.base, proj.id)
	val x = Extracted(buildStruct,session,projRef)

	logInfo("Extracting " + proj.id + "...")

	def evaluateTask[T](taskKey: sbt.Project.ScopedKey[sbt.Task[T]]):Option[(State, Result[T])] =
	EvaluateTask(buildStruct, taskKey, s, projRef)

	def optSetting[A](key: SettingKey[A]) = key in projRef get buildStruct.data
	
	def reqSetting[A](key: SettingKey[A], errorMessage: => String) = {
	  optSetting(key) getOrElse {
            logErrorAndFail(errorMessage)
	  }
	}

	def taskFiles(key:TaskKey[Classpath]):List[String] = {
	  val cp = evaluateTask(key) match {
            case Some((s,Value(deps))) => deps
            case _ => {
	      logger(s).error("Failed to obtain classpath for: " + key)
	      List()
	    }
	  }
	  cp.map{ att => (att.data).getAbsolutePath }.toList
	}

	def settingFiles[A](key: SettingKey[Seq[File]]):List[String] = {
	  optSetting(key).map(fs => fs.map(_.getAbsolutePath).toList).getOrElse(List[String]())
	}

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
	logger(s).info("  ensime-config := " + extras.toReadableString)

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
      logger(s).info("Wrote project to " + file)
      s
    }
    case (s,args) => {
      logger(s).info(ensimeBrief._1)
      s
    }
  }
}
