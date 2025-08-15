/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SilentIgnoredException.
 * @brief @~english Contains description of SilentIgnoredException class.
 */

package ca.griis.speds.transport.service;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;

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
 *      2025-08-07 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SilentIgnoredException extends RuntimeException {
  private static final GriisLogger logger = getLogger(SilentIgnoredException.class);

  private static final long serialVersionUID = -5833845358516454488L;

  public SilentIgnoredException(String message) {
    super(message);
    logger.trace(Trace.ENTER_METHOD_1, "message", message);
  }
}
