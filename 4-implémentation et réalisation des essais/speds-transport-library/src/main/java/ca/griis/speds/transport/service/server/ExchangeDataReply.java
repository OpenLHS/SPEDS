/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ExchangeDataIndication.
 * @brief @~english Implementation of the ExchangeDataIndication class.
 */

package ca.griis.speds.transport.service.server;

import ca.griis.cryptography.hash.entity.Hash;
import ca.griis.cryptography.hash.hashing.Sha512Hashing;
import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.StampDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.DataReplyMessages;
import ca.griis.speds.transport.service.SilentIgnoredException;
import ca.griis.speds.transport.service.security.StampVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
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
 * @brief @~french Un processus qui reçoit une indication d'échange de données.
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
 *      2025-03-11 [JM] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ExchangeDataReply {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ExchangeDataReply.class);

  private final StampVerifier stampVerfier;

  public ExchangeDataReply() {
    logger.trace(Trace.ENTER_METHOD_0);

    this.stampVerfier = new StampVerifier();
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Traite la réception d'un message requête de la couche transport.
   * @param iduDto IDU de la couche inférieure.
   * @exception SilentIgnoredException Exception silencieuse.
   * @return Un IDU à la couche supérieure et un à la couche inférieure.
   *
   * @par Tâches
   *      S.O.
   */
  public DataReplyMessages indication(InterfaceDataUnit45Dto iduDto) {
    logger.trace(Trace.ENTER_METHOD_1, "iduDto", iduDto);

    // Récupérer SDU
    final ProtocolDataUnit4TraDto pdu;
    try {
      pdu = SharedObjectMapper.getInstance().getMapper().readValue(iduDto.getMessage(),
          ProtocolDataUnit4TraDto.class);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoredException(e.getMessage());
    }

    // Création du ICI du Idu3-4
    // extraire le code des iri
    final String sourceIri = iduDto.getContext().getSourceIri();
    final String destinationIri = iduDto.getContext().getDestinationIri();
    final String sourceCode = pdu.getHeader().getSourceCode();
    final String destinationCode = pdu.getHeader().getDestinationCode();

    final Sha512Hashing sha512Hashing = new Sha512Hashing();
    stampVerfier.verifyStamps(pdu, sha512Hashing);

    // Création du message ACK à envoyé à la couche inférieure
    final String msgId = (String) pdu.getHeader().getId();
    final Header45Dto headerTraDto =
        new Header45Dto(Header45Dto.Msgtype.TRA_MSG_REC, msgId, sourceCode, destinationCode,
            new Speds45Dto(pdu.getHeader().getSpeds().getVersion(),
                pdu.getHeader().getSpeds().getReference()));
    final String contentResponse = "ACK";

    // Créer le sceau de l'entête et du contenu
    final String headerString;
    try {
      headerString = SharedObjectMapper.getInstance().getMapper().writeValueAsString(headerTraDto);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoredException(e.getMessage());
    }

    final Hash hashHeader = sha512Hashing.hash(headerString.getBytes(StandardCharsets.UTF_8));
    final Hash hashContent = sha512Hashing.hash(contentResponse.getBytes(StandardCharsets.UTF_8));
    final String sealHeaderSdu = hashHeader.asBase64();
    final String sealContentSdu = hashContent.asBase64();

    final StampDto stampTra = new StampDto(sealHeaderSdu, sealContentSdu);

    final ProtocolDataUnit4TraDto pduTraResponse =
        new ProtocolDataUnit4TraDto(headerTraDto, stampTra, contentResponse);

    // Sérialiser le message protocole de Transport
    final Context45Dto iduContext45 = new Context45Dto(destinationIri, sourceIri,
        iduDto.getContext().getTrackingNumber(), iduDto.getContext().getOptions());
    final String serializedTraMessage;
    final String idu45Response;
    try {
      serializedTraMessage =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(pduTraResponse);
      final InterfaceDataUnit45Dto idu45 =
          new InterfaceDataUnit45Dto(iduContext45, serializedTraMessage);
      idu45Response = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu45);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoredException(e.getMessage());
    }

    // Création du tracking number en utilisant le msgId de Transport
    final UUID trackingNumber = UUID.fromString(msgId);

    final Context34Dto context34Dto = new Context34Dto(
        sourceCode,
        destinationCode,
        sourceIri,
        trackingNumber,
        destinationIri,
        Boolean.FALSE);

    // Création du message à envoyer à la couche supérieur
    final InterfaceDataUnit34Dto idu34 = new InterfaceDataUnit34Dto(context34Dto, pdu.getContent());

    final String idu34Indication;
    try {
      idu34Indication = SharedObjectMapper.getInstance().getMapper().writeValueAsString(idu34);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoredException(e.getMessage());
    }

    final DataReplyMessages dataReplyMessages = new DataReplyMessages(idu34Indication,
        idu45Response);

    logger.trace(Trace.EXIT_METHOD_1, "dataReplyMessages", dataReplyMessages);
    return dataReplyMessages;
  }
}
