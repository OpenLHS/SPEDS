/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe DataReplyMessages.
 * @brief @~english Implementation of the DataReplyMessages class.
 */

package ca.griis.speds.transport.service.server.datatype;

import java.util.Objects;

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
 * @brief @~french Contient les deux réponses du processus ExchangeDataReply
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
 *      2025-03-25 [JM] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public record DataReplyMessages (String response34, String response45) {

  public DataReplyMessages {
    Objects.requireNonNull(response34);
    Objects.requireNonNull(response45);
  }
}
