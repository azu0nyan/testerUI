package tester.ui.components

import DbViewsShared.CourseShared.VerifiedAwaitingConfirmation
import clientRequests.SubmitAnswerResponse
import otsbridge.{AnswerField, ProgrammingLanguage}
import otsbridge.AnswerField.{AnswerField, ProgramAnswer}
import otsbridge.ProblemScore.ProblemScore
import otsbridge.ProgrammingLanguage.ProgrammingLanguage
import slinky.core.WithAttrs.build

import scala.scalajs.js
import slinky.core._
import slinky.web.html.{code, _}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import tester.ui.components.DisplayPartialCourse.LoadedProblemData
import tester.ui.requests.Helpers.sendRequest
import typings.antd.components.{List => AntList, _}
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
import typings.antd.anon.`1`
import typings.antd.antdStrings
import typings.csstype.mod.FloatProperty
import typings.react.mod.CSSProperties
import typings.reactAce.libAceMod

import java.time.Instant


@react object DisplayProblem {
  case class Props(loggedInUser: LoggedInUser, loadedData: LoadedProblemData, updateLoadedData: () => Unit)
  class SetInner(val __html: String) extends js.Object

  val component = FunctionalComponent[Props] { props =>
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
    val problemScoreCard = Card()
      .style(CSSProperties()
        .setFloat(FloatProperty.right)
        .setWidth(150)
        .setPadding(5)
        .setMargin(20)
        .setDisplay(js.|.from("inline"))
      )(ProblemScoreDisplay(pvd.score, pvd.answers.nonEmpty, pvd.answers.exists(_.status.isInstanceOf[VerifiedAwaitingConfirmation])))

    //
    val problemDescription = div(
      problemScoreCard,
      Title().level(typings.antd.antdInts.`3`) .style(CSSProperties().setMinWidth("250px"))(pvd.title),
      MathJax(div(dangerouslySetInnerHTML := new SetInner(pvd.problemHtml))),
    )
    div(style := js.Dynamic.literal(
      width = "-webkit-fill-available"
    ))(
      Row().wrap(true)(
        Col()
          .flex("1 1 300px")(
            Card().style(CSSProperties().setMinWidth("400px").setMaxWidth("900px").setMargin(20).setPadding(5))(
              problemDescription
            )
          ),
        Col()
          .flex("1 1 500px")(
            Card().style(CSSProperties().setMinWidth("500px").setMaxWidth("1600px").setMargin(20).setPadding(5))(
              displayAnswerField(pvd.problemId, pvd.answerFieldType, props.loadedData.answerInField, s => submitAnswer(s)),
            )
          )
      ),
      Row(
        Col()
          .flex("auto")(
            Card().style(CSSProperties().setMargin(20).setPadding(5))(
              displayAnswers(pvd.answers)
            )
          )
      )
    )
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
      div(style := CSSProperties())(
        if (questionText.nonEmpty) p(questionText) else "", //todo innerhtml?
        ProgramAceEditor(uid, if (currentAnswer.nonEmpty) currentAnswer else initialProgram.getOrElse(""), allowedLanguages, submit)
      )
    case AnswerField.SelectOneField(questionText, variants) => div(Input().value(currentAnswer))
    case AnswerField.SelectManyField(questionText, variants) => div(Input().value(currentAnswer))
  },

  )


  class AnswersTableItem(val key: Int, val time: Instant, val score: Option[ProblemScore], val message: String, awaitConfirm: Boolean, val review: Option[String], val answerText: String)
  def toAnswersTableItem(awd: AnswerViewData, id: Int): AnswersTableItem = new AnswersTableItem(id, awd.answeredAt, awd.score, awd.status.toString, awd.status.isInstanceOf[VerifiedAwaitingConfirmation], None, awd.answerText) //todo


  def displayAnswers(a: Seq[AnswerViewData]): WithAttrs[_ >: div.tag.type with section.tag.type <: TagElement] = {
    def answerColumn(tableItem: AnswersTableItem): WithAttrs[_ >: div.tag.type with section.tag.type <: TagElement] = {
      val (modalOpen, setModalOpen) = useState[Boolean](false)
      val (modalContent, setModalContent) = useState[String]("")
      import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
      import io.circe._, io.circe.parser._
      import io.circe.generic.auto._, io.circe.syntax._
      val answer = decode[ProgramAnswer](tableItem.answerText) match {
        case Left(value) =>
          tableItem.answerText
        case Right(ProgramAnswer(prog, lang)) => prog
      }
      if (answer.length < 300) {
        div(pre(code(dangerouslySetInnerHTML := new SetInner(answer))))
      } else {
        div(
          pre(code(dangerouslySetInnerHTML := new SetInner(answer.take(300)))()),
          Button().`type`(primary).onClick(_ => {
            setModalContent(answer)
            setModalOpen(true)
          })("Показать"),
          Modal()
            .title(s"Текст ответа ${tableItem.key}")
            .onOk(_ => setModalOpen(false))
            .onCancel(_ => setModalOpen(false))
            .closable(false)
            .footer(Button().`type`(primary).onClick(_ => setModalOpen(false))("Закрыть"))
            .visible(modalOpen)(
              pre(code(dangerouslySetInnerHTML := new SetInner(modalContent))())
            )
        )
      }
    }

    def timeColumn(tableItem: AnswersTableItem) = {

      div()
    }
    if (a.isEmpty) div()
    else {
      import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}
      section(
        Table[AnswersTableItem]
          .bordered(true)
          //        .dataSourceVarargs(toTableItem(a.head, 1))
          .dataSourceVarargs(a.zipWithIndex.reverse.map { case (ans, i) => toAnswersTableItem(ans, i) }: _ *)
          .columnsVarargs(
            ColumnType[AnswersTableItem]()
              .setTitle("№")
              .setDataIndex("id ")
              .setKey("id")
              .setRender((_, tableItem, _) => build(p(tableItem.key))),
            ColumnType[AnswersTableItem]()
              .setTitle("Время")
              .setDataIndex("time")
              .setKey("time")
              .setRender((_, tableItem, _) => build(timeColumn(tableItem))),
            ColumnType[AnswersTableItem]()
              .setTitle("Системное сообщение")
              .setDataIndex("message")
              .setKey("message")
              .setRender((_, tableItem, _) => build(p(tableItem.message))),
            ColumnType[AnswersTableItem]()
              .setTitle("Отзыв преподавателя")
              .setDataIndex("review")
              .setKey("review")
              .setRender((_, tableItem, _) => build(p(tableItem.review))),
            ColumnType[AnswersTableItem]()
              .setTitle("Ответ")
              .setDataIndex("answerText")
              .setKey("answerText")
              .setRender((_, tableItem, _) => build(answerColumn(tableItem))),
          )
      )
    }
  }


}



