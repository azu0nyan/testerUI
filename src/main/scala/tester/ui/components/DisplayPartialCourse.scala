package tester.ui.components

import otsbridge.CoursePiece.CoursePiece

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.{React, ReactElement}
import slinky.core.facade.ReactContext.RichReactContext
import tester.ui.components.Helpers.SetInnerHtml
import tester.ui.requests.Helpers.sendRequest
import typings.antd.antdStrings.{dark, large}
import typings.antd.components.{List => AntList, _}
import typings.rcMenu.esInterfaceMod
import typings.react.mod.CSSProperties
import viewData.{CourseInfoViewData, PartialCourseViewData, ProblemRefViewData, ProblemViewData}


@react object DisplayPartialCourse {
  case class Props(loggedInUser: LoggedInUser, partialCourse: PartialCourseViewData)

  case class LoadedProblemData(pvd: ProblemViewData, answerInField: String)

  sealed trait DisplayAppMode
  case object DisplayCourseMode extends DisplayAppMode
  case object DisplayProblemMode extends DisplayAppMode
  case object DisplayCourseAndProblem extends DisplayAppMode

  val component = FunctionalComponent[Props] { props =>



    val (appMode, setAppMode) = useState[DisplayAppMode](DisplayCourseAndProblem)
    val (loadedProblems, setLoadedProblems) = useState[Map[String, LoadedProblemData]](Map[String, LoadedProblemData]())

    val (selectedProblem, setSelectedProblemInner) = useState[Option[ProblemRefViewData]](None)
    val (selectedCoursePiece, setSelectedCoursePieceInner) = useState[CoursePiece](props.partialCourse.courseData)

    def setSelectedProblem(pref: Option[ProblemRefViewData]): Unit = {
      setSelectedProblemInner(pref)
      (pref, appMode) match {
        case (Some(_), DisplayCourseMode) => setAppMode(DisplayProblemMode)
        case (None, DisplayProblemMode) =>  setAppMode(DisplayCourseMode)
        case _ =>
      }
    }

    def setSelectedCoursePiece(cp: CoursePiece): Unit = {
      setSelectedCoursePieceInner(cp)
      appMode match {
        case DisplayProblemMode => setAppMode(DisplayCourseMode)
        case _ =>
      }
    }

    def onProblemLoaded(ref: ProblemRefViewData, problemViewData: ProblemViewData): Unit = {
      setLoadedProblems(old => {
        old.get(ref.templateAlias) match {
          case Some(loadedData) => old + (ref.templateAlias -> loadedData.copy(pvd = problemViewData))
          case None => old + (ref.templateAlias -> LoadedProblemData(problemViewData, problemViewData.answers.lastOption.map(_.answerText).getOrElse("")))
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

    def displayCourse(): ReactElement =
      CourseText(props.partialCourse, selectedCoursePiece, p => setSelectedProblem(Some(p)), cp => setSelectedCoursePiece(cp))

    def displayProblem(): ReactElement =
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
        case None =>
          div("Выберите задачу")
      }

    def displayContent(): ReactElement = Layout.Content()(appMode match {
      case DisplayCourseMode => displayCourse()
      case DisplayProblemMode => displayProblem()
      case DisplayCourseAndProblem =>
        Row().wrap(true)(
          Col()
            .flex("1 1 300px")(
              Card().style(CSSProperties().setMinWidth("300px").setMaxWidth("900px").setMargin(20).setPadding(5))(
                displayCourse()
              )
            ),
          Col()
            .flex("1 1 400px")(
              Card().style(CSSProperties().setMinWidth("400px").setMaxWidth("900px").setMargin(20).setPadding(5))(
                displayProblem()
              )
            )
        )
      case _ => println(s"Unknown mode $appMode")
      div()
    })


    Layout()(
      Layout.Sider()(
        CourseContents(props.partialCourse, cp => setSelectedCoursePiece(cp))
      ),
      displayContent(),
        /*
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
        case None =>
          CourseText(props.partialCourse, selectedCoursePiece, p => setSelectedProblem(Some(p)), cp => setSelectedCoursePiece(cp))
*/
          /*div(style := js.Dynamic.literal(
            width = "-webkit-fill-available"
          ))(
            div(dangerouslySetInnerHTML := new SetInnerHtml(selectedCoursePiece.fullHtml(Map())))
          )
      },*/
      Layout.Sider()(
        CourseProblemSelector(props.partialCourse, spRef => setSelectedProblem(Some(spRef)))
//        Menu().theme(dark).mode(esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
//          props.partialCourse.problems.map(p => MenuItem("")(p.title).onClick(_ => setSelectedProblem(Some(p))))
//        )
      ),

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
          case clientRequests.UnknownGetProblemDataFailure() => Notifications.showError(s"Не могу загрузить задачу")
        })
      })
      Space().style(CSSProperties().setWidth("100%").setHeight("100%").setJustifyContent("center"))(
        Spin().tip(s"Загрузка задачи...").size(large)
      )
    }
  }
}
