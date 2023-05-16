package tester.ui.components


import DbViewsShared.CourseShared
import clientRequests.teacher.TeacherConfirmAnswerSuccess
import otsbridge.AnswerField.ProgramAnswer
import otsbridge.ProblemScore
import otsbridge.ProblemScore.{BinaryScore, ProblemScore, XOutOfYScore}

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import typings.antd.{antdInts, antdStrings}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.DateFormat
import tester.ui.components.Helpers.SetInnerHtml
import tester.ui.requests.Request
import typings.react.mod.CSSProperties
import viewData.AnswerViewData

@react object TeacherConfirmAnswerForm {
  case class Props(loggedInUser: LoggedInUser, avd: AnswerViewData)

  val component = FunctionalComponent[Props] { props =>
    val (review, setReview) = useState[Option[String]](props.avd.status match {
      case CourseShared.Verified(score, review, systemMessage, verifiedAt, confirmedAt) => review
      case _ => None
    })

    useEffect(() => {})

    val date = DateFormat.dateFormatter.format(props.avd.answeredAt)

    val scoreDisplay: ReactElement = props.avd.score match {
      case Some(s@ProblemScore.MultipleRunsResultScore(runResults)) =>
        div(
          div(style := js.Dynamic.literal(maxWidth = "300px", margin = "5px"))(ProblemScoreDisplay(s, true, false)),
          DisplayProblem.displayRunResultsTable(runResults)
        )
      case Some(score) => div(style := js.Dynamic.literal(maxWidth = "300px", margin = "5px"))(ProblemScoreDisplay(score, true, false))
      case None => p("Не оценено")
    }

    import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import io.circe._, io.circe.parser._
    import io.circe.generic.auto._, io.circe.syntax._
    val answer = decode[ProgramAnswer](props.avd.answerText) match {
      case Left(prog) =>   props.avd.answerText
      case Right(ProgramAnswer(prog, lang)) => prog
    }


    val scores:Seq[ProblemScore] = props.avd.score.iterator.to(Seq) ++ Seq(
      BinaryScore(true),
      BinaryScore(false),
      XOutOfYScore(1, 2),
      XOutOfYScore(2, 2),
      XOutOfYScore(1, 3),
      XOutOfYScore(2, 3),
      XOutOfYScore(3, 3),
      XOutOfYScore(1, 4),
      XOutOfYScore(2, 4),
      XOutOfYScore(3, 4),
      XOutOfYScore(4, 4),
    ).filter(s => !props.avd.score.contains(s))

    val (score, setScore) = useState[ProblemScore](props.avd.score.getOrElse(BinaryScore(true)))


    /*val selectScore = Select[ProblemScore]
      .defaultValue(score)
      .style(CSSProperties().setWidth("50px"))
      .onChange((newVal, _) => {
//        setScore(newVal)
      })(
        scores.map(sc => Select.Option(sc.toPrettyString)(sc.toPrettyString).withKey(sc.toPrettyString))
      )*/
    //FUCK JS
    val selectScore = Select[String]
      .defaultValue(score.toPrettyString)
      .style(CSSProperties().setWidth("100px"))
      .onChange((newVal, _) => {
          setScore(scores.find(_.toPrettyString == newVal).get)
      })(
        scores.map(sc => Select.Option(sc.toPrettyString)(sc.toPrettyString))
      )

    def submit(forceDeny: Boolean) : Unit = {
      Request.sendRequest(clientRequests.teacher.TeacherConfirmAnswer,
        clientRequests.teacher.TeacherConfirmAnswerRequest(props.loggedInUser.token, props.avd.answerId, if(forceDeny) BinaryScore(false) else score, review))(
        onComplete = {
          case TeacherConfirmAnswerSuccess() =>
            Notifications.showSuccess(s"Оценено")
          case clientRequests.teacher.UnknownTeacherConfirmAnswerFailure() =>
            Notifications.showError(s"Не могу оценить (501)")
        },
        onFailure = _ => Notifications.showError(s"Не могу оценить (4xx)")
      )
    }

    div(style := js.Dynamic.literal(minHeight = "600px"))(
      div(date),
      pre(code(dangerouslySetInnerHTML := new SetInnerHtml(answer))()),
      div(scoreDisplay),
      div("Отзыв",TextArea().rows(6).value(review.getOrElse("").toString).onChange(e => setReview(scala.Option.when(e.target_ChangeEvent.value.nonEmpty)(e.target_ChangeEvent.value)))),
      div(selectScore),
      Button().onClick( _ => submit(false))("Оценить"),
      Button().onClick( _ => submit(true))("Незачесть"),
    )
  }
}
