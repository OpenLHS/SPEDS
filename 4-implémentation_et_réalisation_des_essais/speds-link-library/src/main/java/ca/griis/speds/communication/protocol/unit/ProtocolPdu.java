/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ProtocolIdu.
 * @brief @~english Implementation of the ProtocolIdu class.
 */

package ca.griis.speds.communication.protocol.unit;

import com.fasterxml.jackson.annotation.JsonProperty;

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
 * @brief @~french Unité de données de protocole de communciation.
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
 *      2025-06-10 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public record ProtocolPdu (
    @JsonProperty("remoteAddr") String remoteAddr,
    @JsonProperty("sdu") String sdu) {
}
