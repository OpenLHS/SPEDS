package ca.griis.speds.session.internal.dispatcher.notify;

import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.handler.indication.initiator.InitiatorEvent;
import ca.griis.speds.session.internal.handler.indication.initiator.InitiatorHandlerRegistry;

public final class InitiatorNotifyDispatcher extends EntityNotifyDispatcher {
  public InitiatorNotifyDispatcher(HostStartupContext context, InitiatorEvent initiatorEvent) {
    super(new InitiatorHandlerRegistry(context, initiatorEvent));
  }
}
