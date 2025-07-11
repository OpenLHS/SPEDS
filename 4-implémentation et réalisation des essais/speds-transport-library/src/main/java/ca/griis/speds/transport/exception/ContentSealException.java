/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ContentSealException.
 * @brief @~english Contains description of ContentSealException class.
 */

package ca.griis.speds.transport.exception;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import java.io.Serial;

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
 * @brief @~french Echec lors de la vérification du sceau du contenu
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
 *      2025-03-03 [JM] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class ContentSealException extends RuntimeException {
  private static final GriisLogger logger = getLogger(ContentSealException.class);

  @Serial
  private static final long serialVersionUID = 5017588510216216776L;

  public ContentSealException(String message) {
    super(message);
    logger.trace(Trace.ENTER_METHOD_1, "message", message);
  }
}
