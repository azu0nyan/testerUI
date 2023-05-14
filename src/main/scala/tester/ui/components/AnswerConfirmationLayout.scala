package tester.ui.components


import clientRequests.teacher.{AnswersListFailure, AnswersListSuccess, AwaitingConfirmation}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import typings.antd.{antdInts, antdStrings}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.requests.Request

@react object AnswerConfirmationLayout {
  case class Props(loggedInUser: LoggedInUser, groups: Seq[viewData.GroupDetailedInfoViewData])

  val component = FunctionalComponent[Props] { props =>
  val (answers, setAnswers) = useState[ Seq[viewData.AnswerFullViewData]](Seq())


    useEffect(() => {
      Request.sendRequest(clientRequests.teacher.AnswersList, clientRequests.teacher.AnswersListRequest(props.loggedInUser.token, Seq(AwaitingConfirmation), false, None))(
        onComplete = {
          case AnswersListSuccess(newAnswers) =>
            setAnswers(newAnswers)
          case f:AnswersListFailure => Notifications.showError(s"Не могу загрузить ответы. (501)")
        },
        onFailure = t =>
          Notifications.showError(s"Не могу загрузить ответы. 4xx")
      )

    })

    def workAcceptMenu: ReactElement = {
      div()
    }

    Card().title("Проверка работ")(
      if(answers.isEmpty) div("Нет работ для проверки")
      else {
       workAcceptMenu
      }
    )
  }
}


