/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface NetworkFactory.
 * @brief @~english Implementation of the NetworkFactory interface.
 */

package ca.griis.speds.network.api;

import ca.griis.speds.link.api.DataLinkHost;

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
 * @brief @~french Définit une fabrique pour construire les entités de la couche réseau.
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
public interface NetworkFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Construit un hôte de la couche réseau.
   * @param parameters Les paramètres de l'hôte de la couche réseau sous le format JSON.
   * @return L'hôte de la couche réseau construit par la méthode.
   *
   * @par Tâches
   *      S.O.
   */
  NetworkHost initHost(String parameters);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un service hôte de la couche liaison.
   * @param parameters Les paramètres de l'hôte de la couche liaison sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  DataLinkHost initDataLinkHost(String parameters);
}
