/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ApplicationFactory.
 * @brief @~english Contains description of ApplicationFactory class.
 */

package ca.griis.speds.application.api;

import ca.griis.speds.presentation.api.PresentationHost;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      Factory method design pattern
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Définit une fabriques d'entités nécessaires à la couche application.
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
 *      2025-02-10 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface ApplicationFactory {

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit un hôte de la couche application.
   * @param parameters Les paramètres de l'hôte des couches sous le format JSON.
   * @return L'interface hôte de la couche application
   */
  ApplicationHost init(String parameters);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un service hôte de la couche application.
   * @param parameters Les paramètres de l'hôte des couches sous le format JSON.
   * @param pgaService Service de PGA.
   *
   * @par Tâches
   *      S.O.
   */
  PresentationHost initPresentationHost(String parameters);
}
