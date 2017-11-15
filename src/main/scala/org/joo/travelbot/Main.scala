package org.joo.travelbot

import org.joo.scorpius.Application

object Main {
  
  def main(args: Array[String]) {
      val app = new Application
      app.run(new MessengerVertxBootstrap())
  }
}