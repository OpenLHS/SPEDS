/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description du record InterfaceDataUnit.
 * @brief @~english Contains description of InterfaceDataUnit record.
 */

package ca.griis.speds.api.dto;

import ca.griis.js2p.gen.speds.application.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.application.api.dto.InterfaceDataUnit01Dto;

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
 * @brief @~french Définit une l'unité de données d'interface du protocole SPEDS.
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
 *      2025-04-07 [FO] - Première ébauche.<br>
 *
 * @par Tâches
 *      S.O.
 */
public record SpedsInterfaceDataUnit (HeaderDto.Msgtype type, InterfaceDataUnit01Dto idu) {
}
