/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableNetworkHost.
 * @brief @~english ImmutableNetworkHost class implementation.
 */

package ca.griis.speds.network.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.service.host.ExchangeDataConfirm;
import ca.griis.speds.network.service.host.ExchangeDataIndication;
import ca.griis.speds.network.service.host.ExchangeDataRequest;
import ca.griis.speds.network.service.host.SentMessageIdSet;
import ca.griis.speds.network.service.identification.IdentifierGenerator;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.SealManager;

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
 * @brief @~french Offre les services d'un hôte immutable de la couche réseau.
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
public final class ImmutableNetworkHost implements NetworkHost {
  private static final GriisLogger logger = getLogger(ImmutableNetworkHost.class);

  private final DataLinkHost dataLinkHost;
  private final SentMessageIdSet sentMessagesIds;
  private final ExchangeDataRequest exchangeRequest;
  private final ExchangeDataConfirm exchangeConfirm;
  private final ExchangeDataIndication exchangeIndication;

  public ImmutableNetworkHost(DataLinkHost dataLinkHost, String spedsVersion, String spedsReference,
      CertificatePrivateKeyPair certificatePrivateKeyPair, IdentifierGenerator identifierGenerator,
      NetworkMarshaller networkMarshaller, SealManager sealManager, SentMessageIdSet sentMessages) {
    logger.trace(Trace.ENTER_METHOD_7, "dataLinkHost", dataLinkHost, "spedsVersion", spedsVersion,
        "spedsReference", spedsReference, "certificatePrivateKeyPair", certificatePrivateKeyPair,
        "identifierGenerator", identifierGenerator, "networkMarshaller", networkMarshaller,
        "sealManager", sealManager, "sentMessages", sentMessages);
    this.dataLinkHost = dataLinkHost;
    this.sentMessagesIds = sentMessages;

    this.exchangeRequest = new ExchangeDataRequest(identifierGenerator, networkMarshaller,
        sentMessagesIds, spedsVersion, spedsReference, certificatePrivateKeyPair, sealManager);

    this.exchangeConfirm = new ExchangeDataConfirm(networkMarshaller, sealManager, sentMessagesIds);

    this.exchangeIndication = new ExchangeDataIndication(networkMarshaller, sealManager,
        spedsVersion, spedsReference, certificatePrivateKeyPair);
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    dataLinkHost.close();
    sentMessagesIds.clearMessageIds();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void request(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      String idu56 = exchangeRequest.dataRequestProcess(idu);
      dataLinkHost.request(idu56);
    } catch (Exception e) {
      logger.error(Error.GENERIC_ERROR, e.getMessage());
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void response(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      String idu56 = exchangeRequest.dataRequestProcess(idu);
      dataLinkHost.request(idu56);
    } catch (Exception e) {
      logger.error(Error.GENERIC_ERROR, e.getMessage());
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void confirm() {
    logger.trace(Trace.ENTER_METHOD_0);

    final String idu56 = dataLinkHost.confirm();
    logger.info(Info.VARIABLE_LOGGING_1, "idu56", idu56);

    try {
      exchangeConfirm.dataConfirmProcess(idu56);
    } catch (Exception e) {
      logger.error(Error.GENERIC_ERROR, e.getMessage());
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public String indication() {
    logger.trace(Trace.ENTER_METHOD_0);

    String msgUp = "";
    final String receivedIdu56 = dataLinkHost.indication();

    try {
      // Validation du message reçu de la couche inférieure
      exchangeIndication.validateReceivedMessage(receivedIdu56);

      // Création du message d'accusé de réception
      final String idu56 = exchangeIndication.dataReplyProcess(receivedIdu56);

      // Préparation du message pour la couche supérieure
      final String idu45 = exchangeIndication.dataIndicationProcess(receivedIdu56);
      logger.trace(Trace.EXIT_METHOD_1, "idu45", idu45);

      // Transmettre les IDU
      dataLinkHost.response(idu56);
      msgUp = idu45;
    } catch (Exception e) {
      logger.error(Error.GENERIC_ERROR, e.getMessage());
      msgUp = "";
    }

    logger.trace(Trace.EXIT_METHOD_1, "msgUp", msgUp);
    return msgUp;
  }
}
