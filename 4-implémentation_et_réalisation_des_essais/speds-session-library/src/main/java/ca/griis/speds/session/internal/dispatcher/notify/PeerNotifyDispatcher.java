package ca.griis.speds.session.internal.dispatcher.notify;

import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.handler.indication.peer.PeerHandlerRegistry;

public final class PeerNotifyDispatcher extends EntityNotifyDispatcher {
  public PeerNotifyDispatcher(HostStartupContext context) {
    super(new PeerHandlerRegistry(context));
  }
}
