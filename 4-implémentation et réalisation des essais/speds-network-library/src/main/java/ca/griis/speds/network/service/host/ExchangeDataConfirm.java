/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ExchangeDataConfirm.
 * @brief @~english Contains description of ExchangeDataConfirm class.
 */

package ca.griis.speds.network.service.host;

import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.exception.InvalidPduIdException;
import ca.griis.speds.network.service.exception.InvalidSignatureException;
import ca.griis.speds.network.service.exception.MissingAuthenticationException;
import ca.griis.speds.network.service.verification.CertificateVerification;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import java.util.Objects;

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
 * @brief @~french Un processus de confirmation d'échange de données.
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
 *      2025-06-26 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class ExchangeDataConfirm {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ExchangeDataConfirm.class);

  private final NetworkMarshaller networkMarshaller;
  private final SealManager sealManager;
  private final SentMessageIdSet sentMessageIds;

  public ExchangeDataConfirm(NetworkMarshaller networkMarshaller, SealManager sealManager,
      SentMessageIdSet sentMessageIds) {
    this.networkMarshaller = networkMarshaller;
    this.sealManager = sealManager;
    this.sentMessageIds = sentMessageIds;
  }

  /**
   * @brief @~french Le processus de confirmation d'échange de données
   * @param idu56 le message en provenance de la couche inférieure
   * @exception DeserializationException si le message ne correspond pas à celui attendu ou n'est
   *            pas du bon type
   * @exception InvalidPduIdException Le message ne se retrouve pas dans la table des messages
   *            attendus
   * @exception MissingAuthenticationException le champ authentification est invalide ou manquant
   * @exception InvalidSignatureException L'empreinte de l'entête ou du contenu est invalide.
   */
  public void dataConfirmProcess(String idu56) {
    logger.trace(Trace.ENTER_METHOD_1, "idu56", idu56);
    final ProtocolDataUnit5Dto sdu = networkMarshaller
        .unmarshallNetworkPdu(networkMarshaller.unmarshallFromDataLink(idu56).getMessage());

    final HeaderDto.Msgtype msgType = sdu.getHeader().getMsgtype();
    if (msgType != HeaderDto.Msgtype.RES_REC) {
      final String exception =
          "Wrong message type for Network PDU. Expected RES.REC but got: " + msgType.value();
      throw new DeserializationException(exception);
    }

    final String messageId = sdu.getHeader().getId();
    if (!sentMessageIds.containsMessageId(messageId)) {
      throw new InvalidPduIdException(
          "Message with the following ID was never sent by the current host: " + messageId);
    }

    String auth = String.valueOf(sdu.getHeader().getAuthentification());
    boolean verifyAuth = Objects.isNull(auth) || "".equals(auth)
        || !CertificateVerification.verifyCertificate(auth, sdu.getHeader().getDestinationIri());
    if (verifyAuth) {
      throw new MissingAuthenticationException("Host certificate is missing.");
    }

    if (!sealManager.verifySeal(sdu.getHeader(), Seal.header,
        auth, sdu.getStamp().getHeaderSeal())) {
      throw new InvalidSignatureException("The network header signature is invalid.");
    }

    sentMessageIds.removeMessageId(messageId);

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
