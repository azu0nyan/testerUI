package tester.ui

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DateFormat {
  val dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yy").withZone(ZoneOffset.ofHours(3)) 
  val dateFormatterDYM = DateTimeFormatter.ofPattern("dd MM yyyy").withZone(ZoneOffset.ofHours(3)) 
}
