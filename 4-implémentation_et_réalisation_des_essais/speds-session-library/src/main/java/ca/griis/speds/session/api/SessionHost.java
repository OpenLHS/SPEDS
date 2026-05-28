/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface SessionHost.
 * @brief @~english Implementation of the SessionHost interface.
 */

package ca.griis.speds.session.api;

import java.util.Optional;
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
 * @brief @~french Offre les services d'un hôte de la couche session.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      <p>
 *      À l’heure actuelle, la reprise des messages perdus n’est pas implémentée, mais il est prévu
 *      que, dans une version ultérieure, la session prenne en charge ce mécanisme.
 *      </p>
 *      <p>
 *      L’initiateur de la session est considéré comme le contrôleur de celle-ci. Par conséquent, il
 *      est attendu que la terminaison de la session soit imposée au partenaire par un initiateur.
 *      </>
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface SessionHost {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Soumet l'IDU émise par l’utilisateur de la couche à la couche de protocole
   *        correspondante.
   * @param idu L’interface d’unité de données fournie par l'utilisateur de la couche sous le format
   *        JSON.
   * @return CompletableFuture contenant éventuellement une IDU (Optional<String>) produite par le
   *         traitement. La valeur peut être récupérée en appelant get() sur le CompletableFuture
   *         une fois le calcul terminé.
   *
   * @note 2026-01-30 [FO] - Par exemple, pour une entité, l’IDU d’entrée pourrait représenter une
   *       requête et la sortie pourrait être un futur (CompletableFuture) fournissant une IDU de
   *       confirmation une fois le traitement terminé.
   * @par Tâches
   *      S.O.
   */
  CompletableFuture<Optional<String>> submitIdu(String idu);

  /**
   * @brief @~english «Description of the function»
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
