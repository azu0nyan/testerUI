package tester.ui.components

import typings.csstype.mod.PositionProperty.fixed
import typings.react.mod.CSSProperties

import scala.scalajs.js

object Helpers {

  /**Для использования с dangerouslySetInnerHTML := new SetInnerHtml(value) */
  class SetInnerHtml(val __html: String) extends js.Object
  
  def headerHeight  = 30
  
  def fixedLeftSiderStyle = CSSProperties()
    .setOverflow("auto")
    .setHeight("100vh")
    .setPosition(fixed)
    .setLeft(0)    
    .setTop(headerHeight)
    .setBottom(0)
}
