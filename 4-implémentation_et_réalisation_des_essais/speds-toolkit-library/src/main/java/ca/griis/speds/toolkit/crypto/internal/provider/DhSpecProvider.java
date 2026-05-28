/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DhSpecProvider.
 * @brief @~english Contains description of DhSpecProvider class.
 */

package ca.griis.speds.toolkit.crypto.internal.provider;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.spec.DhGenAlgo;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.spec.DhSpecGenConverter;
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
 * @brief @~french Offre de génèrer une spéfication d'algorithme de Diffie-Hellman.
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
public class DhSpecProvider extends SpecBaseProvider {
  DhSpecProvider(SecurityService facade, CipherSuiteDtoReader reader) {
    super(facade, reader);
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une spéfication d'algorithme de Diffie-Hellman à partir d'une couche de
   *        protocole.
   * @param spedsLayer La couche de protocole de SPEDS.
   * @return Spéfication d'algorithme de Diffie-Hellman si possible.
   *
   * @par Tâches
   *      S.O.
   */
  public Optional<SecuritySpec> getSpec(SpedsLayer spedsLayer) {
    var profile = reader.getSecurityProfile(spedsLayer, AlgorithmCategory.DH);
    Optional<SecuritySpec> spec = super.getSpec(AlgorithmCategory.DH, profile, false);

    if (spec.isEmpty()) {
      var dhAlgo = reader.getSecurityAlgo(spedsLayer, AlgorithmCategory.DH);
      var symmAlgo = reader.getSecurityAlgo(spedsLayer, AlgorithmCategory.SYMM);
      if (dhAlgo.isPresent() && symmAlgo.isPresent()) {
        var dhGenAlgo = new DhGenAlgo(dhAlgo.get(), symmAlgo.get());
        spec = Optional.of(new DhSpecGenConverter().apply(dhGenAlgo));
      }
    }

    return spec;
  }
}
