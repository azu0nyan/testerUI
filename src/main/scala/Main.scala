import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react
import org.scalajs.dom._

import java.time.Instant
import scala.scalajs.js

import viewData._

object Main {



  def main(args: Array[String]): Unit = {   

    val vd = UserViewData("sd","sa", None, None, None, Seq(), "noob", Instant.now())
    ReactDOM.render(
      div(
        s"$vd"
      ),
      document.getElementById("container")
    )
  }
}
