package tester.ui.components


import clientRequests.watcher.{LightGroupScoresRequest, LightGroupScoresSuccess, UnknownLightGroupScoresFailure}
import otsbridge.CoursePiece.CourseRoot
import otsbridge.ProblemScore.ProblemScore
import slinky.core.WithAttrs.build

import scala.scalajs.js
import slinky.core._
import slinky.web.html.{h5, _}
import typings.antd.components._
import typings.antd.{antdBooleans, antdInts, antdStrings}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import tester.ui.requests.Request
import typings.antd.anon.ScrollToFirstRowOnChange
import typings.antd.antdStrings.large
import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}
import typings.csstype.mod.WhiteSpaceProperty.nowrap
import typings.rcTable.anon.X
import typings.rcTable.{libInterfaceMod, rcTableStrings}
import typings.react.mod.CSSProperties
import viewData.{CourseTemplateViewData, GroupDetailedInfoViewData, UserViewData}

import scala.scalajs.js.|

@react object GroupResultsTable {

  //AdminCourseInfo

  case class Props(loggedInUser: LoggedInUser, data: GroupDetailedInfoViewData)

  val component = FunctionalComponent[Props] { props =>
    val (loaded, setLoaded) = useState[Boolean](false)
    val (loadedData, setLoadedData) = useState[LightGroupScoresSuccess](LightGroupScoresSuccess(Map(), Map()))

    useEffect(() => {
      Request.sendRequest(clientRequests.watcher.LightGroupScores,
        LightGroupScoresRequest(props.loggedInUser.token, props.data.groupId, props.data.courses.map(_.courseTemplateAlias), props.data.users.map(_.id)))(
        onComplete = {
          case l: LightGroupScoresSuccess =>

            setLoadedData(l)
            setLoaded(true)
          case UnknownLightGroupScoresFailure() =>
            Notifications.showError(s"Не могу загрузить результаты. 501")
        },
        onFailure = t => Notifications.showError(s"Не могу загрузить результаты $t 4хх")
      )
    }, Seq())


    def table() = {
      class UserTableItem(val userId: String, val userName: String, val problemToScoreId: Map[String, (ProblemScore, String)]) extends js.Object

      def toName(u: UserViewData): String = {
        val f = (u.lastName.getOrElse("").strip() + " " + u.firstName.getOrElse(" ").strip()).strip()
        if (f.isEmpty) u.login else f
      }

      val datasource: Seq[UserTableItem] = loadedData.userScores.map { case (userId, scores) => new UserTableItem(userId, props.data.users.find(_.id == userId).map(toName).getOrElse("Пользователь не найден"),
        scores.flatMap { case (courseAlias, scores) => scores.map { case (problemAlias, score) => (problemAlias, (score, loadedData.aliasToTitle.get(problemAlias).getOrElse(""))) } })
      }.toSeq



      val nameColumn = ColumnType[UserTableItem]()
        .setTitle(h5("Имя"))
        .setWidth("150px")
        .setDataIndex("userName")
        .setFixed(rcTableStrings.left)
        .setRender((_, tableItem, _) => h5(style := js.Dynamic.literal(whiteSpace = "nowrap", overflow = "hidden"))(tableItem.userName))

      val problemColumns =
        loadedData.aliasToTitle.toSeq
          .sortBy { case (a, t) => props.data.courses.map(c => c.problems.indexOf(a)).minOption.getOrElse(-1) } //todo count course id
          .map { case (alias, title) => ColumnType[UserTableItem]()
            .setTitle(h5(title))
            //          .setTitle(div(h5(style := js.Dynamic.literal( color = "red"))(title)))
            .setWidth("50px")
            .setDataIndex(alias)
            .setKey(alias)
            .setRender((_, tableItem, _) => h5(style := js.Dynamic.literal(whiteSpace = "nowrap", overflow = "hidden")) {

              val problemScore = tableItem.problemToScoreId.get(alias)
              problemScore match {
                case Some((score, _)) => SmallProblemScoreDisplay(score)
                case None => div()
              }
            })
          }


      def courseColumns(c: CourseTemplateViewData): ColumnGroupType[UserTableItem] = {
//        val (courseData, setCourseData) = useState[Option[CourseRoot]](None)

//        courseData match {
//          case Some(courseRoot) => ???
//          case None =>
            val prows: Seq[ColumnType[UserTableItem]] = c.problems.map(alias =>
              ColumnType[UserTableItem]()
                .setTitle(build(h5(loadedData.aliasToTitle.getOrElse(alias, alias).toString)))
                .setWidth("50px")
                .setDataIndex(alias)
                .setKey(alias)
                .setRender((_, tableItem, _) => h5(style := js.Dynamic.literal(whiteSpace = "nowrap", overflow = "hidden")) {

                  val problemScore = tableItem.problemToScoreId.get(alias)
                  problemScore match {
                    case Some((score, _)) => SmallProblemScoreDisplay(score)
                    case None => div()
                  }
                })
            )

            val castedArray: js.Array[ColumnGroupType[UserTableItem] | ColumnType[UserTableItem]] = js.Array(prows: _ *)

            ColumnGroupType[UserTableItem](castedArray).setTitle(build(h5(c.title))).setKey(c.courseTemplateAlias)

//        }
      }

      val cc =  props.data.courses.map(courseColumns)//.map(gt => |.from[ColumnGroupType[UserTableItem] ,ColumnGroupType[UserTableItem],ColumnType[UserTableItem]](gt))

      def columns: Seq[ColumnGroupType[UserTableItem] | ColumnType[UserTableItem]] =
        |.from[ColumnType[UserTableItem] ,ColumnGroupType[UserTableItem],ColumnType[UserTableItem]](nameColumn) +:
          props.data.courses.map(courseColumns).map(gt => |.from[ColumnGroupType[UserTableItem] ,ColumnGroupType[UserTableItem],ColumnType[UserTableItem]](gt))

      //      val courseColumns = ColumnGroupType[UserTableItem]()

      class ScrollSettings(val x: String, val y: String) extends js.Object

      Table[UserTableItem]
        .size(antdStrings.small)
        //        .style(CSSProperties().setWhiteSpace(nowrap))
        .bordered(true)
        .dataSourceVarargs(datasource: _ *)
        .pagination(antdBooleans.`false`)
        .scroll(new ScrollSettings("", "80vh").asInstanceOf[js.UndefOr[X] with ScrollToFirstRowOnChange])
        .columnsVarargs(
//          nameColumn
//          (nameC/olumn +: cc) : _ *
          columns: _ *
        )

    }


    Card()
      .title(s"Группа ${props.data.groupTitle}")
      .bordered(true)
      .style(CSSProperties().setWidth("100%"))(
        if (loaded) {
          div(          
            table()
          )
        } else {
          Spin().tip(s"Загрузка результатов...").size(large)
        }
      )
  }
}




