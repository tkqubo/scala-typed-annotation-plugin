package com.github.tkqubo.typed_annotation

import java.net.URLClassLoader

import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification

import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.reporters.{Reporter, StoreReporter}
import scala.tools.nsc.util.ClassPath
import scala.tools.nsc.{Global, Settings}

class TypedAnnotationPluginSpec
  extends Specification {

  "TypedAnnotationPlugin" should {
    "pass with primitives" in {
      val code =
        s"""package com.github.tkqubo.typed_annotation
            |
            |import scala.annotation.StaticAnnotation
            |
            |@TypedAnnotation(classOf[Long])
            |class shouldBeOnLong extends StaticAnnotation
            |
            |object TypedAnnotationPluginTest {
            |  @shouldBeOnLong
            |  val name = "foo" // compilation error
            |
            |  @shouldBeOnLong
            |  def main(args: Array[String]): Unit = {
            |    val a = 1 / 0
            |  } // compilation error
            |
            |  @shouldBeOnLong
            |  val age = 33
            |
            |  @shouldBeOnLong
            |  val salary = 80000000L
            |}
          """.stripMargin
      val sources = List(new BatchSourceFile("<test>", code))
      val reporter: StoreReporter = new StoreReporter
      def equalTo(start: Int, end: Int, expectedType: String, actualType: String): Matcher[reporter.Info] = { (info: reporter.Info) =>
        info.pos.start === start
        info.pos.end === end
        info.msg === s"Not annotated correctly.  This definition is annotated as $expectedType but actually $actualType"
      }

      val compiler = createCompiler(reporter)
      new compiler.Run() compileSources sources

      reporter.infos.size === 2
      val infos: Seq[reporter.Info] = reporter.infos.toSeq.sortBy(_.pos.start)
      val fooDefinition: String = """val name = "foo"""
      infos(0) must equalTo(
        code.indexOf(fooDefinition),
        code.indexOf(fooDefinition) + 4,
        "Long",
        "String"
      )
      val mainDefinition: String = """main(args: Array[String]): Unit"""
      infos(1) must equalTo(
        code.indexOf(mainDefinition),
        code.indexOf(mainDefinition),
        "Long",
        "Unit"
      )
    }

    "pass with complex class hierarchy" in {
      val code =
        s"""package com.github.tkqubo.typed_annotation
            |
            |import scala.annotation.StaticAnnotation
            |
            |trait GrandParent
            |
            |trait Parent extends GrandParent
            |
            |trait Child extends Parent
            |
            |@TypedAnnotation(classOf[Parent])
            |case class parentNamed(value: String) extends StaticAnnotation
            |
            |@TypedAnnotation(classOf[Child])
            |case class childNamed(value: String) extends StaticAnnotation
            |
            |object TypedAnnotationPluginTest {
            |  @parentNamed("grand parent") // compilation error
            |  @childNamed("grand parent") // compilation error
            |  var grandParent: GrandParent = null
            |
            |  @parentNamed("parent")
            |  @childNamed("parent") // compilation error
            |  var parent: Parent = null
            |
            |  @parentNamed("child")
            |  @childNamed("child")
            |  var child: Child = null
            |}
          """.stripMargin
      val sources = List(new BatchSourceFile("<test>", code))
      val reporter: StoreReporter = new StoreReporter
      def equalTo(start: Int, end: Int, expectedType: String, actualType: String): Matcher[reporter.Info] = { (info: reporter.Info) =>
        info.pos.start === start
        info.pos.end === end
        info.msg === s"Not annotated correctly.  This definition is annotated as $expectedType but actually $actualType"
      }

      val compiler = createCompiler(reporter)
      new compiler.Run() compileSources sources

      reporter.infos.size === 3
      val infos: Seq[reporter.Info] = reporter.infos.toSeq.sortBy(_.pos.start)
      val grandParentDefinition: String = "var grandParent: GrandParent"
      infos(0) must equalTo(
        code.indexOf(grandParentDefinition),
        code.indexOf(grandParentDefinition) + 4,
        "com.github.tkqubo.typed_annotation.Parent",
        "com.github.tkqubo.typed_annotation.GrandParent"
      )
      infos(1) must equalTo(
        code.indexOf(grandParentDefinition),
        code.indexOf(grandParentDefinition) + 4,
        "com.github.tkqubo.typed_annotation.Child",
        "com.github.tkqubo.typed_annotation.GrandParent"
      )
      val parentDefinition: String = "var parent: Parent"
      infos(2) must equalTo(
        code.indexOf(parentDefinition),
        code.indexOf(parentDefinition) + 4,
        "com.github.tkqubo.typed_annotation.Child",
        "com.github.tkqubo.typed_annotation.Parent"
      )
    }
  }

  private def createCompiler(reporter: Reporter): Global = {
    val settings = new Settings
    val loader = getClass.getClassLoader.asInstanceOf[URLClassLoader]
    val entries = loader.getURLs.map(_.getPath)
    val scalaPath = entries
      .find(_.endsWith("scala-compiler.jar"))
      .map(_.replaceAll("scala-compiler.jar", "scala-library.jar"))
    settings.classpath.value = ClassPath.join(entries ++ scalaPath: _*)

    new Global(settings, reporter) {
      override protected def computeInternalPhases() {
        super.computeInternalPhases
        for (phase <- new TypedAnnotationPlugin(this).components)
          phasesSet += phase
      }
    }
  }
}
