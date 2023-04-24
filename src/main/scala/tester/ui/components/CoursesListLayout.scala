package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List => AntList, _}
import typings.csstype.mod.{OverflowBlockProperty, PositionProperty}
import typings.react.mod.CSSProperties
import viewData.UserViewData

@react object CoursesListLayout {
  case class Props(u: UserViewData)

  def sider(u: UserViewData) = Layout.Sider()
    .style(CSSProperties()
      //      .setOverflow(OverflowBlockProperty.auto)
      //      .setHeight("100vh")
      //      .setPosition(PositionProperty.fixed)
      //      .setLeft(0)
      //      .setTop(0)
      //      .setBottom(0)
    )(UserInfoBox(u))

  def sizePair(x: Double, y: Double) = scala.scalajs.js.|.from[js.Tuple2[libSpaceMod.SpaceSize, libSpaceMod.SpaceSize], js.Tuple2[libSpaceMod.SpaceSize, libSpaceMod.SpaceSize], libSpaceMod.SpaceSize](js.Tuple2(scala.scalajs.js.|.from(x), scala.scalajs.js.|.from(y)))

  val component = FunctionalComponent[Props] { props =>
    Space()
      .direction(antdStrings.vertical)
      .style(CSSProperties().setWidth("100%"))
      //      .size(typings.antd.antdStrings.small)
      .size(sizePair(0, 48))(
        Layout()
        (
          Layout.Header().style(CSSProperties().setHeight(64d))(h1("Tester")),
          Layout()(
            Layout.Sider()("Left"),
            Layout.Content()
              .style(CSSProperties().setMinHeight("120"))(h1("Contert"), p("asdsad asd a ada s a a as ad ".repeat(123))),
            sider(props.u)
          ),
          Layout.Footer(i("Tester(c) 2049-present")),
        )
      )
  }
}
