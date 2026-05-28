/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface ProtocolHost.
 * @brief @~english Implementation of the ProtocolHost interface.
 */

package ca.griis.speds.communication.protocol;

import ca.griis.speds.communication.protocol.unit.ProtocolIdu;

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
 * @brief @~french Définit un hôte pour le protocole de communication.
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
 *      2026-04-22 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface ProtocolHost {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Traite un IDU du protocole de communication,
   * @param idu IDU du protocole de communication.
   *
   * @par Tâches
   *      S.O.
   */
  void send(ProtocolIdu idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Termine l'hôte.
   *
   * @par Tâches
   *      S.O.
   */
  void close();
}
