package tester.ui.components


import otsbridge.CoursePiece
import otsbridge.CoursePiece.{Container, CoursePiece}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import typings.StBuildingComponent
import typings.antd.antdStrings.dark
import typings.antd.components._
import typings.antd.libMenuMenuItemMod.MenuItem
import viewData.PartialCourseViewData

@react object CourseContents {
  case class Props(pcvd: PartialCourseViewData, onPieceSelected: CoursePiece => Unit)


  val component = FunctionalComponent[Props] { props =>
    //    val (name, setName) = useState[T](t)

    def buildMenuFor(container: Container): ReactElement = {

      val title = container match {
        case CoursePiece.CourseRoot(title, annotation, childs) => title
        case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) => title
        case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) => title
      }

      if (container.childs.count(_.isInstanceOf[Container]) == 0) {
        MenuItem.withKey(container.alias)(title)
      } else {
        SubMenu.withKey(container.alias)
          .title(title)
          .onTitleClick(_ => props.onPieceSelected(props.pcvd.courseData.findByAlias(container.alias).getOrElse(props.pcvd.courseData)))(
            container.childs.flatMap {
              case c: Container => Some(buildMenuFor(c))
              case _ => None
            }: _ *
          )
      }
    }

    useEffect(() => {})

    Menu()
      .onClick(ev => {
        props.onPieceSelected(props.pcvd.courseData.findByAlias(ev.key.toString).getOrElse(props.pcvd.courseData))
      })
      .theme(dark)
      .mode(typings.rcMenu.esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        props.pcvd.courseData.childs.collect { case c: Container => c }.map(buildMenuFor)
      )
  }
}

