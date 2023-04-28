package tester.ui

object Storage {

  private var answers: Map[String, String] = Map()

  def readUserAnswer(uniqueId: String): Option[String] = {
    answers.get(uniqueId)
  }

  def setUserAnswer(uniqueId: String, answer: String): Unit = {
    answers += uniqueId -> answer
  }

  private var theme: String = "github"
  def setTheme(t: String): Unit = theme = t
  def getTheme() : String = theme


  private var fontSize: Int = 14
  def setFontSize(t: Int): Unit = fontSize = t
  def getFontSize(): Int = fontSize
}
