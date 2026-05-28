/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DhSpecGenConverter.
 * @brief @~english Contains description of DhSpecGenConverter class.
 */

package ca.griis.speds.toolkit.crypto.internal.converter.facade.spec;

import ca.griis.security.api.domain.spec.SecuritySpec;
import ca.griis.security.api.domain.spec.dh.EfficientX25519KeyAgreementFnSpec;
import ca.griis.security.api.domain.spec.dh.StrongX25519KeyAgreementFnSpec;
import java.util.Map;
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
 * @brief @~french Convertit une catégorie d'algorithme de SPEDS à une spécification
 *        d'un algorithme cryptographique d'échange de clé.
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
public class DhSpecGenConverter implements Function<DhGenAlgo, SecuritySpec> {
  private static final Map<DhGenAlgo, SecuritySpec> mapping = Map.ofEntries(
      Map.entry(new DhGenAlgo("X25519", "AES128-GCM"), new EfficientX25519KeyAgreementFnSpec()),
      Map.entry(new DhGenAlgo("X25519", "AES256-GCM"), new StrongX25519KeyAgreementFnSpec()));

  @Override
  public SecuritySpec apply(DhGenAlgo algo) {
    return mapping.get(algo);
  }
}
