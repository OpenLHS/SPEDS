/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe TrackingInformation.
 * @brief @~english Implementation of the TrackingInformation class.
 */

package ca.griis.speds.presentation.entity;

import java.util.UUID;
import javax.crypto.SecretKey;

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
 * @brief @~french Représente un enregistrement assurant le suivi inter-couche, comprenant un
 *        identifiant unique grâce à un UUID
 *        ainsi qu'une clé de chiffrement. Cet enregistrement permet de mémoriser, pour la couche
 *        session, l'identifiant
 *        de suivi et la clé de chiffrement associée.
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
public record TrackingInformation (UUID sessionTracking, SecretKey sdek) {
}
