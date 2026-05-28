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

import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.contract.IdentifierGenerator;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

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
public record HostStartupContext (
    TransportHost transportHost,
    ObjectMapper sharedMapper,
    AuthorizationService projectService,
    VersionDto version,
    CertificatePrivateKeysEntry hostKeys,
    IdentifierGenerator identifierGenerator,
    CryptographyService cryptographyService,
    Cache<SessionId, SessionInformation> sessions,
    Cache<UUID, PendingMessage> pendingMessage,
    SessionHostEvent hostEventConsumer,
    ExecutorService executor,
    Integer transportConfirmationTimeout) {

  public void cleanUp() {
    closePreservingSessionStates();
    clearSessionStates();
  }

  public void closePreservingSessionStates() {
    executor.shutdown();
    transportHost.close();
    pendingMessage.invalidateAll();
    pendingMessage.cleanUp();
  }

  public void clearSessionStates() {
    sessions.cleanUp();
    hostKeys.cleanUp();
  }
}
