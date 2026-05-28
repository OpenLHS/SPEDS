package ca.griis.speds.session.internal.dispatcher;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.dispatcher.notify.InitiatorNotifyDispatcher;
import ca.griis.speds.session.internal.dispatcher.notify.PeerNotifyDispatcher;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.handler.indication.initiator.InitiatorEvent;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;

public final class SessionNotifyDispatcher {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SessionNotifyDispatcher.class);

  private final InitiatorNotifyDispatcher initiatorNotifyDispatcher;
  private final PeerNotifyDispatcher peerNotifyDispatcher;

  public SessionNotifyDispatcher(
      HostStartupContext initiatorContext,
      HostStartupContext peerContext,
      InitiatorEvent initiatorEvent) {
    this.initiatorNotifyDispatcher =
        new InitiatorNotifyDispatcher(initiatorContext, initiatorEvent);
    this.peerNotifyDispatcher = new PeerNotifyDispatcher(peerContext);
  }

  public Optional<String> handle(Sidu sidu) {
    logger.trace(Trace.ENTER_METHOD_1, "sidu", sidu);

    Optional<String> pidu = Optional.empty();
    try {
      final var context = sidu.getContext();

      if (context.getService() != Context34Dto.Service.TRANSFER) {
        final var ex = new RuntimeException("FAILED: Unknown service");
        logger.error(Error.IGNORED_ERROR, ex);
      } else if (context.getServicePrimitive() == ServicePrimitive.INDICATION) {
        final var mapper = SharedObjectMapper.getInstance().getMapper();
        final var pdu = mapper.readValue(sidu.getMessage(), Spdu.class);
        final var msgType = MsgType.from(pdu.getHeader().getMsgtype());
        final var expandedSidu = new ExpandedSidu(sidu, pdu, msgType);

        pidu = peerNotifyDispatcher.handle(expandedSidu);
        if (pidu.isEmpty()) {
          pidu = initiatorNotifyDispatcher.handle(expandedSidu);
        }
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "pidu", pidu);
    return pidu;
  }
}
