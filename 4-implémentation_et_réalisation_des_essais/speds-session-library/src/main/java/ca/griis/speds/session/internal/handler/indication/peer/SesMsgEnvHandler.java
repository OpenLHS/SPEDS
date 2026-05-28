/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesMsgEnvHandler.
 * @brief @~english Implementation of the SesMsgEnvHandler class.
 */

package ca.griis.speds.session.internal.handler.indication.peer;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.PiduContext;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.PendingMessage;
import ca.griis.speds.session.internal.domain.PendingResponse;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.crypto.SessionKeyDestroyer;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

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
 * @brief @~french Implémentation du gestionnaire de message SES.MSG.ENV
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
public class SesMsgEnvHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesMsgEnvHandler.class);

  private final ObjectMapper sharedMapper;
  private final SessionSecurityService securityService;
  private final Map<SessionId, SessionInformation> sessions;
  private final TransportHostAdapter transportHost;
  private final ConcurrentMap<UUID, PendingMessage> pendingResponses;

  public SesMsgEnvHandler(HostStartupContext ctx) {
    this.transportHost = new TransportHostAdapter(ctx);
    this.sharedMapper = ctx.sharedMapper();
    this.sessions = ctx.sessions().asMap();
    this.securityService = new SessionSecurityService(ctx.cryptographyService());
    this.pendingResponses = ctx.pendingMessage().asMap();
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_MSG_ENV;
  }

  @Override
  public Optional<String> handle(ExpandedSidu message) {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    final var entityCode = message.sidu().getContext().getDestinationCode();
    Optional<String> result = Optional.empty();
    SessionId sessionId = null;

    try {
      SesMsgEnvDto content =
          sharedMapper.readValue((String) message.spdu().getContent(), SesMsgEnvDto.class);
      sessionId = new SessionId(content.getSession());
      SessionInformation sessionInfo = sessions.get(sessionId);

      if (sessionInfo != null) {
        final var code = message.sidu().getContext().getSourceCode();

        if (sessionInfo.initiatorId().equals(code)) {
          final Boolean verifyStamp =
              securityService.verifyStamp(
                  Base64.getDecoder().decode(message.spdu().getStamp()),
                  ((String) message.spdu().getContent()).getBytes(StandardCharsets.UTF_8),
                  sessionInfo.skak());
          if (verifyStamp) {
            transportHost.response(entityCode, sessionId, message, "SUCCEED");

            // Transmettre la PIDU à la couche supérieure.
            byte[] keyBytes = sessionInfo.sdek().getEncoded();
            String serialSdek = Base64.getEncoder().encodeToString(keyBytes);
            SessionKeyDestroyer.destroy(keyBytes);

            final var msgId = message.spdu().getHeader().getId();
            final var tnKey = UUID.fromString(msgId.toString());

            final var options = Map.of("TN", tnKey);
            PiduContext contextDto = new PiduContext(
                sessionInfo.pgaId(),
                sessionInfo.initiatorId(),
                sessionInfo.peerId(),
                serialSdek,
                Context23Dto.Service.TRANSFER,
                Context23Dto.ServicePrimitive.INDICATION,
                options);

            pendingResponses.putIfAbsent(tnKey, new PendingResponse(sessionId));

            content =
                sharedMapper.readValue((String) message.spdu().getContent(), SesMsgEnvDto.class);
            Pidu pidu = new Pidu(contextDto, content.getContent());
            result = Optional.of(sharedMapper.writeValueAsString(pidu));
          } else {
            transportHost.response(entityCode, sessionId, message,
                "FAILED: AuthenticateException");
          }
        } else {
          transportHost.response(entityCode, sessionId, message,
              "FAILED: The code from the IDU does not match the initiator ID");
        }
      } else {
        transportHost.response(entityCode, sessionId, message,
            "FAILED: UnknownSessionException");
      }
    } catch (IOException e) {
      logger.error(Error.IGNORED_ERROR, e);

      transportHost.response(entityCode, sessionId, message,
          "FAILED: Error during the processing of the message");
    }

    logger.trace(Trace.ENTER_METHOD_1, "result", result);
    return result;
  }
}
