/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe MissingAuthenticationException.
 * @brief @~english MissingAuthenticationException class implementation.
 */

package ca.griis.speds.network.service.exception;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;

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
 * @brief @~french Erreur soulevée lorsque le certificat pour l'authentification est manquant.
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
 *      2025-02-24 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class MissingAuthenticationException extends RuntimeException {
  private static final GriisLogger logger = getLogger(MissingAuthenticationException.class);

  public MissingAuthenticationException(String message) {
    super(message);
    logger.trace(Trace.ENTER_METHOD_1, "message", message);
  }

  public MissingAuthenticationException(String message, Throwable cause) {
    super(message, cause);
    logger.trace(Trace.ENTER_METHOD_2, "message", message, "cause", cause);
  }
}
