/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation la classe SignatureVerification.
 * @brief @~english Implementation of the SignatureVerification class.
 */

package ca.griis.security.internal.certificate.verification;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
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
 * @brief @~french Vérifie la validité de la signature numérique d'un certificat.
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
public class SignatureVerification {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SignatureVerification.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la signature numérique d’un certificat.
   * @param certificate Le certificat a valider
   * @param publicKey La clé publique du certificat qui a émis le certificat
   * @return Boolean qui valide la signature numérique du certificat.
   *
   * @par Tâches
   *      S.O.
   */
  public Boolean verifyCertificateSignature(X509Certificate certificate, PublicKey publicKey) {
    Boolean isValid = false;

    try {
      certificate.verify(publicKey);
      isValid = true;
    } catch (SignatureException | CertificateException | NoSuchAlgorithmException
        | InvalidKeyException | NoSuchProviderException e) {
      logger.error(Error.GENERIC_ERROR, e);
    }

    return isValid;
  }
}
