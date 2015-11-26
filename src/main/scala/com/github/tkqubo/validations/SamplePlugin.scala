package com.github.tkqubo.validations

import scala.tools.nsc.{ Global, Phase }
import scala.tools.nsc.plugins.{ Plugin, PluginComponent }


class SamplePlugin(val global: Global) extends Plugin {
  selfPlugin =>

  import global._

  override val name = "plugin-sample"
  //プラグインの名前
  override val description = "scala compiler plugin sample"
  //プラグインの説明。-Xplugin-listで見られる
  override val components = List[PluginComponent](Component) //プラグイン本体を返す

  private object Component extends PluginComponent {

    override val global: selfPlugin.global.type = selfPlugin.global
    override val runsAfter  = List("refchecks")                //どのフェーズの後に実行するかを指定する
    override val runsBefore = List("")                         //どのフェーズの前に実行するかを指定する
    override val phaseName = selfPlugin.name                   //フェーズ名。-Xshow-phasesで見られる

    override def newPhase(prev: Phase) = new StdPhase(prev) {
      override def name = selfPlugin.name
      override def apply(unit: CompilationUnit): Unit = {
        for {
          tree @ Apply(Select(rcvr, nme.DIV), List(Literal(Constant(0)))) <- unit.body
          if rcvr.tpe <:< definitions.IntClass.tpe
        } reporter.error(tree.pos, "definitely division by zero")
//        printf("unit.body=%s%n%s%n", unit.body.getClass, unit.body)
      }
    }
  }
}
