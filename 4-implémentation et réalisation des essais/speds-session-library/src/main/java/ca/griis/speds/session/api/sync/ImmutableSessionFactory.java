/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableSessionFactory.
 * @brief @~english Implementation of the ImmutableSessionFactory class.
 */

package ca.griis.speds.session.api.sync;

import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.contract.IdentifierGenerator;

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
 * @brief @~french Offre une fabrique d'entités nécessaires à la couche session.
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
 *      2025-05-12 [CB] - Implémentation v2
 *      2025-03-17 [SSC] - Implémentation de la conception (init)
 *      2025-02-18 [MD] - Déplacé le paramètre PgaService vers constructeur </br>
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
@Deprecated
public class ImmutableSessionFactory extends SyncSessionFactory {
  public ImmutableSessionFactory(PgaService pgaService) {
    super(pgaService);
  }

  public ImmutableSessionFactory(PgaService pgaService, IdentifierGenerator identifierGenerator) {
    super(pgaService, identifierGenerator);
  }
}
