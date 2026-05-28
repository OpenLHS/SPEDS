/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface ProtocolHostEvent.
 * @brief @~english Implementation of the ProtocolHostEvent interface.
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
 * @brief @~french Définit les événements générés par le protocole de communication et transmis
 *        à la couche liaison.
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
public interface ProtocolHostEvent {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Notifie une IDU échangée sous le protocole de communication à la couche
   *        liaison.
   * @param idu L’interface d’unité de données échangée à la couche liaison.
   *
   * @par Tâches
   *      S.O.
   */
  void notifyIdu(ProtocolIdu idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Notifie une exception sous le protocole de communication à la couche
   *        liaison.
   * @param exception L’exception échangée à la couche liaison.
   *
   * @par Tâches
   *      S.O.
   */
  void notifyException(Exception exception);
}
