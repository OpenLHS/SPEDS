/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe MessageHandler.
 * @brief @~english Implementation of the MessageHandler class.
 */

package ca.griis.speds.session.internal.handler.indication;

import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import java.util.Optional;

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
 * @brief @~french Définition de l'interface pour un gestionnaire de message
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
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface MessageHandler {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère le type de message géré par ce gestionnaire
   * @return Le type du message
   *
   * @par Tâches
   *      S.O.
   */
  MsgType getHandledType();

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Gère le message reçu
   * @param message le message dépilé à traiter.
   *
   * @par Tâches
   *      S.O.
   */
  Optional<String> handle(ExpandedSidu message);
}
