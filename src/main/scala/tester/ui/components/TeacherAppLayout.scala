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
import typings.antDesignIcons.components.AntdIcon
import typings.csstype.mod.{OverflowYProperty, PositionProperty}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

@react object TeacherAppLayout {
  case class Props(loggedInUser: LoggedInUser, logout: () => Unit)

  sealed trait TeacherAppState
  case object WelcomeScreenTeacherAppState extends TeacherAppState
  case object AnswerConfirmationAppState extends TeacherAppState
  case class GroupInfoTeacherAppState(groupId: String) extends TeacherAppState
  case class GroupResultsTableTeacherAppState(groupId: String) extends TeacherAppState

  val component = FunctionalComponent[Props] { props =>

    val (state, setState) = useState[TeacherAppState](WelcomeScreenTeacherAppState)
    val (groups, setGroups) = useState[Seq[viewData.GroupDetailedInfoViewData]](Seq())
    val (leftCollapsed, setLeftCollapsed) = useState[Boolean](false)

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
        SubMenu.withKey("main")
          .icon(AntdIcon( typings.antDesignIconsSvg.esAsnUserOutlinedMod.default))
          .title("Группы")(
          groups.map(group => SubMenu.withKey(group.groupId).title(group.groupTitle)(
            MenuItem.withKey(group.groupId + "about").onClick(_ => setState(GroupInfoTeacherAppState(group.groupId)))("О группе"),
            MenuItem.withKey(group.groupId + "results").onClick(_ => setState(GroupResultsTableTeacherAppState(group.groupId)))("Результаты"),
          ))
        ),
        MenuItem.withKey("worksChecking").onClick(_ => setState(AnswerConfirmationAppState) ).icon(AntdIcon( typings.antDesignIconsSvg.esAsnAuditOutlinedMod.default))("Проверка работ"),
        MenuItem.withKey("logout").onClick(_ => props.logout() ).icon(AntdIcon( typings.antDesignIconsSvg.esAsnLogoutOutlinedMod.default))("Выход")
      )
    }


    Layout().hasSider(true)(
      Layout.Sider
        .collapsible(true)
        .collapsed(leftCollapsed)
        .onCollapse((b, _) => setLeftCollapsed(b))
        .collapsedWidth(0)
        .zeroWidthTriggerStyle(CSSProperties())
        .trigger(null)
        .style(CSSProperties().setHeight("100vh")
          .setPosition(PositionProperty.fixed)
          .setLeft(0).setTop(0).setBottom(0)
          .setOverflowY(OverflowYProperty.auto))(
          siderGroupMenu
        ),
      Layout().style(CSSProperties().setMarginLeft(200))(
        //header
        Layout.Content(
          state match {
            case WelcomeScreenTeacherAppState =>
              Card.bordered(true)
                .style(CSSProperties())(
                  Title.level(antdInts.`1`)("Добро пожаловать"),
                  p("Вас привествует интерфейс учителя на tester.lnmo.ru . Выберите группу или курс для работы в меню слева.")
                )
            case GroupInfoTeacherAppState(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => GroupDetailedInfo(gvd)
                case None => div(s"Группа $groupId не найдена.")
              }
            case GroupResultsTableTeacherAppState(groupId) =>
              groups.find(_.groupId == groupId) match {
                case Some(gvd) => GroupResultsTable(props.loggedInUser, gvd)
                case None => div(s"Группа $groupId не найдена.")
              }
            case AnswerConfirmationAppState =>
              AnswerConfirmationLayout(props.loggedInUser, groups)
          }
        )
      ),
      //Layout.Footer()
    )
  }
}



