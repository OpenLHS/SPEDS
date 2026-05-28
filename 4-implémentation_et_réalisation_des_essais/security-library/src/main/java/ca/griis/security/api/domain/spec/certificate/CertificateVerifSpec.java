/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CertificateVerifSpec.
 * @brief @~english Implementation of the class CertificateVerifSpec.
 */

package ca.griis.security.api.domain.spec.certificate;

import ca.griis.security.api.domain.spec.SecuritySpec;
import java.util.Map;

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
 * @brief @~french Définit la specification de verification de certificat.
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
 *      «2025-12-17» [BD] - Implémentation initiale
 * @par Tâches
 *      S.O.
 */
public class CertificateVerifSpec extends SecuritySpec {
  private final String revocationVerifParam;

  public CertificateVerifSpec() {
    super("PKIX");

    this.revocationVerifParam = "CRL";
  }

  public String getRevocationVerifParam() {
    return revocationVerifParam;
  }

  @Override
  public Map<String, String> getParameters() {
    return Map.of("revocationVerifParam", revocationVerifParam);
  }
}
