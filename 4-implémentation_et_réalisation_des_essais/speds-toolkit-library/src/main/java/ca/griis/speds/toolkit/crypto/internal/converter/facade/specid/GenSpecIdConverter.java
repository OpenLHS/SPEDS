/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe GenSpecIdConverter.
 * @brief @~english Contains description of GenSpecIdConverter class.
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
 *        d'un algorithme de génération cryptographique.
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
public class GenSpecIdConverter implements Function<AlgorithmCategory, SpecId> {
  private static final Map<AlgorithmCategory, String> mapping = Map.of(
      AlgorithmCategory.SYMM, "AES-Gen",
      AlgorithmCategory.ASYM, "RSA-Encipherment-Signature-Gen",
      AlgorithmCategory.SIGN, "RSA-Encipherment-Signature-Gen",
      AlgorithmCategory.DH, "25519-DH-Gen");

  @Override
  public SpecId apply(AlgorithmCategory category) {
    return new SpecId(mapping.get(category));
  }
}
