/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DeserializationException.
 * @brief @~english Contains description of DeserializationException class.
 */

package ca.griis.speds.link.api.exception;

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
 * @brief @~french Erreur lors de la sérialisation d'un object en une chaîne de caractères json
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
public class SerializationException extends RuntimeException {
  private static final long serialVersionUID = 5017588510216216776L;

  public SerializationException() {}

  /**
   * @param message
   */
  public SerializationException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public SerializationException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
