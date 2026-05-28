/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 * 
 * @licence @@GRIIS_LICENCE@@
 * 
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french  Contient la classe ProtocolException.
 * @brief @~english Contains the ProtocolException class.
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
 * @brief @~french Définit le type d'exception en cas de problèmes avec le le protocole de
 *        communication.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class ProtocolException extends RuntimeException {
  private static final long serialVersionUID = -8946941936581815105L;

  /**
   * @param message
   */
  public ProtocolException(String message) {
    super(message);
  }

  /**
   * @param message
   * @param cause
   */
  public ProtocolException(String message, Throwable cause) {
    super(message, cause);
  }
}
