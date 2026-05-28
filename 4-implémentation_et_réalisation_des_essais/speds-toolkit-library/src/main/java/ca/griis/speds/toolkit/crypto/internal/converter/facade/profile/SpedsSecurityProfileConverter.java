/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpedsSecurityProfileConverter.
 * @brief @~english Contains description of SpedsSecurityProfileConverter class.
 */

package ca.griis.speds.toolkit.crypto.internal.converter.facade.profile;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import java.util.function.Function;

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
 * @brief @~french Convertit le profil de sécurité SPEDS au profil de sécurité du module de
 *        sécurité.
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
 *      2025-12-19 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SpedsSecurityProfileConverter implements
    Function<SpedsConfigItemDto.SecurityProfile, ca.griis.security.api.domain.SecurityProfile> {
  @Override
  public ca.griis.security.api.domain.SecurityProfile apply(
      SpedsConfigItemDto.SecurityProfile profile) {
    return profile.name().equals("STRONG") ? ca.griis.security.api.domain.SecurityProfile.Strongest
        : ca.griis.security.api.domain.SecurityProfile.Efficient;
  }
}
