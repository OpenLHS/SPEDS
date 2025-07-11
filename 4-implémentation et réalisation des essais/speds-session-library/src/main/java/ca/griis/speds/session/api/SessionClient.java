/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface SessionClient.
 * @brief @~english Implementation of the SessionClient interface.
 */

package ca.griis.speds.session.api;

import java.util.concurrent.CompletableFuture;

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
 * @brief @~french Offre les services d'un client de la couche session.
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
public interface SessionClient {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Demande d'échanger des données (non bloquant).
   * @param idu L’interface d’unité de données de la couche supérieure et la couche session
   *        sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  @Deprecated
  void request(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Demande d'établir des données en bloquant tant que la fin de session n'est pas
   *        terminé.
   * @param idu L’interface d’unité de données de la couche supérieure et la couche session
   *        sous le format JSON.
   * @return Suivre l'état d'exécution de la tâche.
   * 
   * @note [FO] 2025-07-07 - Contrairement à une méthode classique, cette version permet à
   *       l'appelant d'attendre explicitement la fin de la tâche, de gérer les erreurs asynchrones
   *       et d'exécuter des suites de traitements en non-bloquant.
   * 
   * @par Tâches
   *      S.O.
   */
  CompletableFuture<Void> requestFuture(String idu);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Confirme l’échange de données.
   * @return L’interface d’unité de données de la couche supérieure et la couche session
   *         sous le format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  String confirm();

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
   * @brief @~french Désalloue les ressources en préservant tous les états de session qui sont en
   *        cours.
   * 
   * @note Un état de session représente toutes les informations sauvegardées pour assurer le
   *       fonctionnement d'une session sur le protocole.
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
   * @brief @~french Nettoie tous les états de session en cours.
   * 
   * @note Un état de session représente toutes les informations sauvegardées pour assurer le
   *       fonctionnement d'une session sur le protocole.
   *
   * @par Tâches
   *      S.O.
   */
  void clearSessionStates();
}
