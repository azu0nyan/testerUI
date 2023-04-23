import slinky.core._
import slinky.web.ReactDOM
import slinky.web.html._
import slinky.core.annotations.react

import org.scalajs.dom._
import scala.scalajs.js


object Test {
/*
  @react class HelloName extends StatelessComponent {
    case class Props(name: String)

    def render = {
      h1(s"Hello ${props.name}")
    }
  }

  @react class MyComponent extends Component {
    type Props = Unit // no props
    case class State(buttonPresses: Int)

    def initialState = State(0)

    def render = {
      div(
        h1(s"Clicked ${state.buttonPresses} times!"),
        button(onClick := (_ => {
          setState(State(state.buttonPresses + 1))
        }))(
          "Click Me!"
        )
      )
    }
  }


  object MyComponent2 extends ComponentWrapper {
    case class Props(name: String)
    case class State(currentIndex: Int)

    class Def(jsProps: js.Object) extends Definition(jsProps) {
      def initialState = State(currentIndex = 0)

      def render = {
        div(
          h1(props.name, s"index is ${state.currentIndex}"),
          button(onClick := (_ => {
            setState(State(state.currentIndex + 1))
          }))(
            "Click Me!"
          )
        )
      }
    }

    def apply(name: String): KeyAndRefAddingStage[Def] = {
      this.apply(Props(name = name))
    }
  }


  def main(args: Array[String]): Unit = {
    println("asdsadsadaa")
    ReactDOM.render(
      div(
        HelloName(name = "Wo s dsdrld"),
        HelloName("Wosdasd s dsdrld"),
        MyComponent2(name = "sad"),
        MyComponent(),
      ),
      document.getElementById("container")
    )
  }
  
 */
}
