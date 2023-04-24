package tester.ui.components

import clientRequests._
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import org.scalajs.dom._
import slinky.core.facade.ReactElement
import tester.ui.requests.Helpers.sendRequest

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import viewData._

import java.time.Instant

@JSImport("antd/dist/antd.css", JSImport.Default)
@js.native
object CSS extends js.Any

sealed trait ApplicationData
case class LoggedInUser(token:String, userViewData: UserViewData) extends ApplicationData
case class NoUser() extends ApplicationData


@react class Application extends Component {
  case class Props()
  type State = ApplicationData
  private val css = CSS

  override def initialState = NoUser()

  import typings.antd.libNotificationMod.{ArgsProps, IconType, default => Notification}
  def showError(message:String) = renderNotification(message, IconType.error)
  def showSuccess(message:String) = renderNotification(message, IconType.success)
  def renderNotification(message:String, iconType: IconType) = {
    Notification.open(ArgsProps(message = message).setType(iconType))
  }


  def tryLogin(lp: LoginForm.LoginPassword): Unit = {
    sendRequest(Login, LoginRequest(lp.login, lp.password))(onComplete = {
      case LoginSuccessResponse(token: String, userData: UserViewData) =>
        setState(LoggedInUser(token, userData))
      case LoginFailureUserNotFoundResponse() =>
        showError(s"Неизвестный логин")
      case LoginFailureWrongPasswordResponse() =>
        showError(s"Неизвестный пароль")
      case LoginFailureUnknownErrorResponse() =>
        showError(s"Ошибка 501")
    }, onFailure = x => {
      showError(s"Ошибка 404")
      x.printStackTrace()
    })
  }

  override def render(): ReactElement = {
    div(
      state match {
        case l:LoggedInUser =>
          UserAppLayout(l)
        case NoUser() => LoginForm(new LoginForm.Props(tryLogin = tryLogin))
      }
    )
  }
}

