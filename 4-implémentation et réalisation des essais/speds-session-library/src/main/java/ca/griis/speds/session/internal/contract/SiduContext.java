/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SiduContext.
 * @brief @~english Implementation of the SiduContext class.
 */

package ca.griis.speds.session.internal.contract;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
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
 * @brief @~french Implémentation de SiduContext
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
public class SiduContext extends Context34Dto {
  private static final long serialVersionUID = 6820746089271685607L;

  public SiduContext() {
    super();
  }

  public SiduContext(String sourceCode, String destinationCode, String sourceIri,
      UUID trackingNumber, String destinationIri, Object options) {
    super(sourceCode, destinationCode, sourceIri, trackingNumber, destinationIri, options);
  }
}
