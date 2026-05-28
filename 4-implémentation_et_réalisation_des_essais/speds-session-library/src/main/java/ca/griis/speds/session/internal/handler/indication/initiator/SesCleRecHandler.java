/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesCleRecHandler.
 * @brief @~english Implementation of the SesCleRecHandler class.
 */

package ca.griis.speds.session.internal.handler.indication.initiator;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

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
 * @brief @~french Implémentation du gestionnaire de message Ses.Cle.Rec
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
public class SesCleRecHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesCleRecHandler.class);

  private final SessionSecurityService securityService;
  private final ObjectMapper mapper;
  private final Map<SessionId, SessionInformation> sessionInfo;
  private final InitiatorEvent initiatorEvent;
  private final TransportHostAdapter transportHost;

  public SesCleRecHandler(HostStartupContext ctx, InitiatorEvent initiatorEvent) {
    this.mapper = ctx.sharedMapper();
    this.sessionInfo = ctx.sessions().asMap();
    this.initiatorEvent = initiatorEvent;
    this.securityService = new SessionSecurityService(ctx.cryptographyService());
    this.transportHost = new TransportHostAdapter(ctx);
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_CLE_REC;
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

    try {
      final SesCleRecDto sesCleRecDto =
          mapper.readValue((String) message.spdu().getContent(), SesCleRecDto.class);
      sessionId = new SessionId(sesCleRecDto.getSession());
      final var validatedSession = Optional.ofNullable(sessionInfo.get(sessionId));

      if (validatedSession.isPresent()) {
        final var code = message.sidu().getContext().getSourceCode();

        if (validatedSession.get().peerId().equals(code)) {
          final SessionInformation sessionInformation = validatedSession.get();
          final Boolean verifyStamp =
              securityService.verifyStamp(
                  Base64.getDecoder().decode(message.spdu().getStamp()),
                  ((String) message.spdu().getContent()).getBytes(StandardCharsets.UTF_8),
                  sessionInformation.skak());

          if (verifyStamp) {
            final Boolean validToken =
                securityService.verifyToken(sessionInformation.token(), sessionInformation.sdek(),
                    sesCleRecDto.getContent());

            if (validToken) {
              transportHost.response(entityCode, sessionId, message, "SUCCEED");

              initiatorEvent.notifyCleRec(message);
            } else {
              transportHost.response(entityCode, sessionId, message, "FAILED: TokenException");
            }
          } else {
            transportHost.response(entityCode, sessionId, message, "FAILED: AuthenticateException");
          }
        } else {
          transportHost.response(entityCode, sessionId, message,
              "FAILED: The code from the IDU does not match the peer ID");
        }
      } else {
        transportHost.response(entityCode, sessionId, message, "FAILED: UnknownSessionException");
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.GENERIC_ERROR, e);

      transportHost.response(entityCode, sessionId, message, "FAILED: Cannot read SES.CLE.REC");
    }

    Optional<String> result = Optional.empty();
    logger.trace(Trace.ENTER_METHOD_1, "result", result);
    return result;
  }
}
