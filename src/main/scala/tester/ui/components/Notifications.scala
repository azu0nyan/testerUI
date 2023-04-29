package tester.ui.components

import org.scalajs.dom.Notification
import slinky.core.facade.ReactElement

object Notifications {

  import typings.antd.libNotificationMod.{ArgsProps, IconType, default => Notification}
  def showError(message: String) = renderNotification(message, IconType.error)
  def showWarning(message: String) = renderNotification(message, IconType.warning)
  def showInfo(message: String) = renderNotification(message, IconType.info)
  def showSuccess(message: String) = renderNotification(message, IconType.success)
  def renderNotification(message: String, iconType: IconType) = {
    Notification.open(ArgsProps(message = message).setType(iconType))
  }

  def renderNotificationReactElement(message: ReactElement, iconType: IconType = IconType.info) = {
    Notification.open(ArgsProps(message = message).setType(iconType))
  }
}
