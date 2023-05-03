package tester.ui.components

import clientRequests.GetPartialCourseData

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.React
import slinky.core.facade.ReactContext.RichReactContext
import tester.ui.requests.Helpers.sendRequest
import typings.antd.antdStrings.large
import typings.antd.components.{List => AntList, _}
import viewData.{CourseInfoViewData, PartialCourseViewData}


@react object CourseLoaderLayout {
  case class Props(loggedInUser: LoggedInUser, courseInfo: CourseInfoViewData, logout: () => Unit)

  val component = FunctionalComponent[Props] { props =>
    val (courseData, setCourseData) = useState[Option[PartialCourseViewData]](None)

    useEffect(() => {
      sendRequest(GetPartialCourseData, clientRequests.GetPartialCourseDataRequest(props.loggedInUser.token, props.courseInfo.courseId))(onComplete = {
        case clientRequests.GetPartialCourseDataSuccess(data) =>
          setCourseData(Some(data))
        case clientRequests.GetPartialCourseNotOwnedByYou() => Notifications.showError(s"Это не ваш курс") //todo
        case clientRequests.GetPartialCourseNotFound() => Notifications.showError(s"Курс не найден") //todo
        case clientRequests.GetPartialCourseDataFailure(clientRequests.BadToken()) => Notifications.showError(s"Полохой токен") //todo
        case clientRequests.GetPartialCourseDataFailure(fal) => Notifications.showError(s"Ошибка 501") //todo
      }, onFailure = {
        x => Notifications.showError(s"Ошибка клиента")
      })
    }, Seq())

    courseData match {
      case Some(p) =>
        DisplayPartialCourse(props.loggedInUser, p, props.logout)
      case None =>
        Helpers.basicLayout(Spin().tip(s"Загрузка курса...").size(large), props.logout)
    }
  }


}
