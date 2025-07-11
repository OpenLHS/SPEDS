/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ServerSession.
 * @brief @~english Implementation of the ServerSession class.
 */

package ca.griis.speds.session.internal.service;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.InvalidTrackingNumberException;
import ca.griis.speds.session.api.exception.NoResponseRequestException;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.handler.peer.PeerHandlerRegistry;
import ca.griis.speds.session.internal.model.PendingIndication;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.Poller;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

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
 * @brief @~french Implémente la logique métier d’un serveur gérant la couche session
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
 *      2025-03-27 [SSC] - Implémentation initiale<br>
 *      2025-05-14 [CB] - Refact - itération 2<br>
 *      2025-06-29 [MD] - Refact - itération 4<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class ServerSession {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ServerSession.class);

  private static final Pidu poisonPill = new Pidu(null, null);

  private final SPEDSDto speds;
  private final SealCreator sealCreator;
  private final ObjectMapper sharedMapper;
  private final TransportHost transportHost;
  private final ConcurrentLinkedQueue<PendingIndication> indicationTracking;
  private final BlockingQueue<Pidu> messageWeHandle;
  private final Map<SessionId, SessionInformation> sessionInformations;
  private final Duration serverTimeout;
  private final Poller poller;

  public ServerSession(HostStartupContext hostStartupContext, Poller poller,
      SealCreator sealCreator) {
    logger.trace(Trace.ENTER_METHOD_3, "hostStartupContext", hostStartupContext, "poller", poller,
        "sealCreator", sealCreator);
    this.indicationTracking = new ConcurrentLinkedQueue<>();
    this.messageWeHandle = new LinkedBlockingDeque<>();
    this.sessionInformations = new ConcurrentHashMap<>();
    this.speds = hostStartupContext.spedsDto();
    this.sharedMapper = hostStartupContext.sharedMapper();
    this.transportHost = hostStartupContext.transportHost();
    this.serverTimeout = hostStartupContext.responseTimeout();
    this.sealCreator = sealCreator;
    this.poller = poller;
    HandlerRegistry handlerRegistry = new PeerHandlerRegistry(hostStartupContext, messageWeHandle,
        sessionInformations, indicationTracking);
    poller.registerHandlers(handlerRegistry);
  }

  public void close() {
    closePreservingSessionStates();
    sessionInformations.clear();
  }

  public void closePreservingSessionStates() {
    poller.stop();

    transportHost.close();
    indicationTracking.clear();
    messageWeHandle.clear();
    transportHost.close();

    try {
      messageWeHandle.put(poisonPill);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void clearSessionStates() {
    sessionInformations.clear();
  }

  public Pidu getPendingMessage() {
    logger.trace(Trace.ENTER_METHOD_0);

    Pidu pidu;
    try {
      pidu = messageWeHandle.poll(serverTimeout.getSeconds(), TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    if (poisonPill.equals(pidu)) {
      throw new RuntimeException("Invalid IDU");
    }

    logger.trace(Trace.EXIT_METHOD_1, "pidu", pidu);
    return pidu;
  }

  public void saveState() {
    // TODO
    // indicationTracking;
    // messageWeHandle;
    // sessionInformations;
  }

  void loadState() {
    // TODO
    // indicationTracking
    // messageWeHandle
    // sessionInformations
  }

  public void sendReply(Pidu pidu) {
    logger.trace(Trace.ENTER_METHOD_1, "pidu", pidu);
    // Récupérer les informations de la session
    PendingIndication pendingIndication = indicationTracking.stream()
        .filter(x -> x.piduTracking().equals(pidu.getContext().getTrackingNumber()))
        .findFirst()
        .orElseThrow(() -> new NoResponseRequestException("PIDU tracking number invalid - "
            + "needs to be same we sent previously; value="
            + pidu.getContext().getTrackingNumber()));
    indicationTracking.remove(pendingIndication);
    SessionInformation sessionInformation = sessionInformations.get(pendingIndication.sessionId());

    if (sessionInformation == null) {
      throw new InvalidTrackingNumberException("The trackingNumber wasn't associated to a session");
    }

    // Transmettre la réponse
    // Créer une SDU
    SesMsgRecDto msgRecDto = new SesMsgRecDto(pidu.getMessage(), sessionInformation.sessionId.id());

    // Créer la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_MSG_REC,
        sessionInformation.trackingNumber,
        false,
        speds);
    String stamp = sealCreator.createSymmetricalSeal(msgRecDto, sessionInformation.skak,
        sharedMapper);
    Spdu spdu = null;
    try {
      spdu = new Spdu(spduHeader, stamp, sharedMapper.writeValueAsString(msgRecDto));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Créer la SIDU
    SiduContext siduContext = new SiduContext(
        sessionInformation.peerId,
        sessionInformation.initiatorId,
        sessionInformation.peerIri,
        pendingIndication.siduTracking(),
        sessionInformation.initiatorIri,
        false);

    try {
      Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
      String serialSidu = sharedMapper.writeValueAsString(sidu);
      logger.trace(Trace.ALGORITHM_1, serialSidu, serialSidu);

      transportHost.dataRequest(serialSidu);
      transportHost.dataConfirm();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    logger.trace(Trace.ENTER_METHOD_0);
  }

  public Map<SessionId, SessionInformation> getSessionInfo() {
    return Map.copyOf(this.sessionInformations);
  }

  public ConcurrentLinkedQueue<PendingIndication> getIndicationTracking() {
    return this.indicationTracking;
  }
}
