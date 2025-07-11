/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface PgaService.
 * @brief @~english Implementation of the PgaService interface.
 */

package ca.griis.speds.session.api;

import ca.griis.speds.session.api.exception.GetIriException;
import ca.griis.speds.session.api.exception.GetPublicKeyException;
import ca.griis.speds.session.api.exception.VerifyException;

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
 * @brief @~french Offre les services en lien avec un PGA.
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
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface PgaService {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la légitimité d'une entité.
   * @param pgaId L'identifiant du PGA.
   * @param entityCode Le code de l'entité.
   * @param puAk La clé publique de l'entité.
   * @exception VerifyException Ssi un problème est arrivé lors de la vérification de légitimité.
   * @return Vrai si l’entité est légitime, sinon faux.
   *
   * @par Tâches
   *      S.O.
   */
  Boolean verifyLegitimacy(String pgaId, String entityCode, String puAk) throws VerifyException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère l'IRI associée à un code d'une entité.
   * @param pgaId L'identifiant du PGA.
   * @param entityCode Le code de l'entité.
   * @exception GetIriException Si un problème est arrivé lors de la récupération de l’IRI d’une
   *            entité.
   * @return L'IRI associée à un code d'une entité.
   *
   * @par Tâches
   *      S.O.
   */
  String getIri(String pgaId, String entityCode) throws GetIriException;

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère une clé publique associée à un code d'une entité.
   * @param pgaId L'identifiant du PGA.
   * @param entityCode Le code de l'entité.
   * @exception GetPublicKeyException Si un problème est arrivé lors de la récupération de la clé
   *            publique.
   * @return La clé publique associée à l'entité de code.
   *
   * @par Tâches
   *      S.O.
   */
  String getPublicKey(String pgaId, String entityCode) throws GetPublicKeyException;
}
