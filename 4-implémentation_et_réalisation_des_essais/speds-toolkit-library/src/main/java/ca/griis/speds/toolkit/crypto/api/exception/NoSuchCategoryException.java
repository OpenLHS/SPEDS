/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe NoSuchCategoryException.
 * @brief @~english Contains description of NoSuchCategoryException class.
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
 * @brief @~french Erreur soulevée lorsque la catégorie d’algorithme n’est pas disponible pour la
 *        couche fournie.
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
public class NoSuchCategoryException extends RuntimeException {
  private static final long serialVersionUID = -4523821720745709038L;

  public NoSuchCategoryException(String message) {
    super(message);
  }

  public NoSuchCategoryException(Throwable cause) {
    super(cause);
  }

  public NoSuchCategoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
