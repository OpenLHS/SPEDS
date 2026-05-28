/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ParameterException.
 * @brief @~english Contains description of ParameterException class.
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
 * @brief @~french Un ou des paramètres nécessaires à l'initialisation de la couche manque
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
 *      2025-02-12 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class ParameterException extends RuntimeException {
  private static final long serialVersionUID = -6765126482857079974L;

  /**
   * @param message
   */
  public ParameterException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public ParameterException(String message, Throwable cause) {
    super(message, cause);
  }
}
