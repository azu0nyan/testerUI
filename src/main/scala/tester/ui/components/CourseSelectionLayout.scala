package tester.ui.components

import clientRequests.GetCoursesList
import clientRequests.admin.{CourseList, CourseListSuccess}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.useState
import tester.ui.requests.Helpers.sendRequest
import typings.antd.antdStrings.dark
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List => AntList, _}
import typings.rcMenu.esInterfaceMod.MenuMode.inline
import typings.react.mod.CSSProperties
import viewData.{PartialCourseViewData, ProblemRefViewData, UserViewData}
import viewData.CourseInfoViewData

import scala.collection.immutable.{AbstractSeq, LinearSeq}

@react object CourseSelectionLayout {
  case class Props(loggedInUser: LoggedInUser, onSelected: CourseInfoViewData => Unit)


  def rightSider(u: UserViewData) = {
    Layout.Sider()
      .style(CSSProperties()
        //      .setOverflow(OverflowBlockProperty.auto)
        //      .setHeight("100vh")
        //      .setPosition(PositionProperty.fixed)
        //      .setLeft(0)
        //      .setTop(0)
        //      .setBottom(0)
      )(UserInfoBox(u))
  }

  def leftSider(coursesList: Seq[CourseInfoViewData], setSelected: CourseInfoViewData => Unit) = {
    Layout.Sider()(coursesList match {
      case Seq() => div("Loading...")
      case courses =>
        Menu().theme(dark).mode(inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
          courses.map(course => MenuItem("")(course.title).onClick(_ => setSelected(course)))
        )
    })
  }

  def content(courses: Seq[CourseInfoViewData], selectedCourse: Option[CourseInfoViewData]) = {
    Layout.Content()
      .style(CSSProperties().setMinHeight("120"))(
        selectedCourse match {
          case Some(course) => div(
            h1(course.title),
            p(course.description),
            p(course.status.toString)
          )
          case None => div("Выберите курс")
        }
      )

  }

  val component = FunctionalComponent[Props] { props =>

    val (coursesList, setCoursesList) = useState[Seq[CourseInfoViewData]](Seq())
    val (selectedCourse, setSelectedCourse) = useState[Option[CourseInfoViewData]](None)

    sendRequest(GetCoursesList, clientRequests.RequestCoursesList(props.loggedInUser.token))(onComplete = {
      case clientRequests.GetCoursesListSuccess(courses) =>
        setCoursesList(courses.existing)
      case clientRequests.GetCoursesListFailure(fal) => //todo
    })

    Layout()(
      leftSider(coursesList, x => setSelectedCourse(Some(x))),
      content(coursesList, selectedCourse),
      rightSider(props.loggedInUser.userViewData)
    )
  }
}
