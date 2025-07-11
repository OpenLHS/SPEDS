/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CertificateVerification.java
 * @brief @~english Contains description of CertificateVerification.java class.
 */

package ca.griis.speds.network.service.verification;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
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
 * @brief @~french «Description brève du composant (classe, interface, ...)»
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
 *      2025-07-01 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class CertificateVerification {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(CertificateVerification.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french «Description de la fonction»
   * @param certificatePem
   * @return
   * @exception «nom de l'exception» «Description de l'exception»
   * @return «Description du retour»
   */
  public static boolean verifyCertificate(String certificatePem, String iri) {
    boolean verify = true;
    try {
      final CertificateFactory cf =
          CertificateFactory.getInstance(CertificatePrivateKeyPair.CERTIFICATE_ALGORITHM);

      X509Certificate certificate = (X509Certificate) cf.generateCertificate(
          new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      if (certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal())) {
        certificate.verify(certificate.getPublicKey());
        IRIFactory iriFactory = IRIFactory.iriImplementation();

        try {
          IRI currentIri = iriFactory.construct(iri);
          if (!getCommonName(certificate).equals(currentIri.getASCIIHost())) {
            verify = false;
          }
        } catch (IRIException | MalformedURLException e) {
          logger.error(Error.GENERIC_ERROR, "IRI problem.");
        }
      } else {
        verify = false;
      }
    } catch (CertificateException | InvalidKeyException | NoSuchAlgorithmException
        | NoSuchProviderException | SignatureException e) {
      logger.error(Error.GENERIC_ERROR, "Host certificate is missing.");
      verify = false;
    }
    return verify;
  }

  private static String getCommonName(X509Certificate certificate) {
    X500Principal subjectX500Principal = certificate.getSubjectX500Principal();
    String dn = subjectX500Principal.getName();

    // Parse the DN string to find the CN
    String commonName = null;
    String[] dnComponents = dn.split(",");
    for (String component : dnComponents) {
      component = component.trim();
      if (component.startsWith("CN=")) {
        commonName = component.substring(3).trim(); // Remove "CN=" prefix
        break;
      }
    }

    String unescapedCommonName = StringEscapeUtils.unescapeJava(commonName);
    return unescapedCommonName;
  }
}
