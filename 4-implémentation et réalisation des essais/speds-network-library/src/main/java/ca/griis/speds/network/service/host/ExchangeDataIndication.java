/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ExchangeDataIndication.
 * @brief @~english Contains description of ExchangeDataIndication class.
 */

package ca.griis.speds.network.service.host;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.js2p.gen.speds.network.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.network.api.dto.StampDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.serialization.NetworkMarshaller;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.exception.InvalidSignatureException;
import ca.griis.speds.network.service.exception.MissingAuthenticationException;
import ca.griis.speds.network.service.identification.DestinationNodeFinder;
import ca.griis.speds.network.service.verification.CertificateVerification;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import ca.griis.speds.network.signature.Seal;
import ca.griis.speds.network.signature.SealManager;
import java.util.Objects;
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
 * @brief @~french Un processus qui traite l'échange de données.
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
public final class ExchangeDataIndication {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ExchangeDataIndication.class);

  private final NetworkMarshaller networkMarshaller;
  private final SealManager sealManager;
  private final SPEDSDto spedsDto;
  private final CertificatePrivateKeyPair certificatePrivateKeyPair;

  public ExchangeDataIndication(NetworkMarshaller networkMarshaller, SealManager sealManager,
      String spedsVersion, String spedsReference,
      CertificatePrivateKeyPair certificatePrivateKeyPair) {
    this.networkMarshaller = networkMarshaller;
    this.sealManager = sealManager;
    this.spedsDto = new SPEDSDto(spedsVersion, spedsReference);
    this.certificatePrivateKeyPair = certificatePrivateKeyPair;
  }

  /**
   * @brief @~french Vérification du message reçu
   * @param receivedIdu56 le message
   * @exception DeserializationException si le message ne correspond pas à celui attendu ou n'est
   *            pas du bon type
   * @exception MissingAuthenticationException le champ authentification est invalide ou manquant
   * @exception InvalidSignatureException L'empreinte de l'entête ou du contenu est invalide.
   */
  public void validateReceivedMessage(String receivedIdu56) {
    logger.trace(Trace.ENTER_METHOD_1, "receivedIdu56", receivedIdu56);

    final InterfaceDataUnit56Dto receivedIdu56Dto =
        networkMarshaller.unmarshallFromDataLink(receivedIdu56);
    final ProtocolDataUnit5Dto receivedSdu =
        networkMarshaller.unmarshallNetworkPdu(receivedIdu56Dto.getMessage());

    final HeaderDto.Msgtype msgType = receivedSdu.getHeader().getMsgtype();
    if (msgType != HeaderDto.Msgtype.RES_ENV) {
      final String exception =
          "Wrong message type for Network PDU. Expected RES.ENV but got: " + msgType.value();
      throw new DeserializationException(exception);
    }

    String auth = String.valueOf(receivedSdu.getHeader().getAuthentification());
    boolean verifyAuth = Objects.isNull(auth) || "".equals(auth) || !CertificateVerification
        .verifyCertificate(auth, receivedSdu.getHeader().getSourceIri());
    if (verifyAuth) {
      throw new MissingAuthenticationException("Host certificate is missing or invalid.");
    }

    if (!sealManager.verifySeal(receivedSdu.getHeader(), Seal.header,
        (String) receivedSdu.getHeader().getAuthentification(),
        receivedSdu.getStamp().getHeaderSeal())) {
      throw new InvalidSignatureException("The network header signature is invalid.");
    }

    if (!sealManager.verifySeal(receivedSdu.getContent(), Seal.content,
        (String) receivedSdu.getHeader().getAuthentification(),
        receivedSdu.getStamp().getContentSeal())) {
      throw new InvalidSignatureException("The network content signature is invalid.");
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  /**
   * @brief @~french Construit le message réponse à faire suivre à la couche inférieure
   * @param receivedIdu56 message reçu de la couche inférieure
   * @return le message construit
   */
  public String dataReplyProcess(String receivedIdu56) {
    logger.trace(Trace.ENTER_METHOD_1, "receivedIdu56", receivedIdu56);
    final InterfaceDataUnit56Dto receivedIdu56Dto =
        networkMarshaller.unmarshallFromDataLink(receivedIdu56);
    final ProtocolDataUnit5Dto receivedSdu =
        networkMarshaller.unmarshallNetworkPdu(receivedIdu56Dto.getMessage());

    final String id = receivedSdu.getHeader().getId();
    final String sourceIri = receivedSdu.getHeader().getSourceIri();
    final String destinationIri = receivedSdu.getHeader().getDestinationIri();
    final UUID dataLinkTrackingNumber = receivedIdu56Dto.getContext().getTrackingNumber();

    final HeaderDto headerDto = new HeaderDto(HeaderDto.Msgtype.RES_REC, id, sourceIri,
        destinationIri, certificatePrivateKeyPair.getAuthentification(), false, spedsDto);

    final String headerSeal =
        sealManager.createSeal(headerDto, Seal.header, certificatePrivateKeyPair.privateKey());

    final String pdu5NetJson = networkMarshaller
        .marshallNetworkPdu(new ProtocolDataUnit5Dto(headerDto, new StampDto(headerSeal, ""), ""));

    String linkIri =
        DestinationNodeFinder.nextNode(HeaderDto.Msgtype.RES_REC, sourceIri, destinationIri);
    final String idu56 = networkMarshaller
        .marshallToDataLink(new Context56Dto(linkIri, dataLinkTrackingNumber, false), pdu5NetJson);

    logger.trace(Trace.EXIT_METHOD_1, "idu56", idu56);
    return idu56;
  }

  /**
   * @brief @~french Construit le message à envoyer à la couche supérieure
   * @param receivedIdu56 le message reçu
   * @return le message construit
   */
  public String dataIndicationProcess(String receivedIdu56) {
    logger.trace(Trace.ENTER_METHOD_1, "receivedIdu56", receivedIdu56);
    final InterfaceDataUnit56Dto receivedIdu56Dto =
        networkMarshaller.unmarshallFromDataLink(receivedIdu56);
    final ProtocolDataUnit5Dto receivedSdu =
        networkMarshaller.unmarshallNetworkPdu(receivedIdu56Dto.getMessage());

    final String id = receivedSdu.getHeader().getId();
    final String sourceIri = receivedSdu.getHeader().getSourceIri();
    final String destinationIri = receivedSdu.getHeader().getDestinationIri();

    final String idu45 = networkMarshaller.marshallToTransport(
        new Context45Dto(sourceIri, destinationIri, UUID.fromString(id), false),
        receivedSdu.getContent());

    logger.trace(Trace.EXIT_METHOD_1, "idu45", idu45);
    return idu45;
  }
}
