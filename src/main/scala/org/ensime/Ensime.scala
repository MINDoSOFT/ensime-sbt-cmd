package org.ensime

import sbt._
import Keys._
import sbt.Project.{ScopedKey, compiled}
import sbt.Load.BuildStructure

// see http://harrah.github.com/xsbt/latest/sxr/Project.scala.html

object Ensime {

  def apply(structure: BuildStructure, build: URI, scoped: ScopedKey[_],
    generation: Int)(implicit display: Show[ScopedKey[_]]): ProjectInfo = {

    /*    val key = scoped.key
    val scope = scoped.scope
    
    lazy val clazz = key.manifest.erasure
    lazy val firstType = key.manifest.typeArguments.head
    val (typeName: String, value: Option[_]) =
    structure.data.get(scope, key) match {
    case None => ("", None)
    case Some(v) =>
    if(clazz == classOf[Task[_]]) ("Task[" + firstType.toString + "]", None)
    else if(clazz == classOf[InputTask[_]]) ("InputTask[" + firstType.toString + "]", None)
    else (key.manifest.toString, Some(v))
    }
    
    val description = key.description getOrElse{""}
    val definedIn = structure.data.definingScope(scope, key) match {
    case Some(sc) => display(ScopedKey(sc, key))
    case None => ""
    }
    val cMap = flattenLocals(compiled(structure.settings, false)(structure.delegates, structure.scopeLocal, display))
    // val related = cMap.keys.filter(k => k.key == key && k.scope != scope)
    val depends = cMap.get(scoped) match { case Some(c) => c.dependencies.toSet; case None => Set.empty }
    // val reverse = reverseDependencies(cMap, scoped)
    */
    ProjectInfo("dude")

  }
}

case class ProjectInfo(name: String){
  def toJson: String = {
    "{}"
  }
}
