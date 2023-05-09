package tester.ui.components

import DbViewsShared.CourseShared
import DbViewsShared.CourseShared.{AnswerStatus, VerifiedAwaitingConfirmation}
import clientRequests.SubmitAnswerResponse
import otsbridge.{AnswerField, ProblemScore, ProgramRunResult, ProgrammingLanguage}
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
import tester.ui.requests.Request.sendRequest
import typings.antd.components.{List => AntList, _}
import typings.antd.antdStrings.{horizontal, primary, topRight}
import typings.betterReactMathjax.components.MathJaxContext.configMathJax3Configundef
import typings.betterReactMathjax.components.{MathJax, MathJaxContext}
import typings.betterReactMathjax.mathJaxContextMathJaxContextMod.MathJaxContextProps
import typings.betterReactMathjax.mathJaxContextMod
import viewData.AnswerViewData
import clientRequests._
import otsbridge.ProgramRunResult.ProgramRunResult
import slinky.core.facade.{Fragment, React, ReactElement}
import tester.ui.components.Helpers.SetInnerHtml
import tester.ui.{DateFormat, Storage}
import typings.antDesignIcons.components.AntdIcon
import typings.antDesignIconsSvg.esAsnDownloadOutlinedMod
import typings.antd.anon.{ScrollToFirstRowOnChange, `1`}
import typings.antd.{antdBooleans, antdStrings}
import typings.csstype.mod.FloatProperty
import typings.rcTable.anon.X
import typings.react.mod.CSSProperties
import typings.reactAce.libAceMod

import java.time.Instant


@react object DisplayProblem {
  case class Props(loggedInUser: LoggedInUser, loadedData: LoadedProblemData, updateLoadedData: () => Unit)
  
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
      }, onFailure = t => {
        Notifications.showError(s"Ошибка при отправке. Проверьте интернет подключение.")
      })
    }

    def answerSubmitSuccess(avd: AnswerViewData): Unit = avd match {
      case AnswerViewData(answerId, problemId, answerText, answeredAt, status) =>
        status match {
          case VerifiedAwaitingConfirmation(score, systemMessage, verifiedAt) =>
            Notifications.renderNotificationReactElement(div(ProblemScoreDisplay(score, true, true)))
          case CourseShared.Verified(score, review, systemMessage, verifiedAt, confirmedAt) =>
            Notifications.renderNotificationReactElement(div(ProblemScoreDisplay(score, true, false)))
          case CourseShared.Rejected(systemMessage, rejectedAt) =>
            Notifications.showError(s"Отклонено. " + (if (systemMessage.nonEmpty) "Подробности в таблице ответов" else ""))
          case CourseShared.BeingVerified() =>
            Notifications.showInfo(s"Проверяется...")
          case CourseShared.VerificationDelayed(systemMessage) =>
            Notifications.showWarning(s"Проверка отложена. " + systemMessage.getOrElse(""))
        }

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
      Title().level(typings.antd.antdInts.`3`).style(CSSProperties().setMinWidth("250px"))(pvd.title),
      MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(pvd.problemHtml))),
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


  class AnswersTableItem(val key: Int, val time: Instant, val score: Option[ProblemScore], val status: AnswerStatus, awaitConfirm: Boolean,
                         val review: Option[String], val answerText: String) extends js.Object
  def toAnswersTableItem(awd: AnswerViewData, id: Int): AnswersTableItem = {
    val review = awd.status match {
      case CourseShared.Verified(score, review, systemMessage, verifiedAt, confirmedAt) => review
      case _ => None
    }

    new AnswersTableItem(id, awd.answeredAt, awd.score, awd.status, awd.status.isInstanceOf[VerifiedAwaitingConfirmation],
      review, awd.answerText) //todo
  }

  def displayOrModal(msg: String, conv: String => ReactElement, openButtonText: String, header: String, charsMax: Int = 500, newLinesMax: Int = 20): ReactElement = {
    val (modalOpen, setModalOpen) = useState[Boolean](false)
    val (modalContent, setModalContent) = useState[String]("")

    if (msg.length < charsMax && msg.count(_ == '\n') < newLinesMax) {
      conv(msg)
    } else {
      div(
        Button().`type`(primary).onClick(_ => {
          setModalContent(msg)
          setModalOpen(true)
        })(openButtonText)
        ,
        Modal()
          .width("1200px")
          .title(s"$header")
          .onOk(_ => setModalOpen(false))
          .onCancel(_ => setModalOpen(false))
          .closable(false)
          .footer(Button().`type`(primary).onClick(_ => setModalOpen(false))("Закрыть"))
          .visible(modalOpen)(
            div(style := js.Dynamic.literal(width = "fit-content"))(
              conv(msg)
            )
          )
      )

      //      Collapse(
      //        Collapse.Panel(header = header)(conv(msg))
      //      )
    }
  }


  def displayRunResultsTable(results: Seq[ProgramRunResult]) = {
    class RunResultsTableItem(val id: Int, val result: ReactElement, val message: ReactElement) extends js.Object

    def toItem(id: Int, r: ProgramRunResult): RunResultsTableItem = r match {
      case ProgramRunResult.ProgramRunResultSuccess(timeMS, message) =>
        new RunResultsTableItem(id,
          div(style := js.Dynamic.literal(color = Helpers.customSuccessColor))(s"$timeMS мс."),
          message match {
            case Some(value) if value.nonEmpty =>
              displayOrModal(value, v => pre(code(style := js.Dynamic.literal(color = Helpers.customSuccessColor), dangerouslySetInnerHTML := new SetInnerHtml(v))()),
                "Показать системное сообщение", "Системное сообщение", newLinesMax = 8)
            case _ => div("")
          }
        )
      case ProgramRunResult.ProgramRunResultWrongAnswer(message) =>
        new RunResultsTableItem(id,
          div(style := js.Dynamic.literal(color = Helpers.customErrorColor))(s"Неверный ответ."),
          message match {
            case Some(value) if value.nonEmpty =>
              Fragment(div("Неверный ответ"),
                displayOrModal(value, v => pre(code(style := js.Dynamic.literal(color = Helpers.customErrorColor), dangerouslySetInnerHTML := new SetInnerHtml(v))()),
                  "Посмотреть", "Неверный ответ", newLinesMax = 8)
              )
            case _ => div("Неверный ответ")
          }
        )
      case ProgramRunResult.ProgramRunResultFailure(message) =>
        new RunResultsTableItem(id,
          div(style := js.Dynamic.literal(color = Helpers.customErrorColor))(s"Ошибка."),
          message match {
            case Some(value) if value.nonEmpty =>
              Fragment(div("Ошибка во время исполнения"),
                displayOrModal(value, v => pre(code(style := js.Dynamic.literal(color = Helpers.customErrorColor), dangerouslySetInnerHTML := new SetInnerHtml(v))()),
                  "Посмотреть", "Ошибка во время исполнения", newLinesMax = 8)
              )
            case _ => div("Ошибка во время исполнения")
          }
        )
      case ProgramRunResult.ProgramRunResultTimeLimitExceeded(timeMs) =>
        new RunResultsTableItem(id, div(style := js.Dynamic.literal(color = Helpers.customErrorColor))(s"$timeMs мс."), div("Превышено время исполнения"))
      case ProgramRunResult.ProgramRunResultNotTested() =>
        new RunResultsTableItem(id, div(style := js.Dynamic.literal(color = Helpers.customWarningColor))("Не протестированно"), div("Программа должна пройти все предыдущие тесты"))
    }
    import typings.antd.libTableInterfaceMod.{ColumnGroupType, ColumnType}

    Table[RunResultsTableItem]
      .bordered(true)
      .dataSourceVarargs(results.zipWithIndex.map { case (r, i) => toItem(i, r) }: _ *)
      .pagination(antdBooleans.`false`)
      .size(antdStrings.small)
      //      .scroll(new ScrollSettings("", ""))
      .scroll(typings.rcTable.anon.X().setY(200).setX("").asInstanceOf[js.UndefOr[X] with ScrollToFirstRowOnChange])
      .columnsVarargs(
        ColumnType[RunResultsTableItem]()
          .setTitle("№")
          .setWidth("50px")
          .setDataIndex("id")
          .setRender((_, tableItem, _) => build(div(tableItem.id))),
        ColumnType[RunResultsTableItem]()
          .setTitle("Результат")
          .setWidth("150px")
          .setDataIndex("result")
          .setRender((_, tableItem, _) => tableItem.result),
        ColumnType[RunResultsTableItem]()
          .setTitle("Сообщение")
          .setWidth("fit-content")
          .setDataIndex("message")
          .setRender((_, tableItem, _) => tableItem.message),
      )

  }


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
      if (answer.length < 20) {
        div(pre(style := js.Dynamic.literal(maxWidth = "200px", maxHeight = "200px", overflow = "scroll"))(code(dangerouslySetInnerHTML := new SetInnerHtml(answer))))
      } else {
        div(
          pre(style := js.Dynamic.literal(maxWidth = "200px", maxHeight = "200px", overflow = "scroll"))(code(dangerouslySetInnerHTML := new SetInnerHtml(answer.take(300)))()),
          Button().`type`(primary).onClick(_ => {
            setModalContent(answer)
            setModalOpen(true)
          })("Показать весь ответ"),
          Modal()
            .width("1200px")
            .title(s"Текст ответа ${tableItem.key} - ${DateFormat.dateFormatter.format(tableItem.time)}")
            .onOk(_ => setModalOpen(false))
            .onCancel(_ => setModalOpen(false))
            .closable(false)
            .footer(Button().`type`(primary).onClick(_ => setModalOpen(false))("Закрыть"))
            .visible(modalOpen)(
              div(style := js.Dynamic.literal(width = "fit-content"))(
                pre(code(dangerouslySetInnerHTML := new SetInnerHtml(modalContent))())
              )
            )
        )
      }
    }

    def timeColumn(tableItem: AnswersTableItem) = {
      div(
        Statistic()
          .title("Отправлено в")
          .value(DateFormat.dateFormatter.format(tableItem.time))
      )

    }


    def answerStatusColumn(status: AnswerStatus) = div(status match {
      case VerifiedAwaitingConfirmation(score, systemMessage, verifiedAt) =>
        p(style := js.Dynamic.literal(color = Helpers.customWarningColor))("Ожидает подтвержденя преподавателем")
      case CourseShared.Verified(score, review, systemMessage, verifiedAt, confirmedAt) =>
        score match {
          case ProblemScore.MultipleRunsResultScore(runResults) =>
            div(
              div(style := js.Dynamic.literal(maxWidth = "300px", margin = "5px"))(ProblemScoreDisplay(score, true, false)),
              displayRunResultsTable(runResults)
            )
          case score => div(style := js.Dynamic.literal(maxWidth = "300px", margin = "5px"))(ProblemScoreDisplay(score, true, false))
        }
      case CourseShared.Rejected(systemMessage, rejectedAt) =>
        systemMessage match {
          case Some(value) =>
            div(
              p(style := js.Dynamic.literal(color = Helpers.customErrorColor))("Отклонено"), {
                val fullMessage = pre(code(style := js.Dynamic.literal(color = Helpers.customErrorColor), dangerouslySetInnerHTML := new SetInnerHtml(value))())
                if (value.length < 500 && value.count(_ == '\n') < 20) {
                  fullMessage
                } else {
                  Collapse(
                    Collapse.Panel(header = "Показать системное сообщение.")(fullMessage)
                  )
                }
              }
            )
          case None => (p(style := js.Dynamic.literal(color = Helpers.customErrorColor))("Отклонено"))
        }
      case CourseShared.BeingVerified() =>
        p(style := js.Dynamic.literal(color = Helpers.customWarningColor))("Проверяется")
      case CourseShared.VerificationDelayed(systemMessage) =>
        p(style := js.Dynamic.literal(color = Helpers.customWarningColor))("Проверка отложена. " + systemMessage.getOrElse(""))
    })

    def systemMessageColumn(tableItem: AnswersTableItem) = {

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
              .setTitle("Статус")
              .setDataIndex("status")
              .setKey("status")
              .setRender((_, tableItem, _) => build(answerStatusColumn(tableItem.status))),
            ColumnType[AnswersTableItem]()
              .setTitle("Отзыв преподавателя")
              .setDataIndex("review")
              .setKey("review")
              .setRender((_, tableItem, _) => build(p(tableItem.review.getOrElse("").asInstanceOf[String]))),
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



