package ca.griis.speds.session.internal.dispatcher.submit;

import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.handler.request.SesDelegateRequestHandler;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class InitiatorSubmitDispatcher {
  private final SesDelegateRequestHandler handler;

  public InitiatorSubmitDispatcher(SesDelegateRequestHandler handler) {
    this.handler = handler;
  }

  public CompletableFuture<Optional<String>> handle(Pidu pidu) {
    CompletableFuture<Optional<String>> idu =
        CompletableFuture.completedFuture(handler.handle(pidu));
    return idu;
  }
}
