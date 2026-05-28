package ca.griis.speds.session.internal.dispatcher.submit;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.dispatcher.SessionSubmitDispatcher;
import ca.griis.speds.session.internal.handler.response.SesTransferResponseHandler;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class PeerSubmitDispatcher {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SessionSubmitDispatcher.class);

  private final SesTransferResponseHandler handler;

  public PeerSubmitDispatcher(SesTransferResponseHandler handler) {
    this.handler = handler;
  }

  public CompletableFuture<Optional<String>> handle(Pidu pidu) {
    logger.trace(Trace.ENTER_METHOD_1, "pidu", pidu);

    handler.handle(pidu);
    CompletableFuture<Optional<String>> tidu = CompletableFuture.completedFuture(Optional.empty());

    logger.trace(Trace.EXIT_METHOD_1, "tidu", tidu);
    return tidu;
  }
}
