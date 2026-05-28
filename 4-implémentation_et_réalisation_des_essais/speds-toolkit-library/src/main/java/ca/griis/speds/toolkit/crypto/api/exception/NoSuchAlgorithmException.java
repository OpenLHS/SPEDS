/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe NoSuchAlgorithmException.
 * @brief @~english Contains description of NoSuchAlgorithmException class.
 */

package ca.griis.speds.toolkit.crypto.api.exception;

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
 * @brief @~french Erreur soulevée lorsque l’algorithme fournie n’est pas disponible dans
 *        l’environnement.
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
 *      2025-11-24 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class NoSuchAlgorithmException extends RuntimeException {
  private static final long serialVersionUID = 7846336813239798314L;

  public NoSuchAlgorithmException(String message) {
    super(message);
  }

  public NoSuchAlgorithmException(Throwable cause) {
    super(cause);
  }

  public NoSuchAlgorithmException(String message, Throwable cause) {
    super(message, cause);
  }
}
