package com.github.tkqubo.typed_annotation

import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.{Global, Phase}


class TypedAnnotationPlugin(val global: Global) extends Plugin {
  selfPlugin =>
  import global._
  case class TypedAnnotatedValOrDefDef(valOrDefDef: ValOrDefDef, annotationInfo: AnnotationInfo, typedAnnotationInfo: AnnotationInfo)
  case class ValOrDefDefWithType(valOrDefDef: ValOrDefDef, annotationType: global.Type, annotatedType: global.Type)

  override val name = "typed-annotation"
  override val description = "type check against annotated member"
  override val components = List[PluginComponent](Component)

  private object Component extends PluginComponent {

    override val global: selfPlugin.global.type = selfPlugin.global
    override val runsAfter  = List("refchecks")
    override val runsBefore = List("")
    override val phaseName = selfPlugin.name

    override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
      override def name = selfPlugin.name
      override def apply(unit: CompilationUnit): Unit = {
        listTypes(listTypedAnnotatedValOrDefDef(unit))
          .filterNot { tuple => tuple.annotationType <:< tuple.annotatedType }
          .foreach { tuple =>
            reporter.error(tuple.valOrDefDef.pos, formatErrorMessage(tuple))
          }
      }
    }
  }

  private def formatErrorMessage(tuple: ValOrDefDefWithType): String =
    s"Not annotated correctly.  This definition is annotated as ${tuple.annotationType} but actually ${tuple.annotatedType}"

  private def listTypes(list: List[TypedAnnotatedValOrDefDef]): List[ValOrDefDefWithType] = {
    list
      .map { tuple =>
        val maybeAnnotationValue = tuple.typedAnnotationInfo
          .assocs
          .find(_._1.toString == "value")
          .collect { case (_, LiteralAnnotArg(Constant(value: NoArgsTypeRef))) => value }
        maybeAnnotationValue.map { (annotationValue: NoArgsTypeRef) =>
          ValOrDefDefWithType(tuple.valOrDefDef, annotationValue, tuple.valOrDefDef.tpt.tpe)
        }
      }
      .collect { case Some(x) => x }
  }

  private def listTypedAnnotatedValOrDefDef(unit: CompilationUnit): List[TypedAnnotatedValOrDefDef] = {
    unit.body
      .collect { case x: ValOrDefDef if x.symbol.annotations.nonEmpty => x }
      .flatMap { (valOrDefDef: global.ValOrDefDef) =>
        valOrDefDef.symbol
          .annotations
          .map { (info: global.AnnotationInfo) =>
            info.atp.typeSymbol
              .getAnnotation(symbolOf[TypedAnnotation])
              .map((typedInfo: global.AnnotationInfo) =>
                TypedAnnotatedValOrDefDef(valOrDefDef, info, typedInfo)
              )
          }
      }
      .collect { case Some(x) => x }
  }
}
