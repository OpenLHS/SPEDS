/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface CertificateService.
 * @brief @~english Implementation of the CertificateService interface.
 */

package ca.griis.speds.toolkit.security.api;

import java.security.cert.X509Certificate;
import org.apache.jena.iri.IRI;

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
 * @brief @~french Définit le service de certificat.
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
 *      2025-11-24 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface CertificateService {
  /**
   * @brief @~french Vérifie la validité d’un certificat pour une entité proprement identifiée par
   *        une IRI.
   * @param certificate Le certificat X.509 à vérifier.
   * @param iri L'IRI de l’entité visée par le certificat.
   * @return Le résultat de la validation sous forme de valeur booléenne.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean checkCertificateValidity(X509Certificate certificate, IRI iri);
}
