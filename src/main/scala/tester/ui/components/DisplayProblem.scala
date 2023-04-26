package tester.ui.components

import clientRequests.SubmitAnswerResponse
import otsbridge.{AnswerField, ProgrammingLanguage}
import otsbridge.AnswerField.{AnswerField, ProgramAnswer}
import otsbridge.ProblemScore.ProblemScore
import otsbridge.ProgrammingLanguage.ProgrammingLanguage
import slinky.core.WithAttrs.build

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import tester.ui.components.DisplayPartialCourse.LoadedProblemData
import tester.ui.requests.Helpers.sendRequest
import typings.antd.components.{List => AntList, _}
import typings.reactAce.components.{Ace, ReactAce}
import typings.reactAce.libAceMod.IAceEditorProps
import typings.aceBuilds.aceBuildsStrings.theme
import typings.antd.antdStrings.primary
import typings.betterReactMathjax.components.MathJaxContext.configMathJax3Configundef
import typings.betterReactMathjax.components.{MathJax, MathJaxContext}
import typings.betterReactMathjax.mathJaxContextMathJaxContextMod.MathJaxContextProps
import typings.betterReactMathjax.mathJaxContextMod
import viewData.AnswerViewData
import clientRequests._

import java.time.Instant


@react object DisplayProblem {
  case class Props(loggedInUser: LoggedInUser, loadedData: LoadedProblemData, updateLoadedData: () => Unit)


  val component = FunctionalComponent[Props] { props =>
    val (currentAnswer, setCurrentAnswer) = useState[String]("")
    useEffect(() => {})

    class SetInner(val __html: String) extends js.Object

    val pvd = props.loadedData.pvd


    def submitAnswer(): Unit = {
      sendRequest(clientRequests.SubmitAnswer, clientRequests.SubmitAnswerRequest(props.loggedInUser.token, pvd.problemId, currentAnswer))(onComplete = {
        case ProblemNotFound() => Notifications.showError("Задача не найдена")
        case AlreadyVerifyingAnswer() => Notifications.showWarning("Ответ на эту задачу уже проверяется, наберитесь терпения.")
        case MaximumAttemptsLimitExceeded(attempts: Int) => Notifications.showError(s"Максимальное количество попыток $attempts превышено.")
        case AnswerSubmissionClosed(cause: Option[String]) => Notifications.showError(s"Прием ответов завершен." + cause.map(c => s"Причина: $c").getOrElse(""))
        case RequestSubmitAnswerFailure(BadToken()) => //todo
        case RequestSubmitAnswerFailure(_) => Notifications.showError(s"Ошибка 501")
        case UserCourseWithProblemNotFound() => Notifications.showError(s"Курс не найден")
        case ProblemIsNotFromUserCourse() => Notifications.showError(s"Задача не из вашего курса")
        case  AnswerSubmitted(avd:AnswerViewData) => answerSubmitSuccess(avd)
      })
    }
    
    def answerSubmitSuccess(avd: AnswerViewData) : Unit = avd match {
      case AnswerViewData(answerId, problemId, answerText, answeredAt, status) =>
        
    }

    div(
      h1(pvd.title),
      MathJax(div(dangerouslySetInnerHTML := new SetInner(pvd.problemHtml))),
      //score
      displayAnswerField(pvd.answerFieldType, currentAnswer, setCurrentAnswer, () => submitAnswer()),
      displayAnswers(pvd.answers)

    )
  }

  def langToAceName(p: ProgrammingLanguage): String = p match {
    case ProgrammingLanguage.Java => "java"
    case ProgrammingLanguage.Haskell => "haskell"
    case ProgrammingLanguage.Scala => "scala"
    case ProgrammingLanguage.Kojo => "scala"
    case ProgrammingLanguage.Cpp => "cpp" //todo check
  }

  def displayAnswerField(af: AnswerField, currentAnswer: String, setCurrentAnswer: String => Unit, submit: () => Unit) = div(af match {
    case AnswerField.DoubleNumberField(questionText) =>
      div(Input().value(currentAnswer))
    //      Input().value(currentText.getOrElse("").asInstanceOf[String])
    case AnswerField.IntNumberField(questionText) =>
      div(Input().value(currentAnswer))
    case AnswerField.TextField(questionText, lines) =>
      div(Input().value(currentAnswer))
    case AnswerField.ProgramInTextField(questionText, allowedLanguages, initialProgram) =>
      div(
        p(questionText), //todo innerhtml?
        diaplayProgramInput(allowedLanguages.headOption.getOrElse(ProgrammingLanguage.Java), if (currentAnswer.nonEmpty) currentAnswer else initialProgram.getOrElse(""), setCurrentAnswer)
      )
    case AnswerField.SelectOneField(questionText, variants) => div(Input().value(currentAnswer))
    case AnswerField.SelectManyField(questionText, variants) => div(Input().value(currentAnswer))
  },
    Button()("Ответить").`type`(primary).onClick(_ => submit())
  )

  def diaplayProgramInput(lang: ProgrammingLanguage, value: String, setCurrentAnswer: String => Unit) = {
    import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
    import io.circe._, io.circe.parser._
    import io.circe.generic.auto._, io.circe.syntax._
    Ace()
      .mode(js.|.from(langToAceName(lang)))
      .theme("github")
      .onChange((s, e) => setCurrentAnswer(ProgramAnswer(s, lang).asJson.noSpaces))
      .name(s"")
      .value(value)
      .build
  }


  class TableItem(val key: Int, val time: Instant, val score: Option[ProblemScore], val message: String, val review: Option[String], val answerText: String)
  def toTableItem(awd: AnswerViewData, id: Int): TableItem = new TableItem(id, awd.answeredAt, awd.score, awd.status.toString, None, awd.answerText) //todo

  def displayAnswers(a: Seq[AnswerViewData]) =
    if (a.isEmpty) div()
    else {
      import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}
      section(
        Table[TableItem]
          .bordered(true)
          //        .dataSourceVarargs(toTableItem(a.head, 1))
          .dataSourceVarargs(a.zipWithIndex.map { case (ans, i) => toTableItem(ans, i) }: _ *)
          .columnsVarargs(
            ColumnType[TableItem]()
              .setTitle("№")
              .setDataIndex("id ")
              .setKey("id")
              .setRender((_, tableItem, _) => build(p(tableItem.key))),
            ColumnType[TableItem]()
              .setTitle("Время")
              .setDataIndex("time")
              .setKey("time")
              .setRender((_, tableItem, _) => build(p(tableItem.time.toString))),
            ColumnType[TableItem]()
              .setTitle("Системное сообщение")
              .setDataIndex("message")
              .setKey("message")
              .setRender((_, tableItem, _) => build(p(tableItem.message))),
            ColumnType[TableItem]()
              .setTitle("Отзыв преподавателя")
              .setDataIndex("review")
              .setKey("review")
              .setRender((_, tableItem, _) => build(p(tableItem.review))),
            ColumnType[TableItem]()
              .setTitle("Текст ответа")
              .setDataIndex("answerText")
              .setKey("answerText")
              .setRender((_, tableItem, _) => build(p(tableItem.answerText))),
          )
      )
    }

  def input(af: AnswerField, currentText: Option[String] = None) = div(
    Input().value(currentText.getOrElse("").asInstanceOf[String]),
    Button()("Ответить")
  )
}



