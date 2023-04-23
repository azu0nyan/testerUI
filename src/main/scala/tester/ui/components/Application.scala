package tester.ui.components

import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import org.scalajs.dom._
import slinky.core.facade.ReactElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import viewData._

import java.time.Instant

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

sealed trait ApplicationData
case class LoggedInUserInfo(userViewData: UserViewData) extends ApplicationData
case class NoUser() extends ApplicationData


@react class Application extends Component {
  case class Props()
  type State = ApplicationData
  private val css = CSS

  override def initialState = NoUser()

  override def render(): ReactElement = {
    div(
      "Войдите",
      state match {
        case LoggedInUserInfo(uvd) =>
          ApplicationLayout(uvd)
        case NoUser() => LoginForm(new LoginForm.Props(tryLogin = lp => {
            println(lp)
            setState(LoggedInUserInfo(UserViewData("id", lp.login, Some(s"na${lp.login}"), Some(s"la${lp.login}"), Some(s"${lp.login}@abibas.ru"), Seq(), "шкила", Instant.now())))
        }))
      }
    )
  }
}

