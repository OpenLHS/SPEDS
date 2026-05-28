/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface PresentationHostEvent.
 * @brief @~english Implementation of the PresentationHostEvent interface.
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
 * @brief @~french Définit les événements générés par un hôte présentation et transmis l'utilisateur
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
 *      2026-01-30 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface PresentationHostEvent {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Notifie une IDU échangée à l'utilisateur de la couche.
   * @param idu L’interface d’unité de données échangée à l'utilisateur de la couche sous le
   *        format JSON.
   *
   * @par Tâches
   *      S.O.
   */
  void notifyIdu(String idu);

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
