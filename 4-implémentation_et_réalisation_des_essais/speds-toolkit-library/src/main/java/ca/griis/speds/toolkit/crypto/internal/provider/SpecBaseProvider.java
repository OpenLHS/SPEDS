/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecBaseProvider.
 * @brief @~english Contains description of SpecBaseProvider class.
 */

package ca.griis.speds.toolkit.crypto.internal.provider;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SecurityProfile;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.profile.SpedsSecurityProfileConverter;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.spec.SpecConverter;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.specid.GenSpecIdConverter;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.specid.SpecIdConverter;
import ca.griis.speds.toolkit.crypto.internal.reader.CipherSuiteDtoReader;
import java.util.Optional;

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
 * @brief @~french Définit comment générer une spéfication.
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
 *      2025-12-03 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public abstract class SpecBaseProvider {
  protected final SecurityService securityFacade;
  protected final CipherSuiteDtoReader reader;
  protected final SpecIdConverter converter;
  protected final GenSpecIdConverter genConverter;

  public SpecBaseProvider(SecurityService facade, CipherSuiteDtoReader reader) {
    this.securityFacade = facade;
    this.reader = reader;
    this.converter = new SpecIdConverter();
    this.genConverter = new GenSpecIdConverter();
  }

  protected Optional<SecuritySpec> getSpec(SpedsLayer spedsLayer, AlgorithmCategory category,
      Boolean keyGenerator) {
    var profile = reader.getSecurityProfile(spedsLayer, category);
    Optional<SecuritySpec> spec = getSpec(category, profile, keyGenerator);

    if (spec.isEmpty()) {
      var algo = reader.getSecurityAlgo(spedsLayer, category);
      if (algo.isPresent()) {
        spec = Optional.of(new SpecConverter().apply(algo.get()));
      }
    }

    return spec;
  }

  protected Optional<SecuritySpec> getSpec(
      AlgorithmCategory category, Optional<SecurityProfile> profile, Boolean keyGenerator) {
    Optional<SecuritySpec> spec = Optional.empty();

    if (profile.isPresent()) {
      final var secProfile = new SpedsSecurityProfileConverter().apply(profile.get());
      final var specs = securityFacade.getProfilSecuritySpecs(secProfile);
      final var specId = keyGenerator ? genConverter.apply(category) : converter.apply(category);
      spec = Optional.of(specs.get(specId.id()));
    }

    return spec;
  }
}
