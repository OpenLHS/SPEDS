/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe InvalidPduId.
 * @brief @~english Implementation of the InvalidPduId class.
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
 * @brief @~french Exception représentant une erreur de réponse de message;
 *        L'identifiant d'un mesasge de réponse doit concorder avec l'identifiant de l'envoi.
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
public class InvalidPduId extends RuntimeException {
  private static final long serialVersionUID = -6917355435908648514L;

  public InvalidPduId() {
    super();
  }

  public InvalidPduId(String message) {
    super(message);
  }

  public InvalidPduId(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidPduId(Throwable cause) {
    super(cause);
  }
}
