package com.github.tkqubo.typed_annotation

import scala.annotation.StaticAnnotation

trait GrandParent

trait Parent extends GrandParent

trait Child extends Parent

@TypedAnnotation(classOf[Parent])
case class parentNamed(value: String) extends StaticAnnotation

@TypedAnnotation(classOf[Child])
case class childNamed(value: String) extends StaticAnnotation

//noinspection ScalaStyle
@TypedAnnotation(classOf[Long])
class shouldBeOnLong extends StaticAnnotation

object TypedAnnotationPluginTest {
  @shouldBeOnLong
  val name = "hoge" // compilation error

  @shouldBeOnLong
  def main(args: Array[String]): Unit = {
    val a = 1 / 0
  } // compilation error

  @shouldBeOnLong
  val age = 33

  @shouldBeOnLong
  val salary = 80000000L

  @parentNamed("grand parent")
  @childNamed("grand parent") // compilation error
  var grandParent: GrandParent = null

  @parentNamed("parent")
  @childNamed("parent") // compilation error
  var parent: Parent = null

  @parentNamed("child")
  @childNamed("child")
  var child: Child = null
}
