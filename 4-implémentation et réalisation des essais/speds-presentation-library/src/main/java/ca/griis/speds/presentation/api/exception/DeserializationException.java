/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe DeserializationException.
 * @brief @~english Implementation of the DeserializationException class.
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
 * @brief @~french Exception représentant une erreur de désérialisation.
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
public class DeserializationException extends RuntimeException {
  private static final long serialVersionUID = -8359092072521944013L;

  public DeserializationException() {
    super();
  }

  public DeserializationException(String message) {
    super(message);
  }

  public DeserializationException(String message, Throwable cause) {
    super(message, cause);
  }

  public DeserializationException(Throwable cause) {
    super(cause);
  }
}
