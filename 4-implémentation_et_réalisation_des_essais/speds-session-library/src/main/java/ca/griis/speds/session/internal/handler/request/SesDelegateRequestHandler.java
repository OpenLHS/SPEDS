/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesDelegateRequestHandler.
 * @brief @~english Implementation of the SesDelegateRequestHandler class.
 */

package ca.griis.speds.session.internal.handler.request;

import ca.griis.js2p.gen.speds.session.api.dto.Context23Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.exception.InvalidTokenException;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.domain.ExpandedSessionSidu;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.PendingConfirmation;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.indication.initiator.InitiatorEvent;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.sync.PendingConfirmationException;
import ca.griis.speds.session.internal.sync.PendingConfirmationRegistry;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
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
 * @brief @~french Effectue une délégation de requête.
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
 *      2026-03-13 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class SesDelegateRequestHandler implements InitiatorEvent {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SesDelegateRequestHandler.class);

  private final SesPubRequestHandler pubHandler;
  private final SesSakRequestHandler sakHandler;
  private final SesCleRequestHandler sdekHandler;
  private final SesMsgRequestHandler msgHandler;
  private final SesFinRequestHandler finHandler;
  private final PendingConfirmationRegistry pendingConfirmationsRegistry;
  private final ObjectMapper mapper;
  private final TransportHostAdapter transportHost;
  private final Map<SessionId, SessionInformation> sessions;
  private final SessionHostEvent sessionHostEvent;

  public SesDelegateRequestHandler(HostStartupContext context) {
    logger.trace(Trace.ENTER_METHOD_1, "context", context);

    this.sessions = context.sessions().asMap();
    this.mapper = context.sharedMapper();
    this.transportHost = new TransportHostAdapter(context);
    this.pendingConfirmationsRegistry =
        new PendingConfirmationRegistry(context.pendingMessage().asMap());
    this.pubHandler = new SesPubRequestHandler(context);
    this.sakHandler = new SesSakRequestHandler(context);
    this.sdekHandler = new SesCleRequestHandler(context);
    this.msgHandler = new SesMsgRequestHandler(context);
    this.finHandler = new SesFinRequestHandler(context);
    this.sessionHostEvent = context.hostEventConsumer();
  }

  public Optional<String> handle(Pidu pidu) {
    logger.trace(Trace.ENTER_METHOD_1, "pidu", pidu);

    Optional<String> confirm = Optional.empty();

    final Context23Dto context = new Context23Dto(
        pidu.getContext().getPga(),
        pidu.getContext().getSourceCode(),
        pidu.getContext().getDestinationCode(),
        pidu.getContext().getSdek(),
        Context23Dto.Service.DELEGATE,
        Context23Dto.ServicePrimitive.CONFIRM,
        false);
    final Pidu confirmPidu = new Pidu(context, "SUCCEED");
    try {
      confirm = Optional.of(mapper.writeValueAsString(confirmPidu));
      requestConfirm(pidu);
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "confirm", confirm);
    return confirm;
  }

  @Override
  public void notifyPubRec(ExpandedSidu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      PendingConfirmation match =
          pendingConfirmationsRegistry.checkMessage(idu, MsgType.SES_PUB_ENV);

      checkSessionIdentity(idu, match);

      final ExpandedSessionSidu expandedSessionIdu = sakHandler.handle(match.sessionId());
      final var entityCode = sessions.get(match.sessionId()).initiatorId();

      pendingConfirmationsRegistry.addMessage(expandedSessionIdu);

      if (!transportHost.request(entityCode, match.sessionId(),
          expandedSessionIdu.expandedSidu())) {
        pendingConfirmationsRegistry.removeMessage(idu);
      }
    } catch (PendingConfirmationException | JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifySakRec(ExpandedSidu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      PendingConfirmation match =
          pendingConfirmationsRegistry.checkMessage(idu, MsgType.SES_SAK_ENV);

      checkSessionIdentity(idu, match);

      final ExpandedSessionSidu expandedSessionSidu = sdekHandler.handle(match.sessionId(), idu);
      final var entityCode = sessions.get(match.sessionId()).initiatorId();

      pendingConfirmationsRegistry.addMessage(expandedSessionSidu);

      if (!transportHost.request(entityCode, match.sessionId(),
          expandedSessionSidu.expandedSidu())) {
        pendingConfirmationsRegistry.removeMessage(idu);
      }
    } catch (PendingConfirmationException | JsonProcessingException | NoSuchAlgorithmException
        | InvalidKeySpecException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyCleRec(ExpandedSidu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      PendingConfirmation match =
          pendingConfirmationsRegistry.checkMessage(idu, MsgType.SES_CLE_ENV);

      checkSessionIdentity(idu, match);

      final ExpandedSessionSidu expandedSessionIdu = msgHandler.handle(match.sessionId(), idu);
      final var entityCode = sessions.get(match.sessionId()).initiatorId();

      pendingConfirmationsRegistry.addMessage(expandedSessionIdu);

      if (!transportHost.request(entityCode, match.sessionId(),
          expandedSessionIdu.expandedSidu())) {
        pendingConfirmationsRegistry.removeMessage(idu);
      }
    } catch (InvalidTokenException | PendingConfirmationException | JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyMsgRec(ExpandedSidu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      PendingConfirmation match =
          pendingConfirmationsRegistry.checkMessage(idu, MsgType.SES_MSG_ENV);

      checkSessionIdentity(idu, match);

      final ExpandedSessionSidu expandedSessionIdu = finHandler.handle(idu);
      final var entityCode = sessions.get(match.sessionId()).initiatorId();

      pendingConfirmationsRegistry.addMessage(expandedSessionIdu);

      if (!transportHost.request(entityCode, match.sessionId(),
          expandedSessionIdu.expandedSidu())) {
        pendingConfirmationsRegistry.removeMessage(idu);
      }
    } catch (PendingConfirmationException | JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyFinRec(ExpandedSidu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      PendingConfirmation match =
          pendingConfirmationsRegistry.checkMessage(idu, MsgType.SES_FIN_ENV);

      checkSessionIdentity(idu, match);

      SessionInformation session = sessions.get(match.sessionId());
      final var entityCode = sessions.get(match.sessionId()).initiatorId();

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", entityCode,
          "msgId", idu.spdu().getHeader().getId(),
          "msgType", idu.spdu().getHeader().getMsgtype(),
          "sessionId", match.sessionId(),
          "sessionTerminatedSuccessfully", true);

      sessions.remove(session.sessionId());

      sessionHostEvent.notifyInitiatorSessionTerminatedSuccessfully(session.sessionId());
    } catch (PendingConfirmationException ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  private void checkSessionIdentity(ExpandedSidu idu, PendingConfirmation match)
      throws PendingConfirmationException {
    SessionInformation session = sessions.get(match.sessionId());
    if (session == null) {
      throw new PendingConfirmationException("No session to the pending confirmation");
    }

    final var code = idu.sidu().getContext().getSourceCode();
    if (session.peerId().equals(code) == false) {
      throw new PendingConfirmationException("The code from the IDU does not match the peer ID");
    }
  }

  private void requestConfirm(Pidu pidu) {
    try {
      final ExpandedSessionSidu expandedSessionIdu = pubHandler.handle(pidu);
      pendingConfirmationsRegistry.addMessage(expandedSessionIdu);

      transportHost.request(pidu.getContext().getSourceCode(), expandedSessionIdu.sessionId(),
          expandedSessionIdu.expandedSidu());
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }
  }
}
