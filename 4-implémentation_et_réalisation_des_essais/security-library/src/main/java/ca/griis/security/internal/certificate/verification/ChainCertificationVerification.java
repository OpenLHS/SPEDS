/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation la classe ChainCertificationVerification.
 * @brief @~english Implementation of the ChainCertificationVerification class.
 */

package ca.griis.security.internal.certificate.verification;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.security.api.domain.spec.certificate.CertificateVerifSpec;
import ca.griis.security.api.domain.spec.certificate.IntermediateCertificates;
import ca.griis.security.api.domain.spec.certificate.RootCertificates;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * @brief @~french Vérifie la chaîne de certification
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
public class ChainCertificationVerification {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ChainCertificationVerification.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la chaîne de certification.
   * @param certificateVerifSpec Spécification du vérificateur de certificat numérique
   * @param rootCertificates Un ou plusieurs certificats X509Certificate
   * @param interCertificates Aucun ou plusieurs X509Certificate
   * @param certificate Le certificat a valider
   * @param crls Les certificats CRL.
   * @return Boolean qui valide la signature numérique du certificat.
   *
   * @note La désactivation de OCSP ou de la vérification CRL peut être réalisée en désactivant le
   *       contrôle de révocation du JDK, c’est-à-dire en utilisant
   *       params.setRevocationEnabled(false). Actuellement, seul le mode CRL est pris en charge.
   * 
   * @par Tâches
   *      S.O.
   */
  public Boolean verifyCertificateChain(CertificateVerifSpec certificateVerifSpec,
      RootCertificates rootCertificates, IntermediateCertificates interCertificates,
      X509Certificate certificate, List<X509CRL> crls) {
    Boolean isValid = false;

    // Chaîne de certificats à partir des certificats intermédiaires et le certificat à vérifier
    List<X509Certificate> chain = new ArrayList<>();
    chain.add(certificate);
    chain.addAll(interCertificates.getCertificates());

    try {
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      CertPath certPath = certificateFactory.generateCertPath(chain);

      Set<TrustAnchor> trustAnchor = new HashSet<>();
      for (var rootCert : rootCertificates.getCertificates()) {
        trustAnchor.add(new TrustAnchor(rootCert, null));
      }

      PKIXParameters params = new PKIXParameters(trustAnchor);
      if (certificateVerifSpec.getRevocationVerifParam().equals("CRL")) {
        Collection<X509CRL> x509Crls = new ArrayList<>(crls);
        CertStore store =
            CertStore.getInstance("Collection", new CollectionCertStoreParameters(x509Crls));
        params.addCertStore(store);

        params.setRevocationEnabled(true);
      }

      final CertPathValidator validator =
          CertPathValidator.getInstance(certificateVerifSpec.getAlgo());
      validator.validate(certPath, params);
      isValid = true;
    } catch (CertificateException | CertPathValidatorException | InvalidAlgorithmParameterException
        | NoSuchAlgorithmException e) {
      logger.error(Error.GENERIC_ERROR, e);
    }

    return isValid;
  }
}
