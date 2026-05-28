/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe CsprngSpecProvider.
 * @brief @~english Contains description of CsprngSpecProvider class.
 */

package ca.griis.speds.toolkit.crypto.internal.provider;

import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import ca.griis.speds.toolkit.crypto.internal.converter.facade.spec.SpecGenConverter;

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
 * @brief @~french Offre de génèrer une spécification d'algorithme du générateur de valeurs
 *        aléatoires.
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
 *      2025-12-03 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class CsprngSpecProvider {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Génère une spéfication d'algorithme du générateur de valeurs aléatoires.
   * @return Spéfication d'algorithme du générateur de valeurs aléatoires.
   *
   * @par Tâches
   *      S.O.
   */
  public CsprngSpec getSpec() {
    return (CsprngSpec) new SpecGenConverter().apply("CSPRNG");
  }
}
