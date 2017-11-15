package org.joo.travelbot.triggers

import org.joo.scorpius.trigger.AbstractTrigger
import org.joo.scorpius.trigger.TriggerExecutionContext
import org.joo.scorpius.support.BaseResponse

class MessengerTrigger extends AbstractTrigger {
  
  def execute(context: TriggerExecutionContext) {
    context.finish(new BaseResponse)
  }
}