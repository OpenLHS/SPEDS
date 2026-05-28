/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecProvider.
 * @brief @~english Contains description of SpecProvider class.
 */

package ca.griis.speds.toolkit.crypto.internal.provider;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.spec.SecuritySpec;
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
 * @brief @~french Offre de générer une spécification d'algorithme.
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
public class SpecProvider extends SpecBaseProvider {
  private final KeyGeneratorSpecProvider keyGenProvider;
  private final DhSpecProvider dhProvider;

  public SpecProvider(SecurityService service, CipherSuiteDtoReader reader) {
    super(service, reader);

    this.keyGenProvider = new KeyGeneratorSpecProvider(service, reader);
    this.dhProvider = new DhSpecProvider(service, reader);
  }

  @Override
  public Optional<SecuritySpec> getSpec(SpedsLayer spedsLayer, AlgorithmCategory category,
      Boolean keyGenerator) {
    Optional<SecuritySpec> spec = null;
    if (keyGenerator) {
      spec = keyGenProvider.getSpec(spedsLayer, category);
    } else if (category == AlgorithmCategory.DH) {
      spec = dhProvider.getSpec(spedsLayer);
    } else {
      spec = super.getSpec(spedsLayer, category, keyGenerator);
    }

    return spec;
  }
}
