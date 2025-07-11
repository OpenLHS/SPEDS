/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ExchangeDataRequest.
 * @brief @~english Contains description of ExchangeDataRequest class.
 */

package ca.griis.speds.network.service.host;

import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.identification.DestinationNodeFinder;
import ca.griis.speds.network.service.identification.IdentifierGenerator;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import java.util.UUID;

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
 * @brief @~french Un processus de demande d'échange de données.
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
public class ExchangeDataRequest {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ExchangeDataRequest.class);

  private final IdentifierGenerator identifierGenerator;
  private final NetworkMarshaller networkMarshaller;
  private final SentMessageIdSet sentMessagesIds;
  private final SPEDSDto spedsDto;
  private final CertificatePrivateKeyPair certificatePrivateKeyPair;
  private final SealManager sealManager;

  public ExchangeDataRequest(IdentifierGenerator identifierGenerator,
      NetworkMarshaller networkMarshaller, SentMessageIdSet sentMessagesIds, String spedsVersion,
      String spedsReference, CertificatePrivateKeyPair certificatePrivateKeyPair,
      SealManager sealManager) {
    this.identifierGenerator = identifierGenerator;
    this.networkMarshaller = networkMarshaller;
    this.sentMessagesIds = sentMessagesIds;
    this.spedsDto = new SPEDSDto(spedsVersion, spedsReference);
    this.certificatePrivateKeyPair = certificatePrivateKeyPair;
    this.sealManager = sealManager;
  }

  /**
   * @brief @~french Processus de demande d’échanger des données
   * @param idu Le message de demande en provenance de la couche supérieure
   * @exception DeserializationException si le message ne correspond pas à un InterfaceDataUnit45Dto
   * @return le message pour la couche inférieure
   */
  public String dataRequestProcess(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu45", idu);
    InterfaceDataUnit45Dto idu45 = networkMarshaller.unmarshallFromTransport(idu);

    final String id = identifierGenerator.generateId();
    // mémorisation du champ id généré
    this.sentMessagesIds.addMessageId(id);

    final String destinationIri = idu45.getContext().getDestinationIri();

    final HeaderDto headerDto =
        new HeaderDto(HeaderDto.Msgtype.RES_ENV, id, idu45.getContext().getSourceIri(),
            destinationIri, certificatePrivateKeyPair.getAuthentification(), false, spedsDto);

    final String headerSeal =
        sealManager.createSeal(headerDto, Seal.header, certificatePrivateKeyPair.privateKey());

    final String contentSeal = sealManager.createSeal(idu45.getMessage(), Seal.content,
        certificatePrivateKeyPair.privateKey());

    final String pdu5NetJson =
        networkMarshaller.marshallNetworkPdu(new ProtocolDataUnit5Dto(headerDto,
            new StampDto(headerSeal, contentSeal), idu45.getMessage()));

    final UUID trackingNumber = UUID.fromString(id);
    final String linkIri = DestinationNodeFinder.nextNode(HeaderDto.Msgtype.RES_ENV,
        idu45.getContext().getSourceIri(), destinationIri);
    final String idu56 = networkMarshaller
        .marshallToDataLink(new Context56Dto(linkIri, trackingNumber, false), pdu5NetJson);

    logger.trace(Trace.EXIT_METHOD_1, "idu56", idu56);
    return idu56;
  }
}
