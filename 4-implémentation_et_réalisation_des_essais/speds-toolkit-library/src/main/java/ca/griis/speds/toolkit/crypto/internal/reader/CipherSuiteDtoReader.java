/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CipherSuiteDtoReader.
 * @brief @~english Contains description of CipherSuiteDtoReader class.
 */

package ca.griis.speds.toolkit.crypto.internal.reader;

import ca.griis.js2p.gen.speds.toolkit.api.dto.CiphersuiteDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SecurityProfile;
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
 * @brief @~french Lecture d'un fichier de configuration d'une suite cryptographique de SPEDS.
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
public class CipherSuiteDtoReader {
  private final CiphersuiteDto cipherSuite;
  private final SpedsConfigItemDtoReader reader;

  public CipherSuiteDtoReader(CiphersuiteDto cipherSuite) {
    this.cipherSuite = cipherSuite;
    this.reader = new SpedsConfigItemDtoReader();
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère le profil de sécurité.
   * @param layer Couche de protocole.
   * @param category Catégorie de l'algorithme.
   * @return Si disponible, le profil de sécurité.
   *
   * @par Tâches
   *      S.O.
   */
  public Optional<SecurityProfile> getSecurityProfile(SpedsLayer layer,
      AlgorithmCategory category) {
    Optional<SecurityProfile> result = Optional.empty();
    Optional<SpedsConfigItemDto> config =
        reader.getSpedsConfigItemDto(layer, category, cipherSuite);

    if (config.isPresent() && config.get().getSecurityProfile() != null) {
      result = Optional.of(config.get().getSecurityProfile());
    }

    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère l'algorithme de sécurité.
   * @param layer Couche de protocole.
   * @param category Catégorie de l'algorithme.
   * @return Si disponible, l'algorithme de sécurité.
   *
   * @par Tâches
   *      S.O.
   */
  public Optional<String> getSecurityAlgo(SpedsLayer layer, AlgorithmCategory category) {
    Optional<String> result = Optional.empty();
    Optional<SpedsConfigItemDto> config =
        reader.getSpedsConfigItemDto(layer, category, cipherSuite);

    if (config.isPresent() && config.get().getSecurityAlgorithm() != null) {
      result = Optional.of(config.get().getSecurityAlgorithm());
    }

    return result;
  }
}
