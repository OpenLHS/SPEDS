/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface PresentationFactory.
 * @brief @~english Implementation of the PresentationFactory interface.
 */

package ca.griis.speds.presentation.api;

import ca.griis.speds.session.api.SessionHost;

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
 * @brief @~french Définit une fabriques d'entités nécessaires à la couche présentation.
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
public interface PresentationFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un hôte de la couche présentation.
   * @param parameters Les paramètres de l'hôte de la couche présentation sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  PresentationHost init(String parameters);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un hôte de la couche session.
   * @param parameters Les paramètres de l'hôte de la couche session sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  SessionHost initSessionHost(String parameters);
}
