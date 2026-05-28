/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface TransportHost.
 * @brief @~english Implementation of the TransportHost interface.
 */

package ca.griis.speds.transport.api;

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
 * @brief @~french Offre les services d'un hôte de la couche transport.
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
public interface TransportHost {
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
}
