/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ApplicationHost.
 * @brief @~english Contains description of ApplicationHost class.
 */

package ca.griis.speds.application.api;

import ca.griis.speds.application.internal.domain.ApplicationInterface;
import ca.griis.speds.presentation.api.PresentationHostEvent;
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
 * @brief @~french Définit les services d'un hôte de la couche application.
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
public interface ApplicationHost extends PresentationHostEvent {

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Soumet l'ApplicationInterface émise par l’utilisateur de la couche à la couche
   *        de protocole correspondante.
   * @param applicationInterface L’interface de données fournie par l'utilisateur de la couche.
   * @return CompletableFuture contenant éventuellement une ApplicationInterface produite par le
   *         traitement. La valeur peut être récupérée en appelant get() sur le CompletableFuture
   *         une fois le calcul terminé.
   *
   * @par Tâches
   *      S.O.
   */
  CompletableFuture<ApplicationInterface> submit(ApplicationInterface applicationInterface);

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
}
