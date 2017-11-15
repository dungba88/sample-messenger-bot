package org.joo.travelbot.scala

import org.joo.scorpius.Application

class Main {
  
  def main(args: Array[String]) {
      val app = new Application
      app.run(new MessengerVertxBootstrap())
  }
}