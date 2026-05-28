/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpecIdConverter.
 * @brief @~english Contains description of SpecIdConverter class.
 */

package ca.griis.speds.toolkit.crypto.internal.converter.facade.specid;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
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
 * @brief @~french Convertit une catégorie d'algorithme de SPEDS à un identifiant de spécification
 *        d'un algorithme cryptographique.
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
public class SpecIdConverter implements Function<AlgorithmCategory, SpecId> {
  private static final Map<AlgorithmCategory, String> mapping = Map.of(
      AlgorithmCategory.SYMM, "AES-GCM",
      AlgorithmCategory.ASYM, "RSA-Encipherment",
      AlgorithmCategory.HASH, "Hashing",
      AlgorithmCategory.SIGN, "RSA-Signature",
      AlgorithmCategory.DH, "25519-DH");

  @Override
  public SpecId apply(AlgorithmCategory category) {
    return new SpecId(mapping.get(category));
  }
}
