/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SilentIgnoreException.
 * @brief @~english Implementation of the SilentIgnoreException class.
 */

package ca.griis.speds.session.internal.processing;

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
 * @brief @~french Implémente une erreur de contrôle de flux interne ignorée silencieusement
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
public class SilentIgnoreException extends Exception {
  private static final long serialVersionUID = 5569591395034165263L;

  public SilentIgnoreException(String message) {
    super(message);
  }

  public SilentIgnoreException(String message, Throwable cause) {
    super(message, cause);
  }

  public SilentIgnoreException(Throwable cause) {
    super(cause);
  }
}
