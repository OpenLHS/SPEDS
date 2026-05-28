/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecAlgoProvider.
 * @brief @~english Contains description of SpecAlgoProvider class.
 */

package ca.griis.speds.toolkit.crypto.internal.provider;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
import ca.griis.security.api.domain.spec.SecuritySpec;
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
 * @brief @~french Offre de générer une spéfication en fonction du nom d'algorithme.
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
 *      2026-01-07 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SpecAlgoProvider {
  private final SpecProvider provider;

  public SpecAlgoProvider(SpecProvider provider) {
    this.provider = provider;
  }

  /**
   * 
   * @fn a
   *
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère la spécification à partir du no mde l'algorithme et la couche du
   *        protocole.
   * @param spedsLayer La couche de protocole de SPEDS.
   * @param algorithm Le nom normalisé de l'algorithme.
   * @return La spécification de l'algorithme si possible.
   *
   * @note 2026-01-07 [FO] - Vu que le nom d'algorithme est indentique le générateur de clés et la
   *       fonction de X25519 et Ed25519, les paramètres de la nction soit choisie et non du
   *       générateur de clés.
   *
   * @par Tâches
   *      S.O.
   */
  public Optional<SecuritySpec> getSpec(SpedsLayer spedsLayer, String algorithm) {
    Optional<SecuritySpec> spec = getAsymSpec(spedsLayer, algorithm);

    if (spec.isEmpty() && algorithm.equals("DRBG")) {
      spec = Optional.of(new CsprngSpecProvider().getSpec());
    } else if (algorithm.equals("SHA-256") || algorithm.equals("SHA-512")) {
      spec = provider.getSpec(spedsLayer, AlgorithmCategory.HASH, false);
    } else if (algorithm.equals("AES")) {
      spec = provider.getSpec(spedsLayer, AlgorithmCategory.SYMM, true);
    } else if (algorithm.equals("AES/GCM/NoPadding")) {
      spec = provider.getSpec(spedsLayer, AlgorithmCategory.SYMM, false);
    }

    if (spec.isPresent() && spec.get().getAlgo().equals(algorithm) == false) {
      spec = Optional.empty();
    }

    return spec;
  }

  private Optional<SecuritySpec> getAsymSpec(SpedsLayer spedsLayer, String algorithm) {
    Optional<SecuritySpec> spec = Optional.empty();

    if (algorithm.equals("RSA")) {
      var rsa = provider.getSpec(spedsLayer, AlgorithmCategory.ASYM, true);
      spec = rsa.isEmpty() ? provider.getSpec(spedsLayer, AlgorithmCategory.SIGN, true) : rsa;
    } else if (algorithm.equals("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")) {
      spec = provider.getSpec(spedsLayer, AlgorithmCategory.ASYM, false);
    } else if (algorithm.equals("X25519")) {
      spec = provider.getSpec(spedsLayer, AlgorithmCategory.DH, false);
    } else if (algorithm.equals("RSASSA-PSS") || algorithm.equals("Ed25519")) {
      spec = provider.getSpec(spedsLayer, AlgorithmCategory.SIGN, false);
    }

    return spec;
  }
}
