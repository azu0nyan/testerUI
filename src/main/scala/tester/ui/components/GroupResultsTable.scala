package tester.ui.components


import clientRequests.watcher.{LightGroupScoresRequest, LightGroupScoresSuccess, UnknownLightGroupScoresFailure}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import typings.antd.{antdInts, antdStrings}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import tester.ui.requests.Request
import typings.antd.antdStrings.large
import typings.react.mod.CSSProperties
import viewData.GroupDetailedInfoViewData

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

    Card()
      .title(s"Группа ${props.data.groupTitle}")
      .bordered(true)
      .style(CSSProperties())(
        if(loaded){
          div(
            s"${loadedData.userScores.size}"
          )
        } else {
          Spin().tip(s"Загрузка результатов...").size(large)
        }
      )
  }
}




