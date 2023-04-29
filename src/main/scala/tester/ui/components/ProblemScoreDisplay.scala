package tester.ui.components

import otsbridge.ProblemScore
import otsbridge.ProblemScore.ProblemScore

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import typings.antd.antdStrings
import typings.antd.components.Progress
import typings.antd.libProgressProgressMod.ProgressSize
import typings.react.mod.CSSProperties

@react object ProblemScoreDisplay {

  def problemScoreElement(text: String, pct: Double, color: String) =
    Progress()
      .`type`(antdStrings.line)
      .percent(pct * 100)
      //      .size(scalajs.js.Tuple2(4d, 100d).asInstanceOf[ProgressSize])
      .format((_, _) => div(b(style := js.Dynamic.literal(color = color))(text)))
      .strokeColor(color)


  case class Props(ps: ProblemScore, hasAnswers: Boolean, haveWaitingConfirmAnswer: Boolean)

  def acceptNotAcceptText(passed: Boolean) =
    if (passed) b(style := js.Dynamic.literal(color = "green"))("Зачтено")
    else b(style := js.Dynamic.literal(color = "red"))("Не зачтено")

  val component = FunctionalComponent[Props] { props =>
    //    val text = props.ps match {
    //      case ProblemScore.BinaryScore(passed) => if(passed) "Зачтено" else "Не зачтено"
    //      case _ => props.ps.toPrettyString
    //    }
    //
    div(style := js.Dynamic.literal(
      display = "flex",
      justifyContent = "center",
//      width = "90%"
    ))(if (props.ps.isMax) {
      props.ps match {
        case ProblemScore.BinaryScore(passed) => acceptNotAcceptText(passed)
        case _ => problemScoreElement(props.ps.toPrettyString, props.ps.percentage, "green")
      }
    } else if (props.haveWaitingConfirmAnswer) {
      div(style := js.Dynamic.literal(
        color = "yellow"
      ))(b("Ожидает подтверждаения преподавателем"))
    } else if (props.ps.toInt == 0 && !props.hasAnswers) {
      div(style := js.Dynamic.literal(
        color = "red"
      ))(b("Нет ответа"))
    } else {
      props.ps match {
        case ProblemScore.BinaryScore(passed) => acceptNotAcceptText(passed)
        case _ => problemScoreElement(props.ps.toPrettyString, props.ps.percentage, if (props.ps.toInt == 0) "red" else "yellow")
      }
    })


  }
}
