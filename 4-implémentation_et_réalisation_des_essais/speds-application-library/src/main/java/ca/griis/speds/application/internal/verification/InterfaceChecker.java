/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de l'interface InterfaceChecker.
 * @brief @~english InterfaceChecker interface implementation.
 */

package ca.griis.speds.application.internal.verification;

import java.util.function.Predicate;

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
 * @brief @~french Permet de vérifier la validité du contenu d'une interface reçu par l'utilisateur
 *        de la couche application.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-03-05 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public interface InterfaceChecker extends Predicate<String> {
}
