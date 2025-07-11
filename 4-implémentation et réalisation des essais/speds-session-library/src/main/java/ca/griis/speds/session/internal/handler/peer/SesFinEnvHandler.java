/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesFinEnvHandler.
 * @brief @~english Implementation of the SesFinEnvHandler class.
 */

package ca.griis.speds.session.internal.handler.peer;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

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
 * @brief @~french Implémentation du gestionnaire de message SES.FIN.ENV
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
public class SesFinEnvHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesFinEnvHandler.class);

  private final ObjectMapper sharedMapper;
  private final SealCreator sealCreator;
  private final SealVerifier sealVerifier = new SealVerifier();
  private final SPEDSDto speds;
  private final TransportHost transportHost;
  private final Map<SessionId, SessionInformation> sessionInfo;

  public SesFinEnvHandler(SPEDSDto speds, ObjectMapper sharedObject, TransportHost transportHost,
      Map<SessionId, SessionInformation> sessionInfo) {
    logger.trace(Trace.ENTER_METHOD_4, "speds", speds, "sharedObject", sharedObject,
        "transportHost", transportHost, "sessionInfo", sessionInfo);

    this.speds = speds;
    this.sharedMapper = sharedObject;
    this.transportHost = transportHost;
    this.sessionInfo = sessionInfo;
    this.sealCreator = new SealCreator();
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_FIN_ENV;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Récupérer les informations de la session.
    SesFinEnvDto sesFinEnvDto = null;
    try {
      sesFinEnvDto =
          sharedMapper.readValue((String) message.spdu().getContent(), SesFinEnvDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    SessionId sessionId = new SessionId(sesFinEnvDto.getSession());
    SessionInformation sessionInformation = sessionInfo.get(sessionId);
    if (sessionInformation == null) {
      throw new SilentIgnoreException("Session required");
    }

    // Authentifier la source de l’envoi.
    // Stamp
    verifyStamp(message, sessionInformation);

    // Transmettre la réponse à la couche inférieure
    // SDU
    SesFinRecDto sesFinRecDto = new SesFinRecDto(sesFinEnvDto.getToken(), sessionId.id());
    String serialSdu;
    try {
      serialSdu = sharedMapper.writeValueAsString(sesFinRecDto);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_FIN_REC,
        message.spdu().getHeader().getId(),
        false,
        speds);
    String stamp = sealCreator.createSymmetricalSeal(serialSdu, sessionInformation.skak,
        sharedMapper);
    Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);

    // SIDU
    SiduContext siduContext = new SiduContext(
        message.sidu().getContext().getDestinationCode(),
        message.sidu().getContext().getSourceCode(),
        message.sidu().getContext().getDestinationIri(),
        message.sidu().getContext().getTrackingNumber(),
        message.sidu().getContext().getSourceIri(),
        false);

    Sidu sidu;
    try {
      sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    sessionInfo.remove(sessionInformation.sessionId);

    // dispatch
    dispatch(sidu);
    logger.trace(Trace.EXIT_METHOD_0);
  }

  private void verifyStamp(ExpandedSidu message, SessionInformation sessionInformation)
      throws SilentIgnoreException {
    boolean verifyStamp = sealVerifier.verifySymmetricalSeal(
        message.spdu().getContent(),
        sessionInformation.skak,
        message.spdu().getStamp(),
        sharedMapper);
    if (!verifyStamp) {
      throw new SilentIgnoreException("Stamp is invalid");
    }
  }

  private void dispatch(Sidu sidu) {
    logger.trace(Trace.ENTER_METHOD_1, "sidu", sidu);

    String serialSidu;

    try {
      serialSidu = sharedMapper.writeValueAsString(sidu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    transportHost.dataRequest(serialSidu);
    transportHost.dataConfirm();

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
