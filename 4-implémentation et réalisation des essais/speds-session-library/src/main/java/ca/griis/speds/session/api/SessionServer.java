/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface SessionServer.
 * @brief @~english Implementation of the SessionServer interface.
 */

package ca.griis.speds.session.api;

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
 * @brief @~french Offre les services d'un serveur de la couche session.
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
public interface SessionServer {
  /**
   * @brief @~english «Description of the function»
   * @return «Return description»
   *
   * @brief @~french Indique un échange.
   * @return L'interface d'unité de données à transmettre à la couche supérieure.
   *
   * @par Tâches
   *      S.O.
   */
  @Deprecated
  String indicateDataExchange();

  /**
   * @brief @~english «Description of the function»
   * @return «Return description»
   *
   * @brief @~french Indique un échange.
   * @return L'interface d'unité de données à transmettre à la couche supérieure.
   *
   * @par Tâches
   *      S.O.
   */
  String indication();

  /**
   * @brief @~english «Description of the function»
   * @param idu «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Répond à un initiateur de requête sous une réponse avec des données à échanger.
   * @param idu L’interface d’unité de données de la couche supérieure et la couche transport
   *        sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  void response(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désalloue les ressources.
   *
   * @par Tâches
   *      S.O.
   */
  void close();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désalloue les ressources en gardant les informations de session.
   *
   * @par Tâches
   *      S.O.
   */
  void closePreservingSessionStates();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désalloue la session.
   *
   * @par Tâches
   *      S.O.
   */
  void clearSessionStates();
}
