/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableDataLinkServer.
 * @brief @~english Implementation of the ImmutableDataLinkServer class.
 */

package ca.griis.speds.link.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.link.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.link.api.dto.InterfaceDataUnit56Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.ProtocolIdu;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.link.api.exception.SerializationException;
import ca.griis.speds.link.api.exception.VerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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
 * @brief @~french Offre les services d'un serveur de la couche liaison.
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
 *      2025-06-06 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableDataLinkServer {
  private static final GriisLogger logger = getLogger(ImmutableDataLinkServer.class);

  private final ObjectMapper objectMapper;
  private final ConcurrentHashMap<UUID, UUID> trackingNumbers;

  ImmutableDataLinkServer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.trackingNumbers = new ConcurrentHashMap<>();
  }

  public void close() {
    trackingNumbers.clear();
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Indique un échange de données.
   * @param protocolIdu Les données reçues du protocole.
   * @exception ProtocolException En cas de problème avec le protocole.
   * @return L’interface d’unité de données de la couche supérieure et la couche liaison sous le
   *         format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  public String indication(ProtocolIdu protocolIdu) {
    logger.trace(Trace.ENTER_METHOD_1, "protocolIdu", protocolIdu);

    // keep tracking number
    UUID trackingNumber = UUID.randomUUID();
    trackingNumbers.put(trackingNumber, protocolIdu.messageIdentifier());
    Context56Dto ici = new Context56Dto(protocolIdu.destinationUri(), trackingNumber, false);
    String sdu = protocolIdu.sdu();
    InterfaceDataUnit56Dto idu = new InterfaceDataUnit56Dto(ici, sdu);

    String iduJson = null;
    try {
      iduJson = objectMapper.writeValueAsString(idu);
    } catch (JsonProcessingException e) {
      throw new SerializationException("serialization failed !", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "iduJson", iduJson);
    return iduJson;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Répond à un échange de données.
   * @param idu L’interface d’unité de données de la couche supérieure et la couche liaison sous le
   *        format JSON.
   * @exception ProtocolException En cas de problème avec le protocole.
   * @return Les données à échanger sur le protocole.
   *
   * @par Tâches
   *      S.O.
   */
  public ProtocolIdu response(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    InterfaceDataUnit56Dto interfaceDataUnit56Dto = null;
    try {
      interfaceDataUnit56Dto = objectMapper.readValue(idu, InterfaceDataUnit56Dto.class);
    } catch (JsonProcessingException e) {
      throw new SerializationException("Invalid idu format !", e);
    }

    Context56Dto ici = interfaceDataUnit56Dto.getContext();
    if (ici == null || !new IriVerification().isValidIri(ici.getDestinationIri())) {
      throw new VerificationException("Invalid ici or IRI value !");
    }

    String sdu = interfaceDataUnit56Dto.getMessage();
    if (sdu == null || sdu.trim().isEmpty()) {
      throw new VerificationException("Invalid sdu value !");
    }

    isValidTrackingNumber(ici);

    UUID messageId = trackingNumbers.remove(ici.getTrackingNumber());
    ProtocolIdu protocolIdu = new ProtocolIdu(ici.getDestinationIri(), messageId, sdu);

    logger.trace(Trace.EXIT_METHOD_1, "protocolIdu", protocolIdu);
    return protocolIdu;
  }

  private void isValidTrackingNumber(Context56Dto ici) {
    logger.trace(Trace.ENTER_METHOD_1, "ici", ici);

    if (!trackingNumbers.containsKey(ici.getTrackingNumber())) {
      throw new ProtocolException("Unable to find tracking number");
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
