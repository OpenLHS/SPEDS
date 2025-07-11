/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe KeyAgreementException.
 * @brief @~english Contains description of KeyAgreementException class.
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
 * @brief @~french Impossible de générer la clé publique et privée selon l'algorithme d'accord de
 *        clé
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
 *      2025-03-19 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class KeyAgreementException extends RuntimeException {

  private static final long serialVersionUID = -8927908538272749777L;

  public KeyAgreementException(String message) {
    super(message);
  }

  public KeyAgreementException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyAgreementException(Throwable cause) {
    super(cause);
  }
}
