/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe DefaultCryptographyFactory.
 * @brief @~english Contains description of DefaultCryptographyFactory class.
 */

package ca.griis.speds.toolkit.crypto.internal;

import ca.griis.js2p.gen.speds.toolkit.api.dto.CiphersuiteDto;
import ca.griis.security.api.service.DefaultSecurityService;
import ca.griis.speds.toolkit.crypto.api.CryptographyFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.crypto.internal.reader.CipherSuiteDtoReader;
import ca.griis.speds.toolkit.serializer.ToolkitSharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

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
 * @brief @~french Implémente une fabrique pour construire un service cryptographique.
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
 *      2025-11-28 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class DefaultCryptographyFactory implements CryptographyFactory {
  @Override
  public CryptographyService initCipherSuite(String configJson)
      throws JsonProcessingException {
    final var cipherSuite = ToolkitSharedObjectMapper.getInstance().getMapper()
        .readValue(configJson, CiphersuiteDto.class);
    final var reader = new CipherSuiteDtoReader(cipherSuite);
    return new DefaultCryptographyService(new DefaultSecurityService(), reader);
  }
}
