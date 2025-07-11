/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface DataLinkClient.
 * @brief @~english Implementation of the DataLinkHost interface.
 */

package ca.griis.speds.link.api;

import ca.griis.speds.link.api.exception.ProtocolException;

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
 * @brief @~french Définit les services d'un client de la couche liaison.
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
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface DataLinkClient {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Demande d’échanger des données.
   * @param idu L’interface d’unité de données de la couche supérieure et la couche liaison sous le
   *        format JSON.
   * @exception ProtocolException En cas de problème avec le protocole.
   * @return Les données à échanger.
   *
   * @par Tâches
   *      S.O.
   */
  String request(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Confirme l’échange de données.
   * @exception ProtocolException En cas de problème avec le protocole.
   * @return L’interface d’unité de données de la couche supérieure et la couche liaison sous le
   *         format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  String confirm();
}
