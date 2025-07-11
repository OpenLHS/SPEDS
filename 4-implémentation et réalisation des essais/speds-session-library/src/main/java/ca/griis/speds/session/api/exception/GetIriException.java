/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe GetIriException.
 * @brief @~english Implementation of the GetIriException class.
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
 * @brief @~french Implémentation de GetIriException
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
public class GetIriException extends RuntimeException {
  private static final long serialVersionUID = 5434531295797442703L;

  public GetIriException(String message) {
    super(message);
  }

  public GetIriException(String message, Throwable cause) {
    super(message, cause);
  }

  public GetIriException(Throwable cause) {
    super(cause);
  }
}
