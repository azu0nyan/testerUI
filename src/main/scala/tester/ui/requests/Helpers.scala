package tester.ui.requests

import clientRequests.Route
import org.scalajs.dom.ext.Ajax

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Helpers {

  def host = "http://tester:8007/"

  import scala.concurrent.ExecutionContext.Implicits.global

  def sendRequest(path: String, data: String): Future[String] = Ajax.post(host + path, data).map(_.responseText)

  def sendRequest[Request, Response](template: Route[Request, Response], request: Request)(
    onComplete: Response => Unit = (r: Response) => (),
    onFailure: Throwable => Unit = _ => ()
  ): Unit =
    sendRequestFut(template, request).onComplete {
      case Success(resp) => onComplete(resp)
      case Failure(exception) => onFailure(exception)
    }

  def sendRequestFut[Request, Response](template: Route[Request, Response], request: Request): Future[Response] =
    Ajax.post(host + template.route, template.encodeRequest(request)).map(_.responseText).map(template.decodeResponse)


}
