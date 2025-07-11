/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesPubEnvDto.
 * @brief @~english Implementation of the SesPubEnvDto class.
 */

package ca.griis.speds.session.internal.contract;

import ca.griis.js2p.gen.speds.session.api.dto.pub.IdentityDto;
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
 * @brief @~french Implémentation de SesPubEnvDto
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
public class SesPubEnvDto extends IdentityDto {
  private static final long serialVersionUID = -4924841955190277195L;

  public SesPubEnvDto() {
    super();
  }

  public SesPubEnvDto(String content, UUID session) {
    super(content, session);
  }
}
