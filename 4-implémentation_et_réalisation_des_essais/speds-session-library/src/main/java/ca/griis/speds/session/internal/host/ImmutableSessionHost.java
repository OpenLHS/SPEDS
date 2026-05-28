/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableSessionHost.
 * @brief @~english Implementation of the ImmutableSessionHost class.
 */

package ca.griis.speds.session.internal.host;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.dispatcher.SessionNotifyDispatcher;
import ca.griis.speds.session.internal.dispatcher.SessionSubmitDispatcher;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.handler.request.SesDelegateRequestHandler;
import ca.griis.speds.session.internal.handler.response.SesTransferResponseHandler;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Offre les services d'un hôte immutable de la couche session.
 * @par Details
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableSessionHost implements SessionHost, TransportHostEvent {
  private static final GriisLogger logger = getLogger(ImmutableSessionHost.class);

  private final HostStartupContext initiatorContext;
  private final HostStartupContext peerContext;
  private final SessionHostEvent hostEventConsumer;
  private final SessionSubmitDispatcher submitDispatcher;
  private final SessionNotifyDispatcher notifyDispatcher;

  public ImmutableSessionHost(
      HostStartupContext initiatorContext,
      HostStartupContext peerContext,
      SessionHostEvent hostEventConsumer) {
    this.initiatorContext = initiatorContext;
    this.peerContext = peerContext;
    this.hostEventConsumer = hostEventConsumer;

    final var requestHandler = new SesDelegateRequestHandler(initiatorContext);
    final var responseHandler = new SesTransferResponseHandler(peerContext);
    this.submitDispatcher = new SessionSubmitDispatcher(requestHandler, responseHandler);
    this.notifyDispatcher =
        new SessionNotifyDispatcher(initiatorContext, peerContext, requestHandler);
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    initiatorContext.cleanUp();
    peerContext.cleanUp();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void closePreservingSessionStates() {
    logger.trace(Trace.ENTER_METHOD_0);

    initiatorContext.closePreservingSessionStates();
    peerContext.closePreservingSessionStates();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void clearSessionStates() {
    logger.trace(Trace.ENTER_METHOD_0);

    initiatorContext.clearSessionStates();
    peerContext.clearSessionStates();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public CompletableFuture<Optional<String>> submitIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    var future = submitDispatcher.handle(idu);

    logger.trace(Trace.EXIT_METHOD_1, "future", future);
    return future;
  }

  @Override
  public void notifyIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      final var mapper = SharedObjectMapper.getInstance().getMapper();
      final var nidu = mapper.readValue(idu, Sidu.class);
      var result = notifyDispatcher.handle(nidu);
      if (result.isPresent()) {
        hostEventConsumer.notifyIdu(result.get());
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyException(Exception exception) {

  }
}
