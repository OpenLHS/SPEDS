/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe InvalidPduIdException.
 * @brief @~english Contains description of InvalidPduIdException class.
 */

package ca.griis.speds.application.api.exception;

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
 * @brief @~french Le numéro de message ne correspond pas à une requête précédente
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
 *      2025-01-29 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class InvalidPduIdException extends RuntimeException {
  private static final long serialVersionUID = -2642900511709961731L;

  public InvalidPduIdException() {}

  /**
   * @param message
   */
  public InvalidPduIdException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public InvalidPduIdException(String message, Throwable cause) {
    super(message, cause);
  }
}
