/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation la classe CertificateExtensionsVerification.
 * @brief @~english Implementation of the CertificateExtensionsVerification class.
 */

package ca.griis.security.internal.certificate.verification;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.security.api.domain.spec.certificate.Hostname;
import ca.griis.security.api.domain.spec.certificate.usage.CertificateKeyUsages;
import ca.griis.security.api.domain.spec.certificate.usage.KeyUsageType;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.apache.commons.text.StringEscapeUtils;

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
 * @brief @~french Vérifie les extensions du certificat.
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
public class CertificateExtensionsVerification {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(CertificateExtensionsVerification.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie les extensions du certificat.
   * @param certificate Le certificat a valider
   * @param hostname Le nom d'hôte du serveur à vérifier
   * @param certificateKeyUsages Les usages qui doivent être présent dans le certificat
   * @return Booléen qui valide la signature numérique du certificat.
   *
   * @par Tâches
   *      S.O.
   */
  @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
  public Boolean verifyCertificateExtensions(X509Certificate certificate, Hostname hostname,
      CertificateKeyUsages certificateKeyUsages) {
    Boolean isValid = false;

    // PRO-ASYM-CRT-04.1
    Set<String> criticalExtensionOids = certificate.getCriticalExtensionOIDs();

    // PRO-ASYM-CRT-04.1.1
    if (criticalExtensionOids.contains("2.5.29.19")) {
      isValid = certificate.getBasicConstraints() == -1;
    }

    // PRO-ASYM-CRT-04.1.2
    if (isValid && criticalExtensionOids.contains("2.5.29.15") && certificateKeyUsages != null) {
      isValid = verifyKeyUsages(certificate.getKeyUsage(), certificateKeyUsages);
    }

    // PRO-ASYM-CRT-04.1.3
    if (isValid) {
      isValid = verifyHostname(certificate, hostname);
    }

    return isValid;
  }

  /**
   * @brief @~french Vérifie les usages du certificat.
   * @param certKeyUsages Les cas d'usages récupérés sur le certificat X509.
   * @param certificateKeyUsages Les cas d'usages défini à l'interface.
   * @return Vrai si les usages du certificat se correspondent.
   *
   * @par Tâches
   *      S.O.
   */
  private Boolean verifyKeyUsages(boolean[] certKeyUsages,
      CertificateKeyUsages certificateKeyUsages) {
    Boolean isValid = false;

    if (certificateKeyUsages.requiredUsages().isEmpty() == false) {
      isValid = true;

      for (int i = 0; i < certKeyUsages.length; ++i) {
        if (certKeyUsages[i]) {
          isValid =
              isValid && certificateKeyUsages.requiredUsages().contains(KeyUsageType.fromBit(i));
        }
      }
    }

    return isValid;
  }

  /**
   * @brief @~french Vérifie le nom d'hôte correspond à celui du certificat.
   * @param certificate Le certificat.
   * @param hostname Le nom d'hôte.
   * @return Vrai si le nom d'hôte correspond à celui du certificat. Sinon, faux.
   *
   * @par Tâches
   *      S.O.
   */
  private Boolean verifyHostname(X509Certificate certificate, Hostname hostname) {
    Boolean verify = isUriInSubjectAlternativeNames(certificate, hostname.name());
    if (!verify) {
      verify = getCommonName(certificate).equals(hostname.name());
    }

    return verify;
  }

  /**
   * @brief @~french Récupère le CN du certificat.
   * @param certificate Le certificat,
   * @return Le CN du certificat sous une chaîne de caractères.
   *
   * @par Tâches
   *      S.O.
   */
  private String getCommonName(X509Certificate certificate) {
    String commonName = null;

    if (certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal())) {
      X500Principal subjectX500Principal = certificate.getSubjectX500Principal();
      String dn = subjectX500Principal.getName();

      // Parse the DN string to find the CN

      String[] dnComponents = dn.split(",");
      for (String component : dnComponents) {
        component = component.trim();
        if (component.startsWith("CN=")) {
          commonName = component.substring(3).trim(); // Remove "CN=" prefix
          break;
        }
      }
    }

    String unescapedCommonName = StringEscapeUtils.unescapeJava(commonName);
    return unescapedCommonName;
  }

  /**
   *
   * @brief @~french Vérifie si le nom d'hôte est dans le SAN du certificat.
   * @param certificate Le nom d'hôte.
   * @param host Le nom d'hôte.
   * @return Vrai si le nom d'hôte est dans le SAN du certificat. Sinon, faux.
   *
   * @par Tâches
   *      S.O.
   */
  private Boolean isUriInSubjectAlternativeNames(X509Certificate certificate, String host) {
    try {
      Collection<List<?>> sans = certificate.getSubjectAlternativeNames();
      if (sans != null) {
        for (List<?> sanItem : sans) {
          int type = (Integer) sanItem.get(0);
          if (type == 2) { // 2 = DNS name
            String dnsName = (String) sanItem.get(1);
            if (host.equalsIgnoreCase(dnsName)) {
              return true;
            }
          }
        }
      }
    } catch (CertificateParsingException e) {
      logger.error(Error.GENERIC_ERROR, e);
    }

    return false;
  }
}
