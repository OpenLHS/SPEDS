package ca.griis.speds.session.internal.dispatcher.notify;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.handler.indication.HandlerRegistry;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

abstract class EntityNotifyDispatcher {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(EntityNotifyDispatcher.class);

  private final Map<MsgType, MessageHandler> handlers;

  public EntityNotifyDispatcher(HandlerRegistry registry) {
    this.handlers = new EnumMap<>(MsgType.class);

    registry.getHandlers().forEach(x -> this.handlers.put(x.getHandledType(), x));
  }

  public Optional<String> handle(ExpandedSidu expandedSidu) {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);

    Optional<String> idu = Optional.empty();
    MessageHandler handler = handlers.get(expandedSidu.msgType());
    if (handler != null) {
      idu = handler.handle(expandedSidu);
    }

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }
}
