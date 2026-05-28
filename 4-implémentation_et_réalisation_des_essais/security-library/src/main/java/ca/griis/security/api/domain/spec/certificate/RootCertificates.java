/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe RootCertificates.
 * @brief @~english Implementation of the class RootCertificates.
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
 * @brief @~french Définit les certificats racines.
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
public final class RootCertificates {
  private final List<X509Certificate> certificates;

  public RootCertificates(List<X509Certificate> certificates) {
    if (certificates == null || certificates.isEmpty()) {
      throw new IllegalArgumentException(
          "La liste de certificats doit contenir au moins un élément");
    }

    this.certificates = new ArrayList<>(certificates);
  }

  public List<X509Certificate> getCertificates() {
    return new ArrayList<>(certificates);
  }
}
