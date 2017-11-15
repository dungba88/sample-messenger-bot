package org.joo.travelbot

import org.joo.scorpius.support.vertx.VertxBootstrap
import io.vertx.core.VertxOptions
import org.joo.scorpius.trigger.handle.disruptor.DisruptorHandlingStrategy
import java.util.concurrent.Executors
import com.lmax.disruptor.dsl.ProducerType
import com.lmax.disruptor.YieldingWaitStrategy
import org.joo.travelbot.triggers.MessengerTrigger

class MessengerVertxBootstrap extends VertxBootstrap {
  
  def run() {
    configureTriggers()
    
    val options = new VertxOptions().setEventLoopPoolSize(8);
    configureServer(options)
  }

  def configureTriggers() {
    triggerManager.setHandlingStrategy(new DisruptorHandlingStrategy(1024, Executors.newFixedThreadPool(3), ProducerType.MULTI, new YieldingWaitStrategy()))
    triggerManager.registerTrigger("fb_msg").withAction(new MessengerTrigger())
  }
}