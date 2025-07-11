/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe NetworkMessageMarshaller.
 * @brief @~english NetworkMessageMarshaller class implementation.
 */

package ca.griis.speds.network.serialization;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.network.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.Context56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.ProtocolDataUnit5Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.service.exception.DeserializationException;
import ca.griis.speds.network.service.exception.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * @brief @~french Permet la sérialisation et désérialisation des échanges de la couche Réseau.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-03-10 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class NetworkMarshaller {
  private static final GriisLogger logger = getLogger(NetworkMarshaller.class);

  private final ObjectMapper objectMapper;

  public NetworkMarshaller(ObjectMapper objectMapper) {
    logger.trace(Trace.ENTER_METHOD_1, "objectMapper", objectMapper);
    this.objectMapper = objectMapper;
  }

  /**
   * @brief @~english «Description of the function»
   * @param ici «Parameter description»
   * @param sdu «Parameter description»
   * @exception SerializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Sérialise une unité de données d'interface (IDU) en provenance de la couche
   *        Réseau à destination de la couche Transport.
   * @param ici Unité de données d'interface qui offre le contexte de l'échange.
   * @param sdu Unité de données de service qui offre le contenu de l'échange.
   * @exception SerializationException Erreur survenue lors de la sérialisation de l'IDU.
   * @return l'IDU sérialisé et prête à être échanger à la couche Transport.
   *
   * @par Tâches
   *      S.O.
   */
  public String marshallToTransport(Context45Dto ici, String sdu) {
    logger.trace(Trace.ENTER_METHOD_2, "ici", ici, "sdu", sdu);
    final String result;
    try {
      result = objectMapper.writeValueAsString(new InterfaceDataUnit45Dto(ici, sdu));
    } catch (JsonProcessingException e) {
      final String exception =
          "Cannot serialize Interface Data Unit between Network and Transport layer: "
              + e.getMessage();
      logger.error(exception);
      throw new SerializationException(exception);
    }
    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param idu «Parameter description»
   * @exception DeserializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désérialise une unité de données d'interface (IDU) en provenance de la couche
   *        Transport à destination de la couche Réseau.
   * @param idu Unité de données d'interface contenant le contexte et le contenu de l'échange.
   * @exception DeserializationException Erreur survenue lors de la désérialisation de l'IDU.
   * @return l'IDU désérialisé et prêt à être utilisé par la couche Réseau.
   *
   * @par Tâches
   *      S.O.
   */
  public InterfaceDataUnit45Dto unmarshallFromTransport(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    InterfaceDataUnit45Dto idu45Dto;
    try {
      idu45Dto = objectMapper.readValue(idu, InterfaceDataUnit45Dto.class);
    } catch (JsonProcessingException e) {
      final String exception =
          "Cannot read Interface Data Unit from Transport to Network layer: " + e.getMessage();
      logger.error(exception);
      throw new DeserializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "idu45Dto", idu45Dto);
    return idu45Dto;
  }

  /**
   * @brief @~english «Description of the function»
   * @param ici «Parameter description»
   * @param sdu «Parameter description»
   * @exception SerializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Sérialise une unité de données d'interface (IDU) en provenance de la couche
   *        Réseau à destination de la couche Liaison.
   * @param ici Unité de données d'interface qui offre le contexte de l'échange.
   * @param sdu Unité de données de service qui offre le contenu de l'échange.
   * @exception SerializationException Erreur survenue lors de la sérialisation de l'IDU.
   * @return l'IDU sérialisé et prête à être échanger à la couche Liaison.
   *
   * @par Tâches
   *      S.O.
   */
  public String marshallToDataLink(Context56Dto ici, String sdu) {
    logger.trace(Trace.ENTER_METHOD_2, "ici", ici, "sdu", sdu);

    final InterfaceDataUnit56Dto idu56Dto = new InterfaceDataUnit56Dto(ici, sdu);

    final String result;
    try {
      result = objectMapper.writeValueAsString(idu56Dto);
    } catch (JsonProcessingException e) {
      final String exception =
          "Cannot serialize Interface Data Unit from Network to Datalink layer: " + e.getMessage();
      logger.error(exception);
      throw new SerializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param idu «Parameter description»
   * @exception DeserializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désérialise une unité de données d'interface (IDU) en provenance de la couche
   *        Liaison à destination de la couche Réseau.
   * @param idu Unité de données d'interface contenant le contexte et le contenu de l'échange.
   * @exception DeserializationException Erreur survenue lors de la désérialisation de l'IDU.
   * @return l'IDU désérialisé et prêt à être utilisé par la couche Réseau.
   *
   * @par Tâches
   *      S.O.
   */
  public InterfaceDataUnit56Dto unmarshallFromDataLink(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    final InterfaceDataUnit56Dto idu56Dto;
    try {
      idu56Dto = objectMapper.readValue(idu, InterfaceDataUnit56Dto.class);
    } catch (JsonProcessingException e) {
      final String exception =
          "Cannot read Interface Data Unit from Datalink to Network layer: " + e.getMessage();
      logger.error(exception);
      throw new DeserializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "idu56Dto", idu56Dto);
    return idu56Dto;
  }

  /**
   * @brief @~english «Description of the function»
   * @param pdu «Parameter description»
   * @exception SerializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Sérialise un message Réseau.
   * @param pdu Message Réseau à sérialiser.
   * @exception SerializationException Erreur survenue lors de la sérialisation du message Réseau.
   * @return le message Réseau sérialisé.
   *
   * @par Tâches
   *      S.O.
   */
  public String marshallNetworkPdu(ProtocolDataUnit5Dto pdu) {
    logger.trace(Trace.ENTER_METHOD_1, "pdu", pdu);

    final String result;
    try {
      result = objectMapper.writeValueAsString(pdu);
    } catch (JsonProcessingException e) {
      final String exception =
          "Cannot serialize Network PDU: " + e.getMessage();
      logger.error(exception);
      throw new SerializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param sdu «Parameter description»
   * @exception DeserializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désérialise un message Réseau.
   * @param sdu Message Réseau à désérialiser.
   * @exception DeserializationException Erreur survenue lors de la désérialisation du message
   *            Réseau.
   * @return Le message Réseau désérialisé et prêt à être utilisé.
   *
   * @par Tâches
   *      S.O.
   */
  public ProtocolDataUnit5Dto unmarshallNetworkPdu(String sdu) {
    logger.trace(Trace.ENTER_METHOD_1, "sdu", sdu);

    final ProtocolDataUnit5Dto pdu;
    try {
      pdu = objectMapper.readValue(sdu, ProtocolDataUnit5Dto.class);
    } catch (JsonProcessingException e) {
      final String exception =
          "Cannot read Network Protocol Data Unit: " + e.getMessage();
      logger.error(exception);
      throw new DeserializationException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "pdu", pdu);
    return pdu;
  }
}
