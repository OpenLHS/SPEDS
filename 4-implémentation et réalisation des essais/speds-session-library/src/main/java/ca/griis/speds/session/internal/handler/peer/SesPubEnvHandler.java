/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesPubEnvHandler.
 * @brief @~english Implementation of the SesPubEnvHandler class.
 */

package ca.griis.speds.session.internal.handler.peer;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.SesPubEnvDto;
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
 * @brief @~french Implémentation du gestionnaire de message SES.PUB.ENV
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
public class SesPubEnvHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesPubEnvHandler.class);

  private final ObjectMapper sharedMapper;
  private final TransportHost transportHost;
  private final SPEDSDto speds;
  private final Map<SessionId, SessionInformation> sessionInfo;
  private final SealCreator sealCreator;
  private final CertificatePrivateKeysEntry keys;

  public SesPubEnvHandler(SPEDSDto speds, TransportHost transportHost, ObjectMapper sharedObject,
      Map<SessionId, SessionInformation> sessionInfo,
      CertificatePrivateKeysEntry keys) {
    this.transportHost = transportHost;
    this.sharedMapper = sharedObject;
    this.speds = speds;
    this.sessionInfo = sessionInfo;
    this.sealCreator = new SealCreator();
    this.keys = keys;
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_PUB_ENV;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Vérifier l’envoi.
    boolean verifyStamp = message.spdu().getStamp().equals("0");
    if (!verifyStamp) {
      throw new SilentIgnoreException("Stamp is invalid, should be 0 - "
          + message.spdu().getStamp());
    }

    // Traiter le contenu
    SesPubEnvDto sesPubEnvDto = null;
    try {
      sesPubEnvDto =
          sharedMapper.readValue((String) message.spdu().getContent(), SesPubEnvDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    SessionInformation sessionInfo = SessionInformation.builder()
        .sessionId(new SessionId(sesPubEnvDto.getSession()))
        .initiatorId(message.sidu().getContext().getSourceCode())
        .initiatorIri(message.sidu().getContext().getSourceIri())
        .peerId(message.sidu().getContext().getDestinationCode())
        .peerIri(message.sidu().getContext().getDestinationIri())
        .initiatorPubKey(sesPubEnvDto.getContent())
        .build();
    this.sessionInfo.put(sessionInfo.sessionId, sessionInfo);

    // Transmettre la réponse à la couche inférieure.
    // Création SPDU
    String serialSession = sessionInfo.sessionId.id().toString();
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_PUB_REC,
        message.spdu().getHeader().getId(),
        false,
        speds);

    String stamp = sealCreator.createSeal(serialSession, keys.getPrivateKey(),
        sharedMapper);
    Spdu spdu = new Spdu(spduHeader, stamp, serialSession);

    // Création SIDU
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

    dispatch(sidu);
    logger.trace(Trace.EXIT_METHOD_0);
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
