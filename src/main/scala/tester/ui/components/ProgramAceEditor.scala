package tester.ui.components

import otsbridge.AnswerField.ProgramAnswer
import otsbridge.ProgrammingLanguage.ProgrammingLanguage
import otsbridge.ProgrammingLanguage
import typings.antd.components.Select

import scala.scalajs.js
import slinky.core._
import slinky.web.html.{code, _}
import slinky.core.annotations.react
import slinky.core.facade.Hooks.{useEffect, useState}
import slinky.core.facade.React
import tester.ui.Storage
import typings.antd.components.{List => AntList, _}
import tester.ui.components.DisplayPartialCourse.LoadedProblemData
import typings.react.mod.CSSProperties
import typings.reactAce.libAceMod
import typings.reactAce.components.{Ace, ReactAce}
import typings.reactAce.libAceMod.IAceEditorProps
import typings.antd.antdStrings.{horizontal, primary, topRight}

@react object ProgramAceEditor {
  case class Props(uniqueId: String, initialValue: String, allowedLanguages: Seq[ProgrammingLanguage], submit: String => Unit)

  def langToAceName(p: ProgrammingLanguage): String = p match {
    case ProgrammingLanguage.Java => "java"
    case ProgrammingLanguage.Haskell => "haskell"
    case ProgrammingLanguage.Scala => "scala3"
    case ProgrammingLanguage.Kojo => "scala"
    case ProgrammingLanguage.Cpp => "c_cpp"
  }
  def aceNameToLang(p: String): ProgrammingLanguage = p match {
    case "java" => ProgrammingLanguage.Java
    case "haskell" => ProgrammingLanguage.Haskell
    case "scala" => ProgrammingLanguage.Kojo
    case "scala3" => ProgrammingLanguage.Scala
    case "c_cpp" => ProgrammingLanguage.Cpp
  }

  def langToDisplayName(p: ProgrammingLanguage): String = p match {
    case ProgrammingLanguage.Java => "java"
    case ProgrammingLanguage.Haskell => "haskell"
    case ProgrammingLanguage.Scala => "scala3"
    case ProgrammingLanguage.Kojo => "Kojo(scala)"
    case ProgrammingLanguage.Cpp => "c++"
  }

  val aceThemes = Seq(
    "ambiance",
    "chaos",
    "chrome",
    "cloud9_day",
    "cloud9_night",
    "clouds",
    "cobalt",
    "crimson_editor",
    "dawn",
    "dracula",
    "dreamweaver",
    "eclipse",
    "github",
    "gob",
    "gruvbox",
    "kuroir",
    "mono_industrial",
    "monokai",
    "nord_dark",
    "one_dark",
    "pastel_on_dark",
    "solarized_dark",
    "solarized_light",
    "sqlserver",
    "terminal",
    "textmate",
    "tomorrow",
  )

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
      .style(CSSProperties().setWidth("150px"))
      .onChange((newVal, _) => setLanguage(aceNameToLang(newVal)))(
        props.allowedLanguages.map(lang => Select.Option(langToAceName(lang))(langToDisplayName(lang)))
      )


    val (theme, setTheme) = useState[String](Storage.getTheme())

    val selectThemeMenu = Select[String]
      .defaultValue(theme)
      .style(CSSProperties().setWidth("150px"))
      .onChange((newVal, _) => {
        Storage.setTheme(newVal)
        setTheme(newVal)
      })(
        aceThemes.map(th => Select.Option(th)(th))
      )

    val (fontSize, setFontSize) = useState[Int](Storage.getFontSize())
    val fontSizes = Seq(8, 10, 12, 14, 16, 20, 24, 32, 48, 72)
    val selectFontSize = Select[Int]
      .defaultValue(fontSize)
      .style(CSSProperties().setWidth("75px"))
      .onChange((newVal, _) => {
        Storage.setFontSize(newVal)
        setFontSize(newVal)
      })(
        fontSizes.map(th => Select.Option(th)(th))
      )


    val aceRef = React.createRef[libAceMod.default]
    useEffect(() => {
      Storage.setUserAnswer(props.uniqueId, ProgramAnswer(program, language).asJson.noSpaces)
    })


    div(

      Space
        .direction(horizontal)
        .style(CSSProperties().setPadding(5))
        ("Язык", selectLangMenu, "Тема", selectThemeMenu, "Размер", selectFontSize),
      Ace()
        .style(CSSProperties().setWidth("100%"))
        .mode(js.|.from(langToAceName(language)))
        .minLines(20)
        .maxLines(200)
        .enableBasicAutocompletion(true)
        .enableLiveAutocompletion(true)
        .theme(theme)
        .onChange((s, e) => Storage.setUserAnswer(props.uniqueId, ProgramAnswer(s, language).asJson.noSpaces))
        .name(props.uniqueId)
        .value(program)
        .fontSize(fontSize)
        .withRef(aceRef)
        .build,
      Button()("Ответить").`type`(primary).onClick(_ => props.submit(Storage.readUserAnswer(props.uniqueId).getOrElse(""))) //todo
    )
  }
}



