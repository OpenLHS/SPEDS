/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableDataLinkHost.
 * @brief @~english Implementation of the ImmutableDataLinkHost class.
 */

package ca.griis.speds.link.internal;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.ProtocolHost;
import ca.griis.speds.communication.protocol.ProtocolHostEvent;
import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.link.internal.dispatcher.SubmitDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * @brief @~french Offre les services d'un hôte de la couche liaison.
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
public class ImmutableDataLinkHost implements Host, ProtocolHostEvent {
  private static final GriisLogger logger = getLogger(ImmutableDataLinkHost.class);

  private final ProtocolHost host;
  private final HostEvent hostEvent;
  private final SubmitDispatcher submitDispatcher;
  private final NotifyDispatcher notifyDispatcher;

  public ImmutableDataLinkHost(ObjectMapper objectMapper, ProtocolHost host, HostEvent hostEvent) {
    this.host = host;
    this.hostEvent = hostEvent;
    this.submitDispatcher = new SubmitDispatcher(objectMapper, host);
    this.notifyDispatcher = new NotifyDispatcher(objectMapper);
  }

  public ProtocolHost getHost() {
    return host;
  }

  @Override
  public void close() {
    host.close();
  }

  @Override
  public CompletableFuture<Optional<String>> submitIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> out = submitDispatcher.handle(idu);
    var future = CompletableFuture.completedFuture(out);

    logger.trace(Trace.EXIT_METHOD_1, "future", future);
    return future;
  }

  @Override
  public void notifyIdu(ProtocolIdu event) {
    logger.trace(Trace.ENTER_METHOD_1, "event", event);

    final var idu = notifyDispatcher.handle(event);
    if (idu.isPresent()) {
      hostEvent.notifyIdu(idu.get());
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyException(Exception exception) {
    hostEvent.notifyException(exception);
  }
}
