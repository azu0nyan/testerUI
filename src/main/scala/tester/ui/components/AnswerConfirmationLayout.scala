package tester.ui.components


import clientRequests.teacher.{AnswerForConfirmationListFailure, AnswerForConfirmationListSuccess, AnswersListFailure, AnswersListSuccess, AwaitingConfirmation, CourseAnswersConfirmationInfo, UserConfirmationInfo}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import typings.antd.{antdInts, antdStrings, libCarouselMod}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.components.Helpers.SetInnerHtml
import tester.ui.requests.Request
import typings.rcTabs.esInterfaceMod
import viewData.AnswerViewData

@react object AnswerConfirmationLayout {
  case class Props(loggedInUser: LoggedInUser, groups: Seq[viewData.GroupDetailedInfoViewData])

  val component = FunctionalComponent[Props] { props =>
  val (userAnswers, setUserAnswers) = useState[ Seq[UserConfirmationInfo]](Seq())


    useEffect(() => {
      Request.sendRequest(clientRequests.teacher.AnswerForConfirmationList,
        clientRequests.teacher.AnswerForConfirmationListRequest(props.loggedInUser.token))(
        onComplete = {
          case AnswerForConfirmationListSuccess(newAnswers) =>
            setUserAnswers(newAnswers)
          case f:AnswerForConfirmationListFailure => Notifications.showError(s"Не могу загрузить ответы. (501)")
        },
        onFailure = t =>
          Notifications.showError(s"Не могу загрузить ответы. 4xx")
      )

    },Seq())




    def getProblemName(u: UserConfirmationInfo, problemId: String): String = { //todo use partial data
      u.courses.find(_.course.problemIds.contains(problemId)) match {
        case Some(course) =>
          val id = course.course.problemIds.indexOf(problemId)
          props.groups.flatMap(_.courses).find(c => c.courseTemplateAlias == course.course.templateAlias) match {
            case Some(courseTemplate) =>
              if(courseTemplate.problems.size > id ) {
                courseTemplate.problems(id)
              } else {
                s"Ошибка в курсе меньше заданий ${course.course.templateAlias}"
              }
            case None => s"Нет курса с алиасом ${course.course.templateAlias}"
          }


        case None => s"Нет имени задания $problemId"
      }
    }
    def getName(uid: String): String = props.groups.flatMap(_.users).find(_.id == uid).map(Helpers.toName).getOrElse(s"Нет имени пользователя $uid")
    def haveAnswers(c:Seq[CourseAnswersConfirmationInfo]) : Boolean = c.exists(_.answer.nonEmpty)



    def userAnswersCarousel(s: Seq[AnswerViewData]): ReactElement = {
      if(s.size > 1) {
        Tabs().tabPosition(esInterfaceMod.TabPosition.left)(
          s.zipWithIndex.reverse.map { case (a, i) =>
            Tabs.TabPane.tab(i.toString).withKey(i.toString)(
              TeacherConfirmAnswerForm(props.loggedInUser, a)
            )
          }
        )
      } else {
        TeacherConfirmAnswerForm(props.loggedInUser, s.head)
      }
    }

    def userAnswersTab(u: UserConfirmationInfo): ReactElement = {
      Tabs().tabPosition(esInterfaceMod.TabPosition.left)(
        u.courses.flatMap(_.answer).groupBy(_.problemId).map{case (problemId, answers) =>
          Tabs.TabPane.tab(getProblemName(u, problemId)).withKey(problemId)(
            userAnswersCarousel(answers)
          )
        }
      )
    }

    def workAcceptTabs: ReactElement = {
//      div(userAnswers.toString())
      Tabs().tabPosition(esInterfaceMod.TabPosition.top)(
        userAnswers.collect{case u@UserConfirmationInfo(uid, courses) if haveAnswers(courses) =>
          Tabs.TabPane.tab(getName(uid)).withKey(uid)(
            userAnswersTab(u)
          )
        }
      )

    }

    Card().title("Проверка работ")(
      if(userAnswers.isEmpty) div("Нет работ для проверки")
      else {
       workAcceptTabs
      }
    )
  }
}


