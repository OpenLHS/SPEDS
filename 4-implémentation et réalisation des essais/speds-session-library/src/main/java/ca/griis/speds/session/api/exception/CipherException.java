/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CipherException.java
 * @brief @~english Contains description of CipherException.java class.
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
 * @brief @~french Implémentation de CipherException
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
 *      2025-03-18 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class CipherException extends RuntimeException {
  private static final long serialVersionUID = 8665806013564789700L;

  public CipherException(String message) {
    super(message);
  }

  public CipherException(Throwable cause) {
    super(cause);
  }

  public CipherException(String message, Throwable cause) {
    super(message, cause);
  }
}
