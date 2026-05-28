/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface HostFactory.
 * @brief @~english Implementation of the HostFactory interface.
 */

package ca.griis.speds.link.api;

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
 * @brief @~french Définit une fabriques d'entités nécessaires à la couche de protocole.
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
 *      2026-01-30 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface HostFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   *
   * @brief @~french Construit un hôte.
   * @param parameters Les paramètres de l'hôte sous le format JSON.
   * @param hostEventConsumer Classe qui implémente l'interface HostEvent et consomme des
   *        événements.
   *
   * @par Tâches
   *      S.O.
   */
  Host init(String parameters, HostEvent hostEventConsumer);
}
