/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe CipherException.
 * @brief @~english Implementation of the CipherException class.
 */

package ca.griis.speds.presentation.api.exception;

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
 * @brief @~french Exception représentant une erreur de chiffrement/déchiffrement.
 * @par Détails
 *      S.O.
 *
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-18 [MD] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class CipherException extends RuntimeException {
  private static final long serialVersionUID = 4246484932814888050L;

  public CipherException() {
    super();
  }

  public CipherException(String message) {
    super(message);
  }

  public CipherException(String message, Throwable cause) {
    super(message, cause);
  }

  public CipherException(Throwable cause) {
    super(cause);
  }
}
