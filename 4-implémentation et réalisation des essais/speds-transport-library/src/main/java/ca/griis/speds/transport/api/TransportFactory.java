/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface TransportFactory.
 * @brief @~english Implementation of the TransportFactory interface.
 */

package ca.griis.speds.transport.api;

import ca.griis.speds.network.api.NetworkHost;

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
 * @brief @~french Définit une fabriques d'entités nécessaires à la couche transport.
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
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface TransportFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un hôte de la couche transport.
   * @param parameters Les paramètres de l'hôte de la couche transport sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  TransportHost init(String parameters);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un service hôte de la couche réseau.
   * @param parameters Les paramètres de l'hôte de la couche réseau sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  NetworkHost initNetworkHost(String parameters);
}
