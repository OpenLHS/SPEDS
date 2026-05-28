/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpedsConfigItemDtoReader.
 * @brief @~english Contains description of SpedsConfigItemDtoReader class.
 */

package ca.griis.speds.toolkit.crypto.internal.reader;

import ca.griis.js2p.gen.speds.toolkit.api.dto.CiphersuiteDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer;
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
 * @brief @~french Offre une lecture d'un élément de la suite cryptographique.
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
public class SpedsConfigItemDtoReader {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french «Description de la fonction»
   * @param layer Couche de protocole.
   * @param category Catégorie d'algorithme.
   * @param cipherSuite Un élément de la suite cryptographique.
   * @return Si disponible, retourne l'élément de la configuration.
   *
   * @par Tâches
   *      S.O.
   */
  public Optional<SpedsConfigItemDto> getSpedsConfigItemDto(
      SpedsLayer layer, AlgorithmCategory category, CiphersuiteDto cipherSuite) {
    Optional<SpedsConfigItemDto> result = cipherSuite.getSpedsProfile().stream()
        .filter(x -> x.getSpedsLayer() == layer && x.getAlgorithmCategory() == category)
        .findFirst();
    return result;
  }
}
