/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CertificateChecker.
 * @brief @~english Contains description of CertificateChecker class.
 */

package ca.griis.speds.network.internal.security;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.toolkit.security.api.DefaultCertificateFactory;
import java.security.cert.X509Certificate;
import org.apache.jena.iri.IRIException0;
import org.apache.jena.iri.IRIFactory;

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
 * @brief @~french Permet de vérifier un certificat.
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
 *      2026-02-16 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class CertificateChecker {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(CertificateChecker.class);
  private static final IRIFactory iriFactory = IRIFactory.iriImplementation();

  private final DefaultCertificateFactory factory;

  public CertificateChecker() {
    this.factory = new DefaultCertificateFactory();
  }

  public Boolean checkCertificate(X509Certificate certificate, String iri) {
    Boolean isValid = false;
    try {
      final var currentIri = iriFactory.construct(iri);
      isValid = factory.init().checkCertificateValidity(certificate, currentIri);
    } catch (IRIException0 e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    return isValid;
  }
}
