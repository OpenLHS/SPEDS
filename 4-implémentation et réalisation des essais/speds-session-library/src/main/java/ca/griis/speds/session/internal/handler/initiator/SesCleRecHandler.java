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

package ca.griis.speds.session.internal.handler.initiator;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
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
import javax.crypto.SecretKey;

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

  private final SealVerifier sealVerifier;
  private final ObjectMapper sharedMapper;
  private final Map<SessionId, SessionInformation> sessionInfo;
  private final BlockingQueue<ExpandedSidu> queue;

  public SesCleRecHandler(ObjectMapper sharedMapper, BlockingQueue<ExpandedSidu> queue,
      Map<SessionId, SessionInformation> sessionInformations, SealVerifier sealVerifier) {
    logger.trace(Trace.ENTER_METHOD_3, "sharedMapper", sharedMapper, "queue", queue,
        "sessionInformations", sessionInformations);
    this.sharedMapper = sharedMapper;
    this.queue = queue;
    this.sessionInfo = sessionInformations;
    this.sealVerifier = sealVerifier;
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_CLE_REC;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Récupérer les informations de la session.
    SesCleRecDto sesCleRecDto = null;
    try {
      sesCleRecDto =
          sharedMapper.readValue((String) message.spdu().getContent(), SesCleRecDto.class);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoreException("Session required - Unable to read SDU");
    }

    SessionId sessionId = new SessionId(sesCleRecDto.getSession());
    SessionInformation sessionInformation = sessionInfo.get(sessionId);
    if (sessionInformation == null) {
      throw new SilentIgnoreException("Session required");
    }

    // Authentifier la source de l’envoi.
    verifySource(message.spdu().getStamp(), sessionInformation.skak, message.spdu().getContent());

    dispatch(message);
    logger.trace(Trace.EXIT_METHOD_0);
  }

  private void verifySource(String seal,
      SecretKey skak,
      Object sdu) throws SilentIgnoreException {
    boolean verifyStamp = sealVerifier.verifySymmetricalSeal(
        sdu,
        skak,
        seal,
        sharedMapper);
    if (!verifyStamp) {
      throw new SilentIgnoreException("Stamp is invalid");
    }
  }

  protected void dispatch(ExpandedSidu expandedSidu) {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);

    try {
      queue.put(expandedSidu);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
