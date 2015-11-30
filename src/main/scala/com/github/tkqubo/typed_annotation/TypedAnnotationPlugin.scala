package com.github.tkqubo.typed_annotation

import scala.tools.nsc.plugins.{Plugin, PluginComponent}
import scala.tools.nsc.{Global, Phase}


class TypedAnnotationPlugin(val global: Global) extends Plugin {
  selfPlugin =>
  import global._

  override val name = "typed-annotation"
  override val description = "type check against annotated member"
  override val components = List[PluginComponent](Component)

  private object Component extends PluginComponent {

    override val global: selfPlugin.global.type = selfPlugin.global
    override val runsAfter  = List("parser")
    override val runsBefore = List("")
    override val phaseName = selfPlugin.name

    override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
      override def name = selfPlugin.name
      override def apply(unit: CompilationUnit): Unit = {
        val annotationDefinitions: List[ClassDef] =
          unit.body.collect { case x @ ClassDef(_, _, _, Template(types, _, _)) if types.exists(extendsAnnotation(_: Tree, "StaticAnnotation")) => x }

        val typedAnnotationDefinitions: List[ClassDef] =
          annotationDefinitions.filter(_.symbol.hasAnnotation(symbolOf[TypedAnnotation]))

        val annotatedValOrDefDefs = unit.body
          .collect { case x: ValOrDefDef if typedAnnotationDefinitions.exists(classDef => x.symbol.hasAnnotation(classDef.symbol)) => x }

        annotatedValOrDefDefs.foreach(validateValOrDefDef(_, typedAnnotationDefinitions))
      }
    }
  }

  private def validateValOrDefDef(valOrDefDef: ValOrDefDef, typedAnnotationDefinitions: List[ClassDef]): Unit = {
    val applicableDefinitions: List[AnnotationInfo] = typedAnnotationDefinitions
      .map(definition => valOrDefDef.symbol.getAnnotation(definition.symbol))
      .collect(collectSome)

    val typedAnnotations: List[AnnotationInfo] =
      applicableDefinitions
        .map(_.symbol.getAnnotation(symbolOf[TypedAnnotation]))
        .collect(collectSome)

    val expectedTypes: List[NoArgsTypeRef] =
      typedAnnotations.map(getTypedType)

    expectedTypes
      .filterNot(valOrDefDef.tpt.tpe.weak_<:<(_))
      .foreach { typ =>
        val message = formatErrorMessage(typ, valOrDefDef.tpt.tpe)
        reporter.error(valOrDefDef.pos, message)
      }
  }

  private def collectSome[T]: PartialFunction[Option[T], T] = { case Some(x) => x }

  private def formatErrorMessage(annotationType: global.Type, annotatedType: global.Type): String =
    s"Not annotated correctly.  This definition is annotated as $annotationType but actually $annotatedType"

  private def getTypedType(info: AnnotationInfo): NoArgsTypeRef =
    info.assocs
      .find(_._1.toString == "value")
      .collect { case (_, LiteralAnnotArg(Constant(value: NoArgsTypeRef))) => value }
      .get

  private def extendsAnnotation(tree: Tree, targetName: String): Boolean = tree match {
    case tt: TypeTree =>
      tt.original match {
        case Select(_, className) => className.toString == targetName
        case _ => false
      }
    case _ => false
  }
}
