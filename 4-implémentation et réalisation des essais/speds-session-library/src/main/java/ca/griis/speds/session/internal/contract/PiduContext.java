/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe PiduContext.
 * @brief @~english Implementation of the PiduContext class.
 */

package ca.griis.speds.session.internal.contract;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import java.util.UUID;

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
 * @brief @~french Implémentation de PiduContext
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
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class PiduContext extends Context23Dto {
  private static final long serialVersionUID = 8453342058715363764L;

  public PiduContext() {
    super();
  }

  public PiduContext(String pga, String sourceCode, String destinationCode, String sdek,
      UUID trackingNumber, Object options) {
    super(pga, sourceCode, destinationCode, sdek, trackingNumber, options);
  }
}
