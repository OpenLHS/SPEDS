/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe NoResponseRequestException.
 * @brief @~english Implementation of the NoResponseRequestException class.
 */

package ca.griis.speds.session.api.exception;

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
 * @brief @~french Implémentation de NoResponseRequestException
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
 *      2025-07-03 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class NoResponseRequestException extends RuntimeException {
  private static final long serialVersionUID = -8927908538272749777L;

  public NoResponseRequestException(String message) {
    super(message);
  }

  public NoResponseRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoResponseRequestException(Throwable cause) {
    super(cause);
  }
}
