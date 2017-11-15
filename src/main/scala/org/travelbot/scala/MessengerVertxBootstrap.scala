package org.travelbot.scala

import java.util.concurrent.Executors

import org.joo.scorpius.support.vertx.VertxBootstrap
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy
import org.travelbot.scala.controllers.MessengerChallengeController

import com.lmax.disruptor.YieldingWaitStrategy
import com.lmax.disruptor.dsl.ProducerType

import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.ext.web.Router

class MessengerVertxBootstrap extends VertxBootstrap {
  
  def run() {
    configureTriggers()
    
    val options = new VertxOptions().setEventLoopPoolSize(8);
    configureServer(options, 9090)
  }
  
  override def configureRoutes(vertx: Vertx): Router = {
    val router = super.configureRoutes(vertx)
    router.get("/fb_msg_hook").handler(new MessengerChallengeController().handle)
    return router
  }
  
  def configureTriggers() {
    triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, Executors.newFixedThreadPool(3), ProducerType.MULTI, new YieldingWaitStrategy()))
  }
}