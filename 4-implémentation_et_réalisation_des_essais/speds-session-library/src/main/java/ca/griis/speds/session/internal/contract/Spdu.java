/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe Spdu.
 * @brief @~english Implementation of the Spdu class.
 */

package ca.griis.speds.session.internal.contract;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.ProtocolDataUnit3SESDto;

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
 * @brief @~french Implémentation de Spdu
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
public class Spdu extends ProtocolDataUnit3SESDto {
  private static final long serialVersionUID = -1780245899254914261L;

  public Spdu() {
    super();
  }

  public Spdu(HeaderDto header, String stamp, Object content) {
    super(header, stamp, content);
  }
}
