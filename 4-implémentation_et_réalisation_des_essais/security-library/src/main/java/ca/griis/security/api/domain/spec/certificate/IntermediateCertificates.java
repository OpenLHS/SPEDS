/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe IntermediateCertificates.
 * @brief @~english Implementation of the class IntermediateCertificates.
 */

package ca.griis.security.api.domain.spec.certificate;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

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
 * @brief @~french Définit les certificats intermediaires.
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
public class IntermediateCertificates {
  private final List<X509Certificate> certificates;

  public IntermediateCertificates(List<X509Certificate> certificates) {
    this.certificates = new ArrayList<>(certificates);
  }

  public List<X509Certificate> getCertificates() {
    return new ArrayList<>(certificates);
  }
}
