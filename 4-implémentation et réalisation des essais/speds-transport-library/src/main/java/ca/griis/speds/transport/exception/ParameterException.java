/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe ParameterException.
 * @brief @~english Contains description of ParameterException class.
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
 * @brief @~french Un ou des paramètres nécessaires à l'initialisation de la couche est manquant
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
 *      2025-02-18 [JM] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class ParameterException extends RuntimeException {
  private static final GriisLogger logger = getLogger(ParameterException.class);

  @Serial
  private static final long serialVersionUID = 5017588510216216776L;

  public ParameterException(String message) {
    super(message);
    logger.trace(Trace.ENTER_METHOD_1, "message", message);
  }
}
