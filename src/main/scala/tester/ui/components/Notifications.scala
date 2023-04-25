package tester.ui.components

import org.scalajs.dom.Notification

object Notifications {

  import typings.antd.libNotificationMod.{ArgsProps, IconType, default => Notification}
  def showError(message: String) = renderNotification(message, IconType.error)
  def showSuccess(message: String) = renderNotification(message, IconType.success)
  def renderNotification(message: String, iconType: IconType) = {
    Notification.open(ArgsProps(message = message).setType(iconType))
  }
}
