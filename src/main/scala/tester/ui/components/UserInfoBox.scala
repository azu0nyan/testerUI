package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import typings.antd.antdStrings
import typings.antd.components.{List => AntList, _}
import typings.react.mod.CSSProperties
import viewData.UserViewData

@react object UserInfoBox {
  case class Props(u: UserViewData)

  val component = FunctionalComponent[Props] { props =>
    Card()
      .title("Профиль")
      .bordered(true)
      .style(CSSProperties().setMaxWidth("200px"))(
        Descriptions()
          .layout(antdStrings.vertical)
          .column(1d)
          .size(antdStrings.small)(
          Descriptions.Item().label("Логин")(props.u.login),
          Descriptions.Item().label("Имя")(props.u.firstName.getOrElse("").asInstanceOf[String]),
          Descriptions.Item().label("Фамилия")(props.u.lastName.getOrElse("").asInstanceOf[String]),
          Descriptions.Item().label("Почта")(props.u.email.getOrElse("").asInstanceOf[String]),
          Descriptions.Item().label("Дата регистрации")(props.u.registeredAt.toString),
          Descriptions.Item().label("Роль")(props.u.role),
          Descriptions.Item().label("Группы")(props.u.groups.mkString(", ")),
        )
      )

  }

  
  
}
