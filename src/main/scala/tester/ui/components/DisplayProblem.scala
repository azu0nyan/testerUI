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
import typings.antd.antdStrings.{horizontal, primary, topRight}
import typings.betterReactMathjax.components.MathJaxContext.configMathJax3Configundef
import typings.betterReactMathjax.components.{MathJax, MathJaxContext}
import typings.betterReactMathjax.mathJaxContextMathJaxContextMod.MathJaxContextProps
import typings.betterReactMathjax.mathJaxContextMod
import viewData.AnswerViewData
import clientRequests._
import slinky.core.facade.{React, ReactElement}
import tester.ui.Storage
import typings.antDesignIcons.components.AntdIcon
import typings.antDesignIconsSvg.esAsnDownloadOutlinedMod
import typings.reactAce.libAceMod

import java.time.Instant


@react object DisplayProblem {
  case class Props(loggedInUser: LoggedInUser, loadedData: LoadedProblemData, updateLoadedData: () => Unit)


  val component = FunctionalComponent[Props] { props =>
    //    val (currentAnswer, saveCurrentAnswer) = useState[String](props.loadedData.answerInField)
    //
    //    useEffect(() => {
    //
    //    }, Seq())

    class SetInner(val __html: String) extends js.Object

    val pvd = props.loadedData.pvd


    def submitAnswer(answer: String): Unit = {
      sendRequest(clientRequests.SubmitAnswer, clientRequests.SubmitAnswerRequest(props.loggedInUser.token, pvd.problemId, answer))(onComplete = {
        case ProblemNotFound() => Notifications.showError("Задача не найдена")
        case AlreadyVerifyingAnswer() => Notifications.showWarning("Ответ на эту задачу уже проверяется, наберитесь терпения.")
        case MaximumAttemptsLimitExceeded(attempts: Int) => Notifications.showError(s"Максимальное количество попыток $attempts превышено.")
        case AnswerSubmissionClosed(cause: Option[String]) => Notifications.showError(s"Прием ответов завершен." + cause.map(c => s"Причина: $c").getOrElse(""))
        case RequestSubmitAnswerFailure(BadToken()) => //todo
        case RequestSubmitAnswerFailure(_) => Notifications.showError(s"Ошибка 501")
        case UserCourseWithProblemNotFound() => Notifications.showError(s"Курс не найден")
        case ProblemIsNotFromUserCourse() => Notifications.showError(s"Задача не из вашего курса")
        case AnswerSubmitted(avd: AnswerViewData) => answerSubmitSuccess(avd)
      })
    }

    def answerSubmitSuccess(avd: AnswerViewData): Unit = avd match {
      case AnswerViewData(answerId, problemId, answerText, answeredAt, status) =>
        props.updateLoadedData()
    }

    div(
      h1(pvd.title),
      MathJax(div(dangerouslySetInnerHTML := new SetInner(pvd.problemHtml))),
      //score
      displayAnswerField(pvd.problemId, pvd.answerFieldType, props.loadedData.answerInField, s => submitAnswer(s)),
      displayAnswers(pvd.answers)

    )
  }


  @react object ProgramAceEditor {
    case class Props(uniqueId: String, initialValue: String, allowedLanguages: Seq[ProgrammingLanguage], submit: String => Unit)

    def langToAceName(p: ProgrammingLanguage): String = p match {
      case ProgrammingLanguage.Java => "java"
      case ProgrammingLanguage.Haskell => "haskell"
      case ProgrammingLanguage.Scala => "scala3"
      case ProgrammingLanguage.Kojo => "scala"
      case ProgrammingLanguage.Cpp => "c_cpp"
    }
    def aceNameToLang(p:String ): ProgrammingLanguage = p match {
      case  "java" => ProgrammingLanguage.Java
      case  "haskell" => ProgrammingLanguage.Haskell
      case  "scala" => ProgrammingLanguage.Kojo
      case  "scala3" => ProgrammingLanguage.Scala
      case  "c_cpp" => ProgrammingLanguage.Cpp
    }

    def langToDisplayName(p: ProgrammingLanguage): String = p match {
      case ProgrammingLanguage.Java => "java"
      case ProgrammingLanguage.Haskell => "haskell"
      case ProgrammingLanguage.Scala => "scala3"
      case ProgrammingLanguage.Kojo => "Kojo(scala)"
      case ProgrammingLanguage.Cpp => "c++"
    }

    val aceThemes = Seq(
      "monokai",
      "github",
      "tomorrow",
      "kuroir",
      "twilight",
      "xcode",
      "textmate",
      "solarized_dark",
      "solarized_light",
      "terminal")

    val component = FunctionalComponent[Props] { props =>
      import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
      import io.circe._, io.circe.parser._
      import io.circe.generic.auto._, io.circe.syntax._

      val toParse = Storage.readUserAnswer(props.uniqueId) match {
        case Some(answ) => answ
        case None => props.initialValue
      }
      val (program, lang) =
        decode[ProgramAnswer](toParse) match {
          case Left(error) => (toParse, props.allowedLanguages.headOption.getOrElse(ProgrammingLanguage.Java))
          case Right(ProgramAnswer(program, programmingLanguage)) => (program, programmingLanguage)
        }

      val (language, setLanguage) = useState[ProgrammingLanguage](lang)
      val selectLangMenu = Select[String]
        .defaultValue(langToAceName(language))
        .onChange((newVal, _) => setLanguage(aceNameToLang(newVal)))(
          props.allowedLanguages.map(lang => Select.Option(langToAceName(lang))(langToDisplayName(lang)))
        )



      val (theme, setTheme) = useState[String](Storage.getTheme())

      val selectThemeMenu = Select[String]
        .defaultValue(theme)
        .onChange((newVal, _) => {
          Storage.setTheme(newVal)
          setTheme(newVal)
        })(
          aceThemes.map(th => Select.Option(th)(th))
        )





      val aceRef = React.createRef[libAceMod.default]
      useEffect(() => {
        println(s"render $language")
        Storage.setUserAnswer(props.uniqueId, ProgramAnswer(program, language).asJson.noSpaces)
        () => {
          println("cle1an")
        }
      })



      div(

        Space.direction(horizontal)("Язык", selectLangMenu, "Тема", selectThemeMenu),
        Ace()
          .mode(js.|.from(langToAceName(language)))
          .theme(theme)
          .onChange((s, e) => Storage.setUserAnswer(props.uniqueId, ProgramAnswer(s, language).asJson.noSpaces))
          .name(props.uniqueId)
          .value(program)
          .fontSize("16")
          .withRef(aceRef)
          .build,
        Button()("Ответить").`type`(primary).onClick(_ => props.submit(Storage.readUserAnswer(props.uniqueId).getOrElse(""))) //todo
      )
    }
  }


  def displayAnswerField(uid: String, af: AnswerField, currentAnswer: String, submit: String => Unit) = div(af match {
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
        ProgramAceEditor(uid, if (currentAnswer.nonEmpty) currentAnswer else initialProgram.getOrElse(""), allowedLanguages, submit)
      )
    case AnswerField.SelectOneField(questionText, variants) => div(Input().value(currentAnswer))
    case AnswerField.SelectManyField(questionText, variants) => div(Input().value(currentAnswer))
  },

  )


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
          .dataSourceVarargs(a.zipWithIndex.reverse.map { case (ans, i) => toTableItem(ans, i) }: _ *)
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



