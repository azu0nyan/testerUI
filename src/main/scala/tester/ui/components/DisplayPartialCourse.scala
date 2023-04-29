package tester.ui.components

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.React
import slinky.core.facade.ReactContext.RichReactContext
import tester.ui.requests.Helpers.sendRequest
import typings.antd.antdStrings.{dark, large}
import typings.antd.components.{List => AntList, _}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.{CourseInfoViewData, PartialCourseViewData, ProblemRefViewData, ProblemViewData}


@react object DisplayPartialCourse {
  case class Props(loggedInUser: LoggedInUser, partialCourse: PartialCourseViewData)

  case class LoadedProblemData(pvd: ProblemViewData, answerInField: String)


  val component = FunctionalComponent[Props] { props =>
    val (loadedProblems, setLoadedProblems) = useState[Map[String, LoadedProblemData]](Map[String, LoadedProblemData]())

    val (selectedProblem, setSelectedProblem) = useState[Option[ProblemRefViewData]](None)
    useEffect(() => {})


    def onProblemLoaded(ref: ProblemRefViewData, problemViewData: ProblemViewData) : Unit  = {
      setLoadedProblems(old =>      {
        old.get(ref.templateAlias) match {
          case Some(loadedData) => old + (ref.templateAlias -> loadedData.copy(pvd = problemViewData))
          case None => old + (ref.templateAlias -> LoadedProblemData(problemViewData, problemViewData.answers.headOption.map(_.answerText).getOrElse("")))
        }
      })
    }
//
//    def saveAnswer(ref: ProblemRefViewData, newAnswer: String): Unit = {
//      println(s"Saving answer $newAnswer")
//      setLoadedProblems(old =>
//        old + (ref.templateAlias -> old(ref.templateAlias).copy(answerInField = newAnswer))
//      )
//    }

    Layout()(
      Layout.Sider(
        Menu().theme(dark).mode(esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
          props.partialCourse.problems.map(p => MenuItem("")(p.title).onClick(_ => setSelectedProblem(Some(p))))
        )
      ),
      selectedProblem match {
        case Some(problemRef) =>
          loadedProblems.get(problemRef.templateAlias) match {
            case Some(loadedData) => DisplayProblem(props.loggedInUser, loadedData, () => {
              sendRequest(clientRequests.GetProblemData, clientRequests.GetProblemDataRequest(props.loggedInUser.token, problemRef.problemId))(onComplete = {
                case clientRequests.GetProblemDataSuccess(pwd) => onProblemLoaded(problemRef, pwd)
                case clientRequests.UnknownGetProblemDataFailure() => Notifications.showError(s"Не могу загрузить задачу")
              })
            }).withKey(problemRef.problemId)
            case None =>
              ProblemLoader(props.loggedInUser, problemRef.problemId, p => onProblemLoaded(problemRef, p))
          }
        case None => div(props.partialCourse.courseData.fullHtml(Map()))
      }

//      Layout.Content(),
//      Layout.Sider()
    )
  }




  @react object ProblemLoader {
    case class Props(loggedInUser: LoggedInUser, problemId: String, onLoad: ProblemViewData => Unit)

    val component = FunctionalComponent[Props] { props =>
      useEffect(() => {
        sendRequest(clientRequests.GetProblemData, clientRequests.GetProblemDataRequest(props.loggedInUser.token, props.problemId))(onComplete = {
          case clientRequests.GetProblemDataSuccess(pwd) => props.onLoad(pwd)
          case clientRequests.UnknownGetProblemDataFailure() =>  Notifications.showError(s"Не могу загрузить задачу")
        })
      })
      Space().style(CSSProperties().setWidth("100%").setHeight("100%").setJustifyContent("center"))(
        Spin().tip(s"Загрузка задачи...").size(large)
      )
    }
  }
}
