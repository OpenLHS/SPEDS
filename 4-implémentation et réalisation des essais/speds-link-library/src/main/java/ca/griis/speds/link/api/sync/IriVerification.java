/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe IriVerification.
 * @brief @~english Implementation of the IriVerification class.
 */

package ca.griis.speds.link.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import java.net.URI;
import java.net.URISyntaxException;

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
 * @brief @~french Offre un service de vérification d'IRI.
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
 *      2025-06-06 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class IriVerification {
  private static final GriisLogger logger = getLogger(IriVerification.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Verifier la validité du format IRI.
   * @param iri La chaine iri
   * @return l'état de validité de l'iri.
   *
   * @par Tâches
   *      S.O.
   */
  public Boolean isValidIri(String iri) {
    logger.trace(Trace.ENTER_METHOD_1, "iri", iri);

    boolean isValid = true;

    if (iri == null || iri.trim().isEmpty()) {
      isValid = false;
    } else {
      try {
        new URI(iri);
      } catch (URISyntaxException e) {
        isValid = false;
      }
    }

    logger.trace(Trace.EXIT_METHOD_1, "isValid", isValid);
    return isValid;
  }
}
