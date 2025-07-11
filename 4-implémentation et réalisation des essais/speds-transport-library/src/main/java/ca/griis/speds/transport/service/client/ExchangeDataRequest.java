/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ExchangeDataRequest.
 * @brief @~english Implementation of the ExchangeDataRequest class.
 */

package ca.griis.speds.transport.service.client;

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
import ca.griis.speds.transport.exception.DeserializationException;
import ca.griis.speds.transport.exception.SerializationException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.identification.IdentifierGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
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
 * @brief @~french Un processus qui demande une requête d'échange de données.
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
 *      2025-03-04 [JM] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class ExchangeDataRequest {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ExchangeDataRequest.class);

  private final IdentifierGenerator identifierGenerator;

  public ExchangeDataRequest(IdentifierGenerator identifierGenerator) {
    logger.trace(Trace.ENTER_METHOD_1, "identifierGenerator", identifierGenerator);
    this.identifierGenerator = identifierGenerator;
  }

  public String dataRequestProcess(String idu, String spedsVersion, String spedsReference,
      Set<String> sentMessagesId) {
    logger.trace(Trace.ENTER_METHOD_4, "idu", idu, "spedsVersion", spedsVersion, "spedsReference",
        spedsReference, "sentMessagesId", sentMessagesId);


    final InterfaceDataUnit34Dto iduDto;
    try {
      iduDto = SharedObjectMapper.getInstance().getMapper().readValue(idu,
          InterfaceDataUnit34Dto.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException(e.getMessage());
    }

    // Récupérer le ICI et SDU de l'idu-3-4
    final Context34Dto ici34 = iduDto.getContext();
    final String sduSes = iduDto.getMessage();
    final String traId = this.identifierGenerator.generateId();
    sentMessagesId.add(traId);

    // Création de l'ici à transmettre à la couche inférieur
    final Context45Dto context = new Context45Dto(ici34.getSourceIri(),
        ici34.getDestinationIri(), UUID.fromString(traId), Boolean.FALSE);

    // Création du sceau d'intégrité du sdu reçu
    final Sha512Hashing sha512Hashing = new Sha512Hashing();
    final Hash hashSdu = sha512Hashing.hash(sduSes.getBytes(StandardCharsets.UTF_8));
    final String sealContentSdu = hashSdu.asBase64();

    // Construire le l'entête du message transport
    final String sourceCode = iduDto.getContext().getSourceCode();
    final String destinationCode = iduDto.getContext().getDestinationCode();
    final Speds45Dto speds = new Speds45Dto(spedsVersion, spedsReference);

    final Header45Dto traMsgHeader = new Header45Dto(
        Header45Dto.Msgtype.TRA_MSG_ENV,
        traId,
        sourceCode,
        destinationCode,
        speds);

    // Création du sceau d'intégrité de l'entête du traMsgEnv
    final String traMsgHeaderSerialized;
    try {
      traMsgHeaderSerialized =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(traMsgHeader);
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
    }
    final Hash traHeader =
        sha512Hashing.hash(traMsgHeaderSerialized.getBytes(StandardCharsets.UTF_8));
    final String sealheaderSdu = traHeader.asBase64();
    final StampDto traStamp = new StampDto(sealheaderSdu, sealContentSdu);

    // Création du message Transport
    final ProtocolDataUnit4TraDto traMsgEnv =
        new ProtocolDataUnit4TraDto(traMsgHeader, traStamp, sduSes);
    final String serialTraMessage;
    try {
      serialTraMessage = SharedObjectMapper.getInstance().getMapper().writeValueAsString(traMsgEnv);
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
    }

    final InterfaceDataUnit45Dto networkIduConstruct =
        new InterfaceDataUnit45Dto(context, serialTraMessage);
    final String networkIdu;
    try {
      networkIdu =
          SharedObjectMapper.getInstance().getMapper().writeValueAsString(networkIduConstruct);
    } catch (JsonProcessingException e) {
      throw new SerializationException(e.getMessage());
    }

    logger.trace(Trace.EXIT_METHOD_1, "networkIdu", networkIdu);
    return networkIdu;
  }
}
