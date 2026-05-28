/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface NetworkHost.
 * @brief @~english Implementation of the NetworkHost interface.
 */

package ca.griis.speds.network.api;

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
 * @brief @~french Définit les services d'un hôte de la couche réseau.
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
public interface NetworkHost {
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
   * 
   * @note 2026-02-23 [FO] - Dans le cas qu'on soumet une réponse, celle-ci doit être associée à une
   *       indication de message préalablement émise. Ainsi, l’interface de l’unité de données reçue
   *       en entrée dans le cadre d’une réponse de transfert doit contenir un élément clé-valeur,
   *       dont le format respecte une structure de type dictionnaire (map). Ce dictionnaire doit
   *       comporter une clé nommée `TN`, dont la valeur correspond à l’identifiant d’un message
   *       précédemment reçu via une indication de message.
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
}
