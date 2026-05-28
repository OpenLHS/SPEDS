/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SessionParameters.
 * @brief @~english SessionParameters class implementation.
 */

package ca.griis.speds.session.integration;

import ca.griis.js2p.gen.speds.session.api.dto.OptionsDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.util.Objects;
import org.apache.jena.iri.IRI;

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
 * @brief @~french «Brève description de la composante (classe, interface, ...)»
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-06-25 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public record SessionParameters (VersionDto spedsDto,
    CertificatePrivateKeysEntry certificatePrivateKeysEntry, String code, IRI iri) {
  public SessionParameters {
    Objects.requireNonNull(spedsDto);
    Objects.requireNonNull(certificatePrivateKeysEntry);
    Objects.requireNonNull(code);
    Objects.requireNonNull(iri);
  }

  public SessionParameters(CryptographyService cryptographyService, OptionsDto options, String code,
      IRI iri) throws ParameterException {
    this(new VersionDto(options.getSpedsVersion(), options.getCertificate()),
        new SessionSecurityService(cryptographyService).getCertificatePrivateKey(
            options.getCertificate(),
            options.getPrivateKey()),
        code, iri);
  }
}
