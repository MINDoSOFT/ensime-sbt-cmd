package org.ensime
import util._
import util.SExp._

object EnsimeCommand {
  import java.io._
  import sbt._
  import Keys._
  import complete.Parser
  import complete.Parser._
  import complete.DefaultParsers.{some,success,token,Space,ID}
  import CommandSupport.logger
  import net.liftweb.json.JsonAST
  import net.liftweb.json.Printer._
  import net.liftweb.json.JsonDSL._
  
  val ensimeCommand = "ensime"
  val ensimeBrief = (ensimeCommand + " dump <project> <outputFile>", 
    "Dump project for <project> information to <outputFile>.")
  val ensimeDetailed = ""

  //  def extracted(implicit state: State): Extracted = Project extract state
  //  def structure(implicit state: State): BuildStructure = extracted.structure
  //
  //  def setting[A](key: SettingKey[A],
  //    errorMessage: => String,
  //    reference: ProjectReference,
  //    configuration: Configuration = Configurations.Compile)(
  //    implicit state: State): List[String] = {
  //    key in (reference, configuration) get structure.data match {
  //      case Some(a) =>
  //      logDebug("Setting for key %s = %s".format(key.key, a))
  //      a.success
  //      case None => errorMessage.failNel
  //    }
  //  }
  //
  //    logger(state).debug(message)
  //  
  def ensime = Command.args(ensimeCommand, ensimeBrief, ensimeDetailed, "huh?"){
    case (s,"generate"::rest) =>  {

      logger(s).info("Gathering project information...")      

      val initX = Project extract s

      val projs = initX.structure.allProjects.map{ 
	proj =>

	implicit val show:Show[ScopedKey[_]] = Project.showContextKey(s)

	val x = Extracted(
	  initX.structure,
	  initX.session,
	  ProjectRef(proj.base, proj.id))

	def taskFiles(key:TaskKey[Classpath]):List[String] = {
	  val (newS,cp) = try{ x.runTask(key, s) }
	  catch{ case e => 
	    e.printStackTrace
	    (s,List())
	  }
	  cp.map{ a => (a.data).getAbsolutePath()}.toList
	}

	def settingFiles(key:SettingKey[Seq[java.io.File]]):List[String] = {
	  val r = x.get(key)
	  r.map{ f => f.getAbsolutePath()}.toList
	}

	val name = x.get(Keys.name)
	val org = x.get(organization)
	val projectVersion = x.get(version)
	val buildScalaVersion = x.get(scalaVersion)
	
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

	val targetPath = x.get(classDirectory in Compile).toString()
	val target = new java.io.File(targetPath).getCanonicalPath

	Map[KeywordAtom,SExp](
	  key(":name") -> name, 
	  key(":package") -> org, 
	  key(":version") -> projectVersion,
	  key(":compile-deps") -> SExp(compileDeps.map(SExp.apply)),
	  key(":runtime-deps") -> SExp(runtimeDeps.map(SExp.apply)),
	  key(":test-deps") -> SExp(testDeps.map(SExp.apply)),
	  key(":source-roots") -> SExp(sourceRoots.map(SExp.apply)),
	  key(":target") -> target)

      }

      val result = SExp(Map(
	key(":projects") -> SExp(projs.map{p => SExp(p)})
      )).toPPReadableString
      val file = rest.headOption.getOrElse(".ensime")
      IO.write(new java.io.File(file), result)
      logger(s).info("Wrote project to " + file)
      
      s    
    }
    case (s,args) => {
      logger(s).info(ensimeBrief._1)
      s
    }
  }
}
