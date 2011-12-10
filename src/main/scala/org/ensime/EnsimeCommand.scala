package org.ensime

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
  
  def ensime = Command.args(ensimeCommand, ensimeBrief, ensimeDetailed, "huh?"){
    case (s,"dump"::projName::rest) =>  {
      
      val initX = Project extract s
      implicit val show = Project.showContextKey(s)

      val x:Extracted = if(projName != "root") {
	Extracted(initX.structure, initX.session, 
	  ProjectRef(new java.io.File("."), projName))
      }
      else initX

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

      val json = (
	("name" -> name) 
	~ ("org" -> org) 
	~ ("projectVersion" -> projectVersion) 
	~ ("buildScalaVersion" -> buildScalaVersion) 
	~ ("compileDeps" -> compileDeps) 
	~ ("testDeps" -> testDeps) 
	~ ("runtimeDeps" -> runtimeDeps) 
	~ ("sourceRoots" -> sourceRoots) 
	~ ("target" -> target))

      val result = compact(JsonAST.render(json))

      rest.headOption match{
	case Some(outputFile) => {
	  IO.write(new java.io.File(outputFile), result)
	  logger(s).info("Wrote dump to " + outputFile)
	}
	case None => {
	  logger(s).info(result) 
	}
      }

      s    
    }
    case (s,args) => {
      logger(s).info(ensimeBrief._1)
      s
    }
  }


}
