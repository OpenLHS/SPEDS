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

package ca.griis.speds.session.internal.handler.peer;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.PiduContext;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.model.PendingIndication;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
  private final SealVerifier sealVerifier;
  private final Map<SessionId, SessionInformation> sessionInfo;
  private final ConcurrentLinkedQueue<PendingIndication> indicationTracking;
  private final BlockingQueue<Pidu> queue;

  public SesMsgEnvHandler(ObjectMapper sharedMapper, Map<SessionId, SessionInformation> sessionInfo,
      ConcurrentLinkedQueue<PendingIndication> indicationTracking, BlockingQueue<Pidu> queue) {
    logger.trace(Trace.ENTER_METHOD_4, "sharedMapper", sharedMapper, "sessionInfo", sessionInfo,
        "indicationTracking", indicationTracking, "queue", queue);
    this.sharedMapper = sharedMapper;
    this.sessionInfo = sessionInfo;
    this.indicationTracking = indicationTracking;
    this.queue = queue;
    this.sealVerifier = new SealVerifier();
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_MSG_ENV;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Récupérer les informations de la session.
    SesMsgEnvDto content = null;
    try {
      content =
          sharedMapper.readValue((String) message.spdu().getContent(), SesMsgEnvDto.class);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoreException("Session required");
    }
    SessionId sessionId = new SessionId(content.getSession());
    SessionInformation sessionInformation = sessionInfo.get(sessionId);
    if (sessionInformation == null) {
      throw new SilentIgnoreException("Session required");
    }

    // Authentifier la source de l’envoi.
    // Stamp
    boolean verifyStamp = sealVerifier.verifySymmetricalSeal(
        content,
        sessionInformation.skak,
        message.spdu().getStamp(),
        sharedMapper);
    if (!verifyStamp) {
      throw new SilentIgnoreException("Stamp is invalid");
    }

    // Mettre à jour les informations de la session.
    // On utilise volontairement le même numéro de suivis pour faire la correspondance, rendant
    // le numéro de suivis comme un passthrough pour cette couche
    SessionInformation updatedInfo = SessionInformation.builder()
        .of(sessionInformation)
        .trackingNumber(message.spdu().getHeader().getId())
        .build();
    sessionInfo.put(sessionId, updatedInfo);
    indicationTracking.add(
        new PendingIndication(sessionId,
            message.sidu().getContext().getTrackingNumber(),
            message.spdu().getHeader().getId()));

    // Transmettre la PIDU à la couche supérieure.
    byte[] keyBytes = sessionInformation.sdek.getEncoded();
    String serialSdek = Base64.getEncoder().encodeToString(keyBytes);
    PiduContext contextDto = new PiduContext(
        updatedInfo.pgaId,
        updatedInfo.initiatorId,
        updatedInfo.peerId,
        serialSdek,
        message.sidu().getContext().getTrackingNumber(),
        false);

    Pidu pidu = new Pidu(contextDto, content.getContent());
    dispatch(pidu);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  private void dispatch(Pidu pidu) {
    logger.trace(Trace.ENTER_METHOD_1, "pidu", pidu);
    try {
      queue.put(pidu);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }
}
