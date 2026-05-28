/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe KeyGeneratorSpecProvider.
 * @brief @~english Contains description of KeyGeneratorSpecProvider class.
 */

package ca.griis.speds.toolkit.crypto.internal.provider;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.spec.SpecGenConverter;
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
 * @brief @~french .
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
public class KeyGeneratorSpecProvider extends SpecBaseProvider {
  KeyGeneratorSpecProvider(SecurityService facade, CipherSuiteDtoReader reader) {
    super(facade, reader);
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une spéfication d'algorithme de générateur de clés.
   * @param spedsLayer La couche de protocole de SPEDS.
   * @param category La catégorie d'u nalgorithme.
   * @return Spéfication d'algorithme de générateur de clés si possible.
   *
   * @par Tâches
   *      S.O.
   */
  public Optional<SecuritySpec> getSpec(SpedsLayer spedsLayer, AlgorithmCategory category) {
    var profile = reader.getSecurityProfile(spedsLayer, category);
    var spec = super.getSpec(category, profile, true);

    if (spec.isEmpty()) {
      var algo = reader.getSecurityAlgo(spedsLayer, category);
      if (algo.isPresent()) {
        spec = Optional.of(new SpecGenConverter().apply(algo.get()));
      }
    }

    return spec;
  }
}
