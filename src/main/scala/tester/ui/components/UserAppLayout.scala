package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.useState
import slinky.core.facade.ReactElement
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List => AntList, _}
import typings.csstype.mod.FloatProperty
import typings.react.mod.CSSProperties
import viewData.{CourseInfoViewData, PartialCourseViewData, ProblemRefViewData, UserViewData}

@react object UserAppLayout {


  case class Props(loggedInUser: LoggedInUser, logout: () => Unit)


  val component = FunctionalComponent[Props] { props =>
    val (selectedCourse, setSelectedCourse) = useState[Option[CourseInfoViewData]](None)




    selectedCourse match {
      case Some(course) => CourseLoaderLayout(props.loggedInUser, course, props.logout)
      case None => Helpers.basicLayout(CourseSelectionLayout(loggedInUser = props.loggedInUser, onSelected = s => setSelectedCourse(Some(s))), props.logout)
    }

  }
}
