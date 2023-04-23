package tester.ui

import org.scalajs.dom._
import slinky.web.ReactDOM
import slinky.web.html._
import viewData._

import java.time.Instant
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Main {






  def main(args: Array[String]): Unit = {
    IndexCSS
    val vd = UserViewData("sd","sa", None, None, None, Seq(), "noob", Instant.now())
    ReactDOM.render(
      div(
        components.Application(components.Application.Props())
      ),
      document.getElementById("container")
    )
  }
}


@JSImport("./index.css", JSImport.Namespace)
@js.native
object IndexCSS extends js.Object
