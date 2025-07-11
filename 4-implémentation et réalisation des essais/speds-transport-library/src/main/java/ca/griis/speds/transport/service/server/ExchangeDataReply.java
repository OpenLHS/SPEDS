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
import ca.griis.speds.transport.exception.ContentSealException;
import ca.griis.speds.transport.exception.DeserializationException;
import ca.griis.speds.transport.exception.HeaderSealException;
import ca.griis.speds.transport.exception.SerializationException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.server.datatype.DataReplyMessages;
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
public class ExchangeDataReply {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ExchangeDataReply.class);

  public static DataReplyMessages dataReplyProcess(InterfaceDataUnit45Dto iduDto,
      String spedsVersion, String spedsReference) {
    logger.trace(Trace.ENTER_METHOD_3, "iduDto", iduDto, "spedsVersion", spedsVersion,
        "spedsReference", spedsReference);

    // Récupérer SDU
    final ProtocolDataUnit4TraDto pdu;
    try {
      pdu = SharedObjectMapper.getInstance().getMapper().readValue(iduDto.getMessage(),
          ProtocolDataUnit4TraDto.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException(e.getMessage());
    }

    // Création du ICI du Idu3-4
    // extraire le code des iri
    final String sourceIri = iduDto.getContext().getSourceIri();
    final String destinationIri = iduDto.getContext().getDestinationIri();
    final String sourceCode = pdu.getHeader().getSourceCode();
    final String destinationCode = pdu.getHeader().getDestinationCode();

    final Sha512Hashing sha512Hashing = new Sha512Hashing();
    verifyStamps(pdu, sha512Hashing);

    // Création du message ACK à envoyé à la couche inférieure
    final String msgId = pdu.getHeader().getId().toString();
    final Header45Dto headerTraDto =
        new Header45Dto(Header45Dto.Msgtype.TRA_MSG_REC, msgId, sourceCode, destinationCode,
            new Speds45Dto(spedsVersion, spedsReference));
    final String contentResponse = "ACK";

    // Créer le sceau de l'entête et du contenu
    final String headerString;
    try {
      headerString = SharedObjectMapper.getInstance().getMapper().writeValueAsString(headerTraDto);
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
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
      throw new SerializationException(e.getMessage());
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
      throw new SerializationException(e.getMessage());
    }

    final DataReplyMessages dataReplyMessages = new DataReplyMessages(idu34Indication,
        idu45Response);

    logger.trace(Trace.EXIT_METHOD_1, "dataReplyMessages", dataReplyMessages);
    return dataReplyMessages;
  }

  private static void verifyStamps(ProtocolDataUnit4TraDto sdu, Sha512Hashing sha512Hashing) {
    logger.trace(Trace.ENTER_METHOD_2, "sdu", sdu, "sha512Hashing", sha512Hashing);

    // Vérification de l'intégrité de l'entête et du contenu
    final String sduHeader;
    try {
      sduHeader = SharedObjectMapper.getInstance().getMapper().writeValueAsString(sdu.getHeader());
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
    }

    // Hasher l'entête et le contenu
    final Hash headerEncrypt = sha512Hashing.hash(sduHeader.getBytes(StandardCharsets.UTF_8));
    final Hash contentEncrypt =
        sha512Hashing.hash(sdu.getContent().getBytes(StandardCharsets.UTF_8));
    final String sealHeaderSduGenerated = headerEncrypt.asBase64();
    final String sealContentSduGenerated = contentEncrypt.asBase64();

    // Vérifier l'intégrité de l'entête et du contenu
    if (!sdu.getStamp().getHeaderSeal().equals(sealHeaderSduGenerated)) {
      throw new HeaderSealException("Header seal are not the same");
    }

    if (!sdu.getStamp().getContentSeal().equals(sealContentSduGenerated)) {
      throw new ContentSealException("Content seal are not the same");
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
