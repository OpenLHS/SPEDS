/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface IdentifierGenerator.
 * @brief @~english IdentifierGenerator interface implementation.
 */

package ca.griis.speds.transport.service.identification;

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
 * @brief @~french Génère un identifiant.
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
 *      2025-03-03 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public interface IdentifierGenerator {
  /**
   * @brief @~english «Description of the function»
   * @return «Return description»
   *
   * @brief @~french Génère un identifiant à utiliser pour les messages Transport.
   * @return L'identifiant du message Transport sous forme de chaîne de caractères.
   *
   * @par Tâches
   *      S.O.
   */
  String generateId();
}
