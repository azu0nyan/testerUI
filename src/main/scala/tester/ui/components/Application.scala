package tester.ui.components

import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import org.scalajs.dom._
import slinky.core.facade.ReactElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

sealed trait ApplicationData
case class LoggedInUserInfo() extends ApplicationData
case class NoUser() extends ApplicationData


@react class Application extends Component {
  case class Props()
  type State = ApplicationData
  private val css = CSS


  override def initialState = NoUser()

  override def render(): ReactElement = {
    div(
      "Войдите",
      LoginForm()
    )
  }
}

