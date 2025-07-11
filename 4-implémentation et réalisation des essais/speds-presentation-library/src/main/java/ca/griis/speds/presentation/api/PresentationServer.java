/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface PresentationServer.
 * @brief @~english Implementation of the PresentationServer interface.
 */

package ca.griis.speds.presentation.api;

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
 * @brief @~french Définit les services d'un serveur de la couche présentation.
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
public interface PresentationServer {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Indique un échange de données.
   * @return L’interface d’unité de données de la couche supérieure et la couche présentation sous
   *         le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  String indication();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Répond à un échange de données.
   * @param idu L’interface d’unité de données de la couche supérieure et la couche présentation
   *        sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  void response(String idu);
}
