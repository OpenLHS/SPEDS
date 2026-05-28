/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CertificatePrivateKeysEntry.
 * @brief @~english «File description»
 */

package ca.griis.speds.session.internal.security;

import ca.griis.speds.session.internal.security.crypto.SessionKeyDestroyer;
import java.security.PrivateKey;
import java.security.cert.Certificate;

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
 * @brief @~french Certificat associé à une clé privée.
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
 *      2026-03-06 [JM] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class CertificatePrivateKeysEntry {
  private final Certificate certificate;
  private final PrivateKey privateKey;

  public CertificatePrivateKeysEntry(Certificate certificate, PrivateKey privateKey) {
    this.certificate = certificate;
    this.privateKey = privateKey;
  }

  public Certificate getCertficate() {
    return this.certificate;
  }

  public PrivateKey getPrivateKey() {
    return this.privateKey;
  }

  public void cleanUp() {
    SessionKeyDestroyer.destroy(privateKey);
  }
}
