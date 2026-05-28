package ca.griis.speds.session.internal.dispatcher;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.dispatcher.submit.InitiatorSubmitDispatcher;
import ca.griis.speds.session.internal.dispatcher.submit.PeerSubmitDispatcher;
import ca.griis.speds.session.internal.handler.request.SesDelegateRequestHandler;
import ca.griis.speds.session.internal.handler.response.SesTransferResponseHandler;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SessionSubmitDispatcher {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SessionSubmitDispatcher.class);

  private final InitiatorSubmitDispatcher initiatorSubmitDispatcher;
  private final PeerSubmitDispatcher peerSubmitDispatcher;

  public SessionSubmitDispatcher(SesDelegateRequestHandler requestHandler,
      SesTransferResponseHandler responseHandler) {
    this.initiatorSubmitDispatcher = new InitiatorSubmitDispatcher(requestHandler);
    this.peerSubmitDispatcher = new PeerSubmitDispatcher(responseHandler);
  }

  public CompletableFuture<Optional<String>> handle(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    CompletableFuture<Optional<String>> pidu = CompletableFuture.completedFuture(Optional.empty());
    final var mapper = SharedObjectMapper.getInstance().getMapper();

    try {
      final var tsdu = mapper.readValue(idu, Pidu.class);
      final var context = tsdu.getContext();

      if (context.getServicePrimitive() == ServicePrimitive.REQUEST
          && context.getService() == Context23Dto.Service.DELEGATE) {
        pidu = initiatorSubmitDispatcher.handle(tsdu);
      } else if (context.getServicePrimitive() == ServicePrimitive.RESPONSE
          && context.getService() == Context23Dto.Service.TRANSFER) {
        pidu = peerSubmitDispatcher.handle(tsdu);
      } else {
        final String failed = "FAILED: This service primitive is not supported. Details: "
            + context.getService() + ", " + context.getServicePrimitive();
        final var ex = new RuntimeException(failed);
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (Exception e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "pidu", pidu);
    return pidu;
  }
}
