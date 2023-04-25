package tester.ui.components

import otsbridge.AnswerField.AnswerField
import otsbridge.ProblemScore.ProblemScore
import slinky.core.WithAttrs.build

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import tester.ui.components.DisplayPartialCourse.LoadedProblemData
import typings.antd.components.{List => AntList, _}
import typings.reactAce.components.{Ace, ReactAce}
import typings.reactAce.libAceMod.IAceEditorProps
import typings.aceBuilds.aceBuildsStrings.theme
import viewData.AnswerViewData

import java.time.Instant


@react object DisplayProblem {
  case class Props(loggedInUser: LoggedInUser, loadedData: LoadedProblemData)

  val component = FunctionalComponent[Props] { props =>
    //    val (name, setName) = useState[T](t)
    useEffect(() => {})

    class SetInner(val __html: String) extends js.Object

    val pvd = props.loadedData.pvd

    div(
      h1(pvd.title),
      //score
      div(dangerouslySetInnerHTML := new SetInner(pvd.problemHtml)),
      input(pvd.answerFieldType, scala.Option.when(props.loadedData.answerInField.nonEmpty)(props.loadedData.answerInField)),
      diaplayProgramInput(),
      displayAnswers(pvd.answers)

    )
  }

  def diaplayProgramInput() = Ace().mode(js.|.from("java")).theme("github")
    .onChange((s, e) => println(s + " " + e.toString))
    .name(s"")
    .value("init")
    .build


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
          .dataSourceVarargs(a.zipWithIndex.map{case (ans, i) => toTableItem(ans, i)} : _ *)
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



