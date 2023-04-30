package tester.ui.components


import otsbridge.{CoursePiece, DisplayMe}
import otsbridge.CoursePiece.CoursePiece

import scala.scalajs.js
import slinky.core._
import slinky.web.html._
import typings.antd.components._
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import tester.ui.components.Helpers.SetInnerHtml
import typings.antd.{antdInts, antdStrings}
import typings.react.mod.CSSProperties
import viewData.{PartialCourseViewData, ProblemRefViewData}

@react object CourseText {
  case class Props(partialCourse: PartialCourseViewData, selectedPiece: CoursePiece, setSelectedProblem: ProblemRefViewData => Unit, setSelectedCoursePiece: CoursePiece => Unit)


  def cont(r: ReactElement) = div(style := js.Dynamic.literal(
    width = "-webkit-fill-available",
    display = "flex",
    justifyContent = "center"
  ))(
    Card().style(CSSProperties().setMinWidth("400px").setMaxWidth("900px").setMargin(20).setPadding(5))(
      r
    )
  )


  val component = FunctionalComponent[Props] { props =>

    def genLinkFor(c: CoursePiece) : ReactElement = c match {
      case container: CoursePiece.Container => container match {
        case CoursePiece.CourseRoot(title, annotation, childs) =>
          Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))(title)
        case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) =>
          Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))(title)
        case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) =>
          Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))(title)
      }
      case CoursePiece.HtmlToDisplay(alias, displayMe, htmlRaw) =>
        Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Перейти к тексту")
      case CoursePiece.TextWithHeading(alias, heading, bodyHtml, displayMe, displayInContentsHtml) =>
        Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))(heading)
      case CoursePiece.Paragraph(alias, bodyHtml, displayMe) =>
        Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedCoursePiece(c))("Перейти к тексту")
      case CoursePiece.Problem(problemAlias, displayMe, displayInContentsHtml) =>
        props.partialCourse.refByAlias(problemAlias) match {
          case Some(pref) => div(
            Title().level(antdInts.`4`)(s"Задача ${pref.title}", ProblemScoreDisplay(pref.score, true, false)), //todo load data
            Button().`type`(antdStrings.primary).onClick(_ => props.setSelectedProblem(pref))("К условию")
          )
          case None => div(s"Задача $problemAlias не найдена, сначала учитель должен добавить её в курс.")
        }
    }

    def genForChilds(childs: Seq[CoursePiece]): ReactElement = {
      div(
        childs.map(f => f.displayMe match {
          case DisplayMe.OwnPage => genLinkFor(f)
          case DisplayMe.Inline => matchPiece(f)
        }
      ) : _ *)
    }

    //todo add next|prev buttons
    def matchPiece(c: CoursePiece): ReactElement = {
      import typings.betterReactMathjax.components.{MathJax, MathJaxContext}
      c match {
        case container: CoursePiece.Container =>
          container match {
            case CoursePiece.CourseRoot(title, annotation, childs) =>
              div(
                Title().level(antdInts.`2`)(title),
                MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(annotation))),
                genForChilds(childs)
              )
            case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) =>
              div(
                Title().level(antdInts.`3`)(title),
                MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(textHtml))),
                genForChilds(childs)
              )
            case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) =>
              div(
                Title().level(antdInts.`4`)(title),
                MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(textHtml))),
                genForChilds(childs)
              )
          }
        case CoursePiece.HtmlToDisplay(alias, displayMe, htmlRaw) =>
          MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(htmlRaw)))
        case CoursePiece.TextWithHeading(alias, heading, bodyHtml, displayMe, displayInContentsHtml) =>
          div(
            Title().level(antdInts.`4`)(heading),
            MathJax(div(dangerouslySetInnerHTML := new SetInnerHtml(bodyHtml)))
          )
        case CoursePiece.Paragraph(alias, bodyHtml, displayMe) =>
          MathJax(p(dangerouslySetInnerHTML := new SetInnerHtml(bodyHtml)))
        case cp@CoursePiece.Problem(problemAlias, displayMe, displayInContentsHtml) => //todo use display me
          genLinkFor(cp)
      }
    }
    cont(matchPiece(props.selectedPiece))
  }
}

