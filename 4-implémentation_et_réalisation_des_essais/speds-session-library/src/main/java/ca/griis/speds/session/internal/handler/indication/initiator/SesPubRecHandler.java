/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesPubRecHandler.
 * @brief @~english Implementation of the SesPubRecHandler class.
 */

package ca.griis.speds.session.internal.handler.indication.initiator;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
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
 * @brief @~french Implémentation du gestionnaire de message SES.PUB.REC
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
public class SesPubRecHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesPubRecHandler.class);

  private final Map<SessionId, SessionInformation> sessions;
  private final AuthorizationService projectService;
  private final InitiatorEvent initiatorEvent;
  private final CryptographyService cryptographyService;
  private final TransportHostAdapter transportHost;

  public SesPubRecHandler(HostStartupContext ctx, InitiatorEvent initiatorEvent) {
    logger.trace(Trace.ENTER_METHOD_2, "ctx", ctx, "initiatorEvent", initiatorEvent);
    this.sessions = ctx.sessions().asMap();
    this.cryptographyService = ctx.cryptographyService();
    this.projectService = ctx.projectService();
    this.initiatorEvent = initiatorEvent;
    this.transportHost = new TransportHostAdapter(ctx);
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_PUB_REC;
  }

  @Override
  public Optional<String> handle(ExpandedSidu message) {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    /**
     * @note 2026-04-28 - Le code de destination ici dans l'IDU représente le code de l'initiateur
     *       de la session Pour plus détails, voir la spécification du protocole de la couche
     *       session.
     */
    final var entityCode = message.sidu().getContext().getDestinationCode();
    SessionId sessionId = null;

    // Authentifier la source de l’envoi
    // Session
    final Optional<SessionInformation> validatedSession = validateSession(message);

    if (validatedSession.isPresent()) {
      final var code = message.sidu().getContext().getSourceCode();
      sessionId = validatedSession.get().sessionId();

      if (validatedSession.get().peerId().equals(code)) {
        final SessionInformation sessionInformation = validatedSession.get();
        final PublicKey key =
            projectService.getEntityPublicKey(sessionInformation.pgaId(),
                sessionInformation.peerId());

        // Stamp
        final Boolean verifyStamp = cryptographyService.checkSignatureValidity(SESSION,
            Base64.getDecoder().decode(message.spdu().getStamp()),
            key,
            ((String) message.spdu().getContent()).getBytes(StandardCharsets.UTF_8));

        if (verifyStamp) {
          transportHost.response(entityCode, sessionId, message, "SUCCEED");

          initiatorEvent.notifyPubRec(message);
        } else {
          sessions.remove(sessionInformation.sessionId());

          transportHost.response(entityCode, sessionId, message,
              "FAILED: AuthenticateException");
        }
      } else {
        transportHost.response(entityCode, sessionId, message,
            "FAILED: UnknownEntityCodeException");
      }
    } else {
      transportHost.response(entityCode, sessionId, message,
          "FAILED: UnknownSessionException");
    }

    Optional<String> result = Optional.empty();
    logger.trace(Trace.ENTER_METHOD_1, "result", result);
    return result;
  }

  private Optional<SessionInformation> validateSession(ExpandedSidu message) {
    UUID content = UUID.fromString((String) message.spdu().getContent());
    SessionId sessionId = new SessionId(content);
    SessionInformation sessionInformation = sessions.get(sessionId);

    return Optional.ofNullable(sessionInformation);
  }
}
