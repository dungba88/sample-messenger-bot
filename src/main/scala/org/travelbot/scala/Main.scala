package org.travelbot.scala

import org.joo.scorpius.Application

object Main {
  
  def main(args: Array[String]) {
      val app = new Application
      app.run(new MessengerVertxBootstrap())
  }
}