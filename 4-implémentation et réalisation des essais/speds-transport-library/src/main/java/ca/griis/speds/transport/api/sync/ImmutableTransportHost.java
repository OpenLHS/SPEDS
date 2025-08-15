/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableTransportHost.
 * @brief @~english Implementation of the ImmutableTransportHost class.
 */

package ca.griis.speds.transport.api.sync;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.exception.SerializationException;
import ca.griis.speds.transport.service.DataReplyMessages;
import ca.griis.speds.transport.service.IdentifierGenerator;
import ca.griis.speds.transport.service.Poller;
import ca.griis.speds.transport.service.client.ExchangeDataConfirmation;
import ca.griis.speds.transport.service.client.ExchangeDataRequest;
import ca.griis.speds.transport.service.server.ExchangeDataReply;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
 * @brief @~french Offre les services d'un hôte de la couche transport.
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
public final class ImmutableTransportHost implements TransportHost {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ImmutableTransportHost.class);

  private final NetworkHost networkHost;
  private final String spedsVersion;
  private final String spedsReference;
  private final ExchangeDataRequest request;
  private final Poller poller;
  private final Set<String> pendingMessagesId;

  ImmutableTransportHost(NetworkHost networkHost, String spedsVersion,
      String spedsReference, IdentifierGenerator identifierGenerator,
      Integer pollerMaxQueueCapacity, Integer pollerNbThreads, Integer pollerSleepsMs) {
    logger.trace(Trace.ENTER_METHOD_7, "networkHost", networkHost, "spedsVersion", spedsVersion,
        "spedsReference", spedsReference, "identifierGenerator", identifierGenerator,
        "pollerMaxQueueCapacity", pollerMaxQueueCapacity, "pollerNbThreads", pollerNbThreads,
        "pollerSleepMs", pollerSleepsMs);
    this.networkHost = networkHost;
    this.spedsVersion = spedsVersion;
    this.spedsReference = spedsReference;

    this.pendingMessagesId = ConcurrentHashMap.newKeySet();
    this.request = new ExchangeDataRequest(identifierGenerator, pendingMessagesId);

    ExchangeDataReply indication = new ExchangeDataReply();
    ExchangeDataConfirmation confirm = new ExchangeDataConfirmation(pendingMessagesId);
    this.poller =
        new Poller(networkHost, indication, confirm, pollerMaxQueueCapacity, pollerNbThreads,
            pollerSleepsMs);
  }

  @Override
  public void listen() {
    poller.start();
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    poller.close();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void request(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    String networkIdu;
    try {
      networkIdu = request.request(idu, spedsVersion, spedsReference);

      networkHost.request(networkIdu);
      networkHost.confirm();
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void dataRequest(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    request(idu);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void dataConfirm() {
    logger.trace(Trace.ENTER_METHOD_0);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public String dataReply() {
    logger.trace(Trace.ENTER_METHOD_0);

    final String idu = indication();

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }

  @Override
  public String indication() {
    logger.trace(Trace.ENTER_METHOD_0);

    final DataReplyMessages dataReplyMessages = poller.poll();

    final String idu34 = dataReplyMessages.response34();
    final String idu45 = dataReplyMessages.response45();

    networkHost.request(idu45);
    networkHost.confirm();

    logger.trace(Trace.EXIT_METHOD_1, "idu34", idu34);
    return idu34;
  }

  @Override
  public Boolean isPending(String msgId) {
    return pendingMessagesId.contains(msgId);
  }
}
