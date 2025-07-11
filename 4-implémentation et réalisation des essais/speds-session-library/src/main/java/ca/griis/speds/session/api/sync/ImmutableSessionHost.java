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

package ca.griis.speds.session.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.processing.MessageDispatcher;
import ca.griis.speds.session.internal.processing.Poller;
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
public final class ImmutableSessionHost implements SessionHost {
  private static final GriisLogger logger = getLogger(ImmutableSessionHost.class);

  private final ImmutableSessionClient client;
  private final ImmutableSessionServer server;
  private final Poller poller;

  ImmutableSessionHost(HostStartupContext clientContext,
      HostStartupContext serverContext) {
    logger.trace(Trace.ENTER_METHOD_1, "clientContext", clientContext);
    MessageDispatcher messageDispatcher = new MessageDispatcher(clientContext.sharedMapper());
    poller = new Poller(clientContext.transportHost(), messageDispatcher,
        clientContext.serverPollingInterval());
    // todo - mettre dans le start quand il y en aura un vrai
    poller.start();
    this.client = new ImmutableSessionClient(clientContext, poller);
    this.server = new ImmutableSessionServer(serverContext, poller);
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    client.close();
    server.close();
    poller.stop();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void closePreservingSessionStates() {
    logger.trace(Trace.ENTER_METHOD_0);

    client.closePreservingSessionStates();
    server.closePreservingSessionStates();
    poller.stop();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void clearSessionStates() {
    logger.trace(Trace.ENTER_METHOD_0);

    client.clearSessionStates();
    server.clearSessionStates();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void request(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    client.request(idu);
  }

  @Override
  public CompletableFuture<Void> requestFuture(String idu) {
    return client.requestFuture(idu);
  }

  @Override
  public String confirm() {
    logger.trace(Trace.ENTER_METHOD_0);
    final String result = client.confirm();

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  @Override
  public String indicateDataExchange() {
    final String result = indication();
    return result;
  }

  @Override
  public String indication() {
    logger.trace(Trace.ENTER_METHOD_0);
    final String result = server.indication();

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  @Override
  public void response(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    server.response(idu);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public Exception getException() {
    logger.trace(Trace.ENTER_METHOD_0);

    Exception lastThrown;

    try {
      lastThrown = poller.getException();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new RuntimeException(e);
    }

    if (lastThrown != null) {
      poller.clearExceptions();
    }

    logger.trace(Trace.EXIT_METHOD_1, "lastThrown", lastThrown);
    return lastThrown;
  }
}
