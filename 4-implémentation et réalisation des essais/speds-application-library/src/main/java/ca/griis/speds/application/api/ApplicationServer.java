/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ApplicationServer.
 * @brief @~english Contains description of ApplicationServer class.
 */

package ca.griis.speds.application.api;

import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;
import ca.griis.speds.application.api.exception.DeserializationException;

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
 * @brief @~french L'interface du Serveur de la couche applicative
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
 *      2025-02-07 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface ApplicationServer {

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Indique un échange de données
   * @exception DeserializationException Erreur de désérialisation
   * @return L'interface de données
   */
  InterfaceDataUnit01Dto indication();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   *
   * @brief @~french Répond à un échange de données
   * @param idu l'interface de données contenat la réponse
   */
  void response(InterfaceDataUnit01Dto idu);
}
