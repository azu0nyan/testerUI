package tester.ui.components


import clientRequests.admin.{GroupListResponseFailure, GroupListResponseSuccess}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import typings.antd.{antdInts, antdStrings}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.requests.Request
import typings.csstype.mod.OverflowYProperty
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

@react object TeacherAppLayout {
  case class Props(loggedInUser: LoggedInUser, logout: () => Unit)

  sealed trait TeacherAppState
  case object WelcomeScreen extends TeacherAppState
  case class GroupInfo(groupId: String) extends TeacherAppState
  case class GroupResultsTable(groupId: String) extends TeacherAppState

  val component = FunctionalComponent[Props] { props =>

    val (state, setState) = useState[TeacherAppState](WelcomeScreen)
    val (groups, setGroups) = useState[Seq[viewData.GroupDetailedInfoViewData]](Seq())

    useEffect(() => {
      Request.sendRequest(clientRequests.admin.GroupList, clientRequests.admin.GroupListRequest(props.loggedInUser.token))(
        onComplete = {
          case GroupListResponseSuccess(groups) =>
            setGroups(groups)
          case GroupListResponseFailure() =>
            Notifications.showError(s"Не могу загрузить список групп (501)")
        }, onFailure = t =>
          Notifications.showError(s"Не могу загрузить список групп ${t.toString} (4xx)")
      )
    }, Seq())


    def siderGroupMenu: ReactElement = {
      Menu().theme(antdStrings.dark).mode(esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        SubMenu.withKey("main").title("Группы")(
          groups.map(group => SubMenu.withKey(group.groupId).title(group.groupTitle)(
            MenuItem.withKey(group.groupId + "about").onClick(_ => setState(GroupInfo(group.groupId)))("О группе"),
            MenuItem.withKey(group.groupId + "results").onClick(_ => setState(GroupResultsTable(group.groupId)))("Результаты"),
          ))
        )
      )
    }


    Layout()(
      Layout.Header(),
      Layout(
        Layout.Sider.style(CSSProperties().setHeight("100vh").setMaxHeight("100vh").setOverflowY(OverflowYProperty.scroll))(
          siderGroupMenu
        ),
        Layout.Content(
          state match {
            case WelcomeScreen =>
              Card.bordered(true)
                .style(CSSProperties())(
                  Title.level(antdInts.`1`)("Добро пожаловать"),
                  p("Вас привествует интерфейс учителя на tester.lnmo.ru . Выберите группу или курс для работы в меню слева.")
                )
            case GroupInfo(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => GroupDetailedInfo(gvd)
                case None => div(s"Группа $groupId не найдена.")
              }
            case GroupResultsTable(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => div(s"Результаты группы ${gvd.groupTitle}")
                case None => div(s"Группа $groupId не найдена.")
              }
          }
        )
      ),
      Layout.Footer()
    )
  }
}



