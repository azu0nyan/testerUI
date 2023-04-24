package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.useState
import typings.antd.{antdStrings, libSpaceMod}
import typings.antd.components.{List => AntList, _}
import typings.react.mod.CSSProperties
import viewData.{CourseInfoViewData, PartialCourseViewData, ProblemRefViewData, UserViewData}

@react object UserAppLayout {
  case class Props(loggedInUser: LoggedInUser)


  def sizePair(x: Double, y: Double) = scala.scalajs.js.|.from[js.Tuple2[libSpaceMod.SpaceSize, libSpaceMod.SpaceSize], js.Tuple2[libSpaceMod.SpaceSize, libSpaceMod.SpaceSize], libSpaceMod.SpaceSize](js.Tuple2(scala.scalajs.js.|.from(x), scala.scalajs.js.|.from(y)))

  val component = FunctionalComponent[Props] { props =>
    val (selectedCourse, setSelectedCourse) = useState[Option[CourseInfoViewData]](None)
    val (selectedProblem, setSelectedProblem) = useState[Option[ProblemRefViewData]](None)


    Space()
      .direction(antdStrings.vertical)
      .style(CSSProperties().setWidth("100%"))
      //      .size(typings.antd.antdStrings.small)
      .size(sizePair(0, 48))(
        Layout()
        (
          Layout.Header().style(CSSProperties().setHeight(64d))(h1("Tester")),
          selectedCourse match {
            case Some(course) => div(course.toString)
            case None => CourseSelectionLayout(loggedInUser = props.loggedInUser, onSelected = s => setSelectedCourse(Some(s)))
          },
          Layout.Footer(i("Tester(c) 2049-present")),
        )
      )
  }
}
