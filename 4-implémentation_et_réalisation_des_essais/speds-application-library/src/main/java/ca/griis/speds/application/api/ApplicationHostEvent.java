/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface ApplicationHostEvent.
 * @brief @~english Implementation of the ApplicationHostEvent interface.
 */

package ca.griis.speds.application.api;

import ca.griis.speds.application.internal.domain.ApplicationInterface;
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
 * @brief @~french Définit les événements générés par un hôte application et transmis l'utilisateur
 *        de
 *        cette couche.
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
 *      2026-03-04 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface ApplicationHostEvent {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Notifie un message échangé à l'utilisateur de la couche.
   * @param applicationInterface L'interface de données entre l'utilisateur de la couche application
   *        et la couche application.
   *
   * @par Tâches
   *      S.O.
   */
  CompletableFuture<ApplicationInterface> notify(ApplicationInterface applicationInterface);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Notifie une exception à l'utilisateur de la couche.
   *        liaison.
   * @param exception L’exception échangée à l'utilisateur de la couche.
   *
   * @par Tâches
   *      S.O.
   */
  void notifyException(Exception exception);
}
