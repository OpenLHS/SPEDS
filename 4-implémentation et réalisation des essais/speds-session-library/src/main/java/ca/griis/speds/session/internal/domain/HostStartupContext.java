/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe HostStartupContext.
 * @brief @~english Implementation of the HostStartupContext class.
 */

package ca.griis.speds.session.internal.domain;

import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.contract.IdentifierGenerator;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;

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
 * @brief @~french Implémentation de HostStartupContext
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
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public record HostStartupContext (TransportHost transportHost, ObjectMapper sharedMapper,
    PgaService pgaService, SPEDSDto spedsDto,
    CertificatePrivateKeysEntry hostKeys,
    IdentifierGenerator identifierGenerator,
    Duration serverPollingInterval,
    Duration responseTimeout) {

}
