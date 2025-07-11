/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de l'interface ApplicationClient.
 * @brief @~english Contains description of ApplicationClient interface.
 */

package ca.griis.speds.application.api;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.speds.application.api.exception.DeserializationException;
import ca.griis.speds.application.api.exception.InvalidPduIdException;
import com.fasterxml.jackson.core.JsonProcessingException;

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
 * @brief @~french L'interface du Client de la couche applicative
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
 *      2025-01-24 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface ApplicationClient {

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Demande d'échanger des données
   * @param iduDto Le context et le message à envoyer
   * @exception JsonProcessingException Exception lors de la sérialisation de IDU
   */
  void request(InterfaceDataUnit01Dto iduDto);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Confirme l'échange de données
   * @return le context et le message
   * @exception DeserializationException Exception de désérialisation
   * @exception InvalidPduIdException L’identifiant de la réponse ne correspond pas à un identifiant
   *            d’une requête précédemment envoyée.
   */
  InterfaceDataUnit01Dto confirm();
}
