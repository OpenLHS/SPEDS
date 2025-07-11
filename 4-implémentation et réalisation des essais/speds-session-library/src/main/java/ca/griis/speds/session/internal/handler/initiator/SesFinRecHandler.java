/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesFinRecHandler.
 * @brief @~english Implementation of the SesFinRecHandler class.
 */

package ca.griis.speds.session.internal.handler.initiator;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

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
 * @brief @~french Implémentation du gestionnaire de message SES.FIN.REC
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
public class SesFinRecHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesFinRecHandler.class);

  private final SealVerifier sealVerifier = new SealVerifier();
  private final ObjectMapper sharedMapper;
  private final Map<SessionId, SessionInformation> sessionInfo;
  private final BlockingQueue<ExpandedSidu> queue;

  public SesFinRecHandler(ObjectMapper sharedMapper, BlockingQueue<ExpandedSidu> queue,
      Map<SessionId, SessionInformation> sessionInformations) {
    logger.trace(Trace.ENTER_METHOD_3, "sharedMapper", sharedMapper, "queue", queue,
        "sessionInformations", sessionInformations);
    this.sharedMapper = sharedMapper;
    this.sessionInfo = sessionInformations;
    this.queue = queue;
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_FIN_REC;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Récupérer les informations de la session.
    SesFinRecDto sesMsgRecDto = null;
    try {
      sesMsgRecDto =
          sharedMapper.readValue((String) message.spdu().getContent(), SesFinRecDto.class);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoreException("Session required");
    }

    SessionId sessionId = new SessionId(sesMsgRecDto.getSession());
    SessionInformation sessionInformation = sessionInfo.get(sessionId);
    if (sessionInformation == null) {
      throw new SilentIgnoreException("Session required");
    }

    // Authentifier la source de l’envoi.
    // Stamp
    // stamp chiffer avec skak
    boolean verifyStamp = sealVerifier.verifySymmetricalSeal(
        message.spdu().getContent(),
        sessionInformation.skak,
        message.spdu().getStamp(),
        sharedMapper);
    if (!verifyStamp) {
      throw new SilentIgnoreException("Stamp is invalid");
    }

    dispatch(message);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  private void dispatch(ExpandedSidu expandedSidu) {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);
    try {
      queue.put(expandedSidu);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    logger.trace(Trace.EXIT_METHOD_0);
  }
}
