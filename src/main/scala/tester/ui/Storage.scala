package tester.ui

object Storage {

  private var answers: Map[String, String] = Map()

  def readUserAnswer(uniqueId: String): Option[String] = {
    answers.get(uniqueId)
  }

  def setUserAnswer(uniqueId: String, answer: String): Unit = {
    answers += uniqueId -> answer
  }
}
