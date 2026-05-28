/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation la classe TemporalVerification.
 * @brief @~english Implementation of the TemporalVerification class.
 */

package ca.griis.security.internal.certificate.verification;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

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
 * @brief @~french Vérifie la validité temporelle d'un certificat.
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
 *      2025-12-11 [JM] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */

public class TemporalVerification {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(TemporalVerification.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la validité temporelle d’un certificat.
   * @param certificate Le certificat a valider
   * @return Boolean qui valide la validité temporelle du certificat.
   *
   * @par Tâches
   *      S.O.
   */
  public Boolean verifyCertificateTemporalValidity(X509Certificate certificate) {
    Boolean isValid = false;

    try {
      certificate.checkValidity();
      isValid = true;
    } catch (CertificateNotYetValidException | CertificateExpiredException e) {
      logger.error(Error.GENERIC_ERROR, e);
    }

    return isValid;
  }
}
