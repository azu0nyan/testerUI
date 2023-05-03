package tester.ui.components

import otsbridge.CoursePiece
import otsbridge.CoursePiece.{Container, CoursePiece, Problem}

import scala.scalajs.js
import slinky.core._
import slinky.web.html.{b, div}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.ReactElement
import typings.antDesignIcons.components.AntdIcon
import typings.antDesignIconsSvg.esMod.CheckOutlined
import typings.antd.{antdStrings, libUtilColorsMod}
import typings.antd.antdStrings.dark
import typings.antd.components._
import typings.antd.libUtilColorsMod.PresetColorType
import typings.antd.libUtilTypeMod.LiteralUnion
import typings.react.mod.CSSProperties
import viewData.{PartialCourseViewData, ProblemRefViewData}

@react object CourseProblemSelector {


  case class Props(pcvd: PartialCourseViewData, onProblemSelected: ProblemRefViewData => Unit)

  val component = FunctionalComponent[Props] { props =>
    def problemsInPiece(cp: CoursePiece): Int = cp.allProblems.flatMap(p => props.pcvd.refByAlias(p.problemAlias)).size
    def pieceProgress(cp: CoursePiece): Int = cp.allProblems.flatMap(p => props.pcvd.refByAlias(p.problemAlias)).map(_.score.percentage).sum.toInt

    def buildFor(coursePiece: CoursePiece): ReactElement = coursePiece match {
      case container: Container => buildForContainer(container)
      case p: Problem => buildForProblem(p)
      case _ => div()
    }

    def title(c: Container) = c match {
      case CoursePiece.CourseRoot(title, annotation, childs) => title
      case CoursePiece.Theme(alias, title, textHtml, childs, displayMe) => title
      case CoursePiece.SubTheme(alias, title, textHtml, childs, displayMe) => title
    }

    def buildForContainer(c: Container): ReactElement = {
      val problemsTotal = problemsInPiece(c)
      if (problemsTotal > 0) {
        val progress = pieceProgress(c)
        val text = s"$progress/$problemsTotal"
        val (bgCol, col) = {
          if (progress == problemsTotal) (Helpers.customSuccessColor, "white")
          else if (progress == 0) (Helpers.customErrorColor, "white")
          else (Helpers.customWarningColor, "white")
        }

        SubMenu.withKey(c.alias)
          .title(Badge
            .style(CSSProperties()
              .setColor(col)
              .setBackgroundColor(bgCol)
              .setBorderWidth(0)
              .setFontWeight(typings.csstype.csstypeStrings.bolder)
              .setBoxShadow("none")
              .set("-webkit-box-shadow", "none")
            )
            .offset(js.Tuple2.apply("12px", "0px"))
            .count(text)
            .size(antdStrings.small)(Text.style(CSSProperties().setColor("white"))(s"${title(c)} ")))(
            c.childs.map(cp => buildFor(cp))
          )

      } else div()
    }

    def buildForProblem(p: Problem): ReactElement = {
      props.pcvd.refByAlias(p.alias) match {
        case Some(pref) =>
          val (bgCol, col) = {
            if (pref.score.isMax) (Helpers.customSuccessColor, "white")
            else if (pref.score.toInt == 0) (Helpers.customErrorColor, "white")
            else (Helpers.customWarningColor, "white")
          }
          if (pref.score.isMax) {
            MenuItem.withKey(p.alias)({
              Badge
                .style(CSSProperties()
                  .setColor(col)
                  .setBackgroundColor(bgCol)
                  .setBorderWidth(0)
                  .setFontWeight(typings.csstype.csstypeStrings.bolder)
                  .setBoxShadow("none")
                  .set("-webkit-box-shadow", "none")
                )
                .offset(js.Tuple2.apply("7px", "0px"))
                .count("✓")
                .size(antdStrings.small)(Text.style(CSSProperties().setColor("white"))(props.pcvd.refByAlias(p.alias).map(_.title).getOrElse(p.alias).asInstanceOf[String]))
            })

          } else {
            MenuItem.withKey(p.alias)(Text.style(CSSProperties().setColor("white"))(props.pcvd.refByAlias(p.alias).map(_.title).getOrElse(p.alias).asInstanceOf[String]))
          }
        case None => div()
      }

    }

    Menu()
      .onClick(ev => {
        props.pcvd.refByAlias(ev.key.toString) match {
          case Some(problemRef) => props.onProblemSelected(problemRef)
          case None =>
        }

        //        props.onPieceSelected(props.pcvd.courseData.findByAlias(ev.key.toString).getOrElse(props.pcvd.courseData))
      })
      .theme(dark)
      .mode(typings.rcMenu.esInterfaceMod.MenuMode.inline) /*.defaultSelectedKeys(js.Array("1"))*/ (
        props.pcvd.courseData.childs.collect { case c: Container => c }.map(buildForContainer)
      )
  }
}


