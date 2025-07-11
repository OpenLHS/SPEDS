package ca.griis.speds.communication.protocol;

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
 * @brief @~french Unité de données d'interface du protocol
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
public record ProtocolIdu (String destinationUri, UUID messageIdentifier, String sdu) {
}
