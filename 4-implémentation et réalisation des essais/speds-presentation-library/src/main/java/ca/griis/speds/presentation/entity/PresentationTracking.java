/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe PresentationTracking.
 * @brief @~english Implementation of the PresentationTracking class.
 */

package ca.griis.speds.presentation.entity;

import java.util.UUID;


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
 * @brief @~french Représente un enregistrement d'un identifiant de message de présentation, unique
 *        grâce à un UUID.
 *        Cet enregistrement permet de contextualiser de manière explicite l'information contenue
 *        dans l'UUID.
 * @par Détails
 *      S.O.
 *
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-18 [MD] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public record PresentationTracking (UUID uuid) {
}
