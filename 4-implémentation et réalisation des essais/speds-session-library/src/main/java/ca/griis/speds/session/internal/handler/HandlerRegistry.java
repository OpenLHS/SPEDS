/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe HandlerRegistry.
 * @brief @~english Implementation of the HandlerRegistry class.
 */

package ca.griis.speds.session.internal.handler;

import java.util.List;

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
 * @brief @~french Définit l'interface d'un registre de gestionnaire interne.
 * @par Details
 *      L'ordre attendu d'un échange va ainsi:
 *
 *      presentation --> SES_PUB_ENV ↔ SES_PUB_REC
 *      SES_SAK_ENV ↔ SES_SAK_REC
 *      SES_CLE_ENV ↔ SES_CLE_REC
 *      (presentation <-- SES_MSG_ENV ↔ SES_MSG_REC --> presentation)+
 *      SES_FIN_ENV ↔ SES_FIN_REC
 *
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
public interface HandlerRegistry {
  List<MessageHandler> getHandlers();
}
