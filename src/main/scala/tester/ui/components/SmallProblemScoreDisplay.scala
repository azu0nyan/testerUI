package tester.ui.components

import otsbridge.ProblemScore
import otsbridge.ProblemScore.ProblemScore

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react


@react object SmallProblemScoreDisplay {
  case class Props(ps: ProblemScore)

  def acceptNotAcceptText(passed: Boolean) =
    if (passed) b(style := js.Dynamic.literal(color = Helpers.customSuccessColor))("\uD83D\uDDF8")
    else b(style := js.Dynamic.literal(color = Helpers.customErrorColor))("✘")

  val component = FunctionalComponent[Props] { props =>
    div(style := js.Dynamic.literal(
      display = "flex",
      justifyContent = "center",
      fontSize = "10"
    ))(props.ps match {
      case bs:ProblemScore.BinaryScore => acceptNotAcceptText(bs.passed)
      case x if x.isMax => b(style := js.Dynamic.literal(color = Helpers.customSuccessColor))(x.toPrettyString)
      case x if x.toInt == 0 => b(style := js.Dynamic.literal(color = Helpers.customErrorColor))(x.toPrettyString)
      case x => b(style := js.Dynamic.literal(color = Helpers.customWarningColor))(x.toPrettyString)
    })

  }
}