/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe DefaultCertificateService.
 * @brief @~english Implementation of the DefaultCertificateService class.
 */

package ca.griis.speds.toolkit.security.api;

import ca.griis.security.api.SecurityService;
import ca.griis.security.api.domain.spec.certificate.Hostname;
import ca.griis.security.api.domain.spec.certificate.usage.CertificateKeyUsages;
import ca.griis.security.api.domain.spec.certificate.usage.KeyUsageType;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Set;
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
 * @brief @~french Implémente le service de sécurité.
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
 *      2025-11-24 [FO] - Implémentation initiale.<br>
 *
 * @par Tâches
 *      S.O.
 */
public class DefaultCertificateService implements CertificateService {
  private final SecurityService facade;

  public DefaultCertificateService(SecurityService facade) {
    this.facade = facade;
  }

  @Override
  public Boolean checkCertificateValidity(X509Certificate certificate, IRI iri) {
    final CertificateKeyUsages usages =
        new CertificateKeyUsages(Set.of(
            KeyUsageType.DIGITAL_SIGNATURE,
            KeyUsageType.KEY_ENCIPHERMENT,
            KeyUsageType.KEY_AGREEMENT));
    final URI uri = URI.create(iri.toString());
    final Hostname hostname = new Hostname(uri.getHost());
    final Boolean isValid = facade.verifyCertificateTemporalValidity(certificate)
        && facade.verifyCertificateExtensions(certificate, hostname, usages);

    return isValid;
  }
}
