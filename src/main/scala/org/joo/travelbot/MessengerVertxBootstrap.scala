package org.joo.travelbot

import org.joo.scorpius.support.vertx.VertxBootstrap
import io.vertx.core.VertxOptions
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy
import java.util.concurrent.Executors
import com.lmax.disruptor.dsl.ProducerType
import com.lmax.disruptor.YieldingWaitStrategy
import io.vertx.ext.web.Router
import io.vertx.core.Vertx
import org.joo.travelbot.controllers.MessengerChallengeController

object MessengerVertxBootstrap extends VertxBootstrap {
  
  def run() {
    configureTriggers()
    
    val options = new VertxOptions().setEventLoopPoolSize(8);
    configureServer(options)
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