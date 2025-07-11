/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe InterfaceDataUnitConverter.
 * @brief @~english Contains description of InterfaceDataUnitConverter class.
 */

package ca.griis.speds.api.converter;

import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.js2p.gen.speds.application.api.dto.ProtocolDataUnit1APPDto;
import ca.griis.speds.api.dto.SpedsInterfaceDataUnit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
 * @brief @~french Définit un convertisseur d'unité de données d'interface de données de la couche
 *        application.
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
 *      2025-04-07 [FO] - Première ébauche.<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class InterfaceDataUnitConverter {
  private final ObjectMapper mapper;

  public InterfaceDataUnitConverter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Convertit une unité de données d'interface en unité de données du protocole.
   * @param idu L'unité de données de l'interface venant de la couche application.
   * @return L'unité de données du protocole.
   *
   * @par Tâches
   *      S.O.
   */
  public ProtocolDataUnit1APPDto convertToProtocolDataUnit(InterfaceDataUnit01Dto idu)
      throws JsonMappingException, JsonProcessingException {
    ProtocolDataUnit1APPDto pdu = mapper.readValue(idu.getMessage(), ProtocolDataUnit1APPDto.class);
    return pdu;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Convertit une unité de données d'interface en unité de données d'interface
   *        comprenant le type de message.
   * @param idu L'unité de données de l'interface venant de la couche application.
   * @return L'unité de données d'interface comprenant le type de message.
   *
   * @par Tâches
   *      S.O.
   */
  public SpedsInterfaceDataUnit convertToInterfaceDataUnit(InterfaceDataUnit01Dto idu)
      throws JsonMappingException, JsonProcessingException {
    ProtocolDataUnit1APPDto pdu = mapper.readValue(idu.getMessage(), ProtocolDataUnit1APPDto.class);
    SpedsInterfaceDataUnit result = new SpedsInterfaceDataUnit(pdu.getHeader().getMsgtype(), idu);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Convertit une unité de données d'interface en unité de données d'interface
   *        comprenant le type de message.
   * @param idu L'unité de données de l'interface venant de la couche application.
   * @type Le type de message.
   * @return L'unité de données d'interface comprenant le type de message.
   *
   * @par Tâches
   *      S.O.
   */
  public SpedsInterfaceDataUnit convertToInterfaceDataUnit(InterfaceDataUnit01Dto idu,
      HeaderDto.Msgtype type) {
    SpedsInterfaceDataUnit result = new SpedsInterfaceDataUnit(type, idu);
    return result;
  }
}
