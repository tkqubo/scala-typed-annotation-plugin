package com.github.tkqubo.typed_annotation

import scala.annotation.StaticAnnotation

//noinspection ScalaStyle
@Typed(classOf[Long])
class shouldBeOnLong extends StaticAnnotation

object TypedAnnotationPluginTest {
  @shouldBeOnLong
  val name = "hoge"

  @shouldBeOnLong
  def main(args: Array[String]): Unit = {
    val a = 1 / 0
  }

  @shouldBeOnLong
  val age = 33

  @shouldBeOnLong
  val salary = 80000000L
}
