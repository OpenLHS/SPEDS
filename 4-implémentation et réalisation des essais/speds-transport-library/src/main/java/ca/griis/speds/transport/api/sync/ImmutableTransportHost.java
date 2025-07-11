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
import ca.griis.speds.transport.service.client.ExchangeDataConfirmation;
import ca.griis.speds.transport.service.client.ExchangeDataRequest;
import ca.griis.speds.transport.service.identification.IdentifierGenerator;
import ca.griis.speds.transport.service.message.PollingManager;
import ca.griis.speds.transport.service.server.ExchangeDataReply;
import ca.griis.speds.transport.service.server.datatype.DataReplyMessages;
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
  private final PollingManager pollingManager;
  private final IdentifierGenerator identifierGenerator;
  private final Set<String> sentMessagesIds;

  ImmutableTransportHost(NetworkHost networkHost, String spedsVersion,
      String spedsReference, PollingManager pollingManager,
      IdentifierGenerator identifierGenerator) {
    logger.trace(Trace.ENTER_METHOD_5, "networkHost", networkHost, "spedsVersion", spedsVersion,
        "spedsReference", spedsReference, "pollingManager", pollingManager, "identifierGenerator",
        identifierGenerator);
    this.networkHost = networkHost;
    this.spedsVersion = spedsVersion;
    this.spedsReference = spedsReference;
    this.pollingManager = pollingManager;
    this.identifierGenerator = identifierGenerator;
    this.sentMessagesIds = ConcurrentHashMap.newKeySet();
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    networkHost.close();
    pollingManager.close();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void dataRequest(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    final String networkIdu =
        new ExchangeDataRequest(identifierGenerator).dataRequestProcess(idu, spedsVersion,
            spedsReference, sentMessagesIds);

    networkHost.request(networkIdu);
    networkHost.confirm();
    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void dataConfirm() {
    logger.trace(Trace.ENTER_METHOD_0);

    ExchangeDataConfirmation.dataConfirmationProcess(pollingManager.pollResponse(),
        sentMessagesIds);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public String dataReply() {
    logger.trace(Trace.ENTER_METHOD_0);

    final DataReplyMessages dataReplyMessages =
        ExchangeDataReply.dataReplyProcess(pollingManager.pollRequest(), spedsVersion,
            spedsReference);

    final String idu34 = dataReplyMessages.response34();
    final String idu45 = dataReplyMessages.response45();

    // Envoie le Ack vers le client
    networkHost.request(idu45);
    networkHost.confirm();

    logger.trace(Trace.EXIT_METHOD_1, "idu34", idu34);
    return idu34;
  }
}
