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
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.IdentifierGenerator;
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
public final class ExchangeDataRequest {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ExchangeDataRequest.class);

  private final IdentifierGenerator identifierGenerator;
  private final Set<String> pendingMessagesId;

  public ExchangeDataRequest(IdentifierGenerator identifierGenerator,
      Set<String> pendingMessagesId) {
    logger.trace(Trace.ENTER_METHOD_2, "identifierGenerator", identifierGenerator,
        "pendingMessagesId",
        pendingMessagesId);
    this.identifierGenerator = identifierGenerator;
    this.pendingMessagesId = pendingMessagesId;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Traite l'envoi d'un message requête de la couche transport.
   * @param idu IDU de la couche supérieure en chaîne de caractères.
   * @param spedsVersion Version de SPEDS.
   * @param spedsReference Référence <a SPEDS.
   * @return IDU de la couche inférrieure en chaîne de caractères à transmettre.
   *
   * @par Tâches
   *      S.O.
   */
  public String request(String idu, String spedsVersion, String spedsReference)
      throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_3, "idu", idu, "spedsVersion", spedsVersion, "spedsReference",
        spedsReference);

    final InterfaceDataUnit34Dto iduDto =
        SharedObjectMapper.getInstance().getMapper().readValue(idu, InterfaceDataUnit34Dto.class);

    // Récupérer le ICI et SDU de l'idu-3-4
    final Context34Dto ici34 = iduDto.getContext();
    final String sduSes = iduDto.getMessage();
    final String traId = identifierGenerator.generateId();
    pendingMessagesId.add(traId);

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
    final String traMsgHeaderSerialized =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(traMsgHeader);
    final Hash traHeader =
        sha512Hashing.hash(traMsgHeaderSerialized.getBytes(StandardCharsets.UTF_8));
    final String sealheaderSdu = traHeader.asBase64();
    final StampDto traStamp = new StampDto(sealheaderSdu, sealContentSdu);

    // Création du message Transport
    final ProtocolDataUnit4TraDto traMsgEnv =
        new ProtocolDataUnit4TraDto(traMsgHeader, traStamp, sduSes);
    final String serialTraMessage =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(traMsgEnv);
    final InterfaceDataUnit45Dto networkIduConstruct =
        new InterfaceDataUnit45Dto(context, serialTraMessage);
    final String networkIdu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(networkIduConstruct);

    logger.trace(Trace.EXIT_METHOD_1, "networkIdu", networkIdu);
    return networkIdu;
  }
}
