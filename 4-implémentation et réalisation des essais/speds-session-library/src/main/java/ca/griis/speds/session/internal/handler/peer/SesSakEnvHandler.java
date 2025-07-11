/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesSakEnvHandler.
 * @brief @~english Implementation of the SesSakEnvHandler class.
 */

package ca.griis.speds.session.internal.handler.peer;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.KeyAgreementException;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.domain.SkakWithPubKey;
import ca.griis.speds.session.internal.handler.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.SilentIgnoreException;
import ca.griis.speds.session.internal.service.crypto.KeyAgreement;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.session.internal.service.seal.SealVerifier;
import ca.griis.speds.session.internal.util.KeyAlgorithm;
import ca.griis.speds.session.internal.util.KeyMapping;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
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
 * @brief @~french Implémentation du gestionnaire de message SES.SAK.ENV
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
public class SesSakEnvHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesSakEnvHandler.class);

  private final ObjectMapper sharedMapper;
  private final SPEDSDto speds;
  private final TransportHost transportHost;
  private final CertificatePrivateKeysEntry hostCertKey;
  private final Map<SessionId, SessionInformation> sessionInfo;

  public SesSakEnvHandler(SPEDSDto speds, ObjectMapper sharedMapper, TransportHost transportHost,
      CertificatePrivateKeysEntry hostCertKey, Map<SessionId, SessionInformation> sessionInfo) {
    logger.trace(Trace.ENTER_METHOD_5, "speds", speds, "sharedMapper", sharedMapper,
        "transportHost", transportHost, "hostCertKey", hostCertKey, "sessionInfo", sessionInfo);
    this.sharedMapper = sharedMapper;
    this.transportHost = transportHost;
    this.hostCertKey = hostCertKey;
    this.speds = speds;
    this.sessionInfo = sessionInfo;
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_SAK_ENV;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Authentifier la source de l’envoi
    // Session
    SesSakEnvDto sesSakEnvDto = null;
    SessionId sessionId;
    SessionInformation sessionInformation;
    try {
      sesSakEnvDto =
          sharedMapper.readValue((String) message.spdu().getContent(), SesSakEnvDto.class);
      UUID sessionUuid = UUID.fromString(sesSakEnvDto.getSession());
      sessionId = new SessionId(sessionUuid);
      sessionInformation = sessionInfo.get(sessionId);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoreException("Session required");
    }

    if (sessionInformation == null) {
      throw new SilentIgnoreException("Session required");
    }

    // Stamp
    verifyStamp(sessionInformation.initiatorPubKey, message, sessionId);

    // Traiter le contenu
    // Création de Skak
    SkakWithPubKey skakWithPubKey;
    try {
      skakWithPubKey = createSkak(sesSakEnvDto, sessionInformation);
    } catch (Exception e) {
      throw new KeyAgreementException("Unable to finish skak creation", e);
    }

    // Transmettre la réponse à la couche inférieur
    Sidu sidu = buildSidu(message, skakWithPubKey, sessionId);

    dispatch(sidu);
    logger.trace(Trace.EXIT_METHOD_0);
  }

  private Sidu buildSidu(ExpandedSidu message, SkakWithPubKey skakWithPubKey, SessionId sessionId) {
    // Créer une SDU SES.SAK.REC
    String serialChoice = Base64.getEncoder()
        .encodeToString(skakWithPubKey.choice().getPublic().getEncoded());
    SesSakRecDto sdu = new SesSakRecDto(serialChoice, sessionId.id());

    // Construire la SPDU=
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_SAK_REC,
        message.spdu().getHeader().getId(),
        false,
        speds);
    String stamp = new SealCreator().createSeal(sdu, hostCertKey.getPrivateKey(), sharedMapper);
    Spdu spdu = null;
    try {
      spdu = new Spdu(spduHeader, stamp, sharedMapper.writeValueAsString(sdu));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

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
    return sidu;
  }

  private void verifyStamp(String key, ExpandedSidu message, SessionId sessionId)
      throws SilentIgnoreException {

    boolean verifyStamp = new SealVerifier().verifySeal(sharedMapper,
        message.spdu().getContent(),
        key,
        message.spdu().getStamp());
    if (!verifyStamp) {
      sessionInfo.remove(sessionId);
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

  private SkakWithPubKey createSkak(SesSakEnvDto sesSakEnvDto,
      SessionInformation sessionInformation) {
    logger.trace(Trace.ENTER_METHOD_2, "sesSakEnvDto", sesSakEnvDto,
        "sessionInformation", sessionInformation);

    KeyAgreement keyAgreement = new KeyAgreement();
    PublicKey initiatorPubChoice =
        KeyMapping.getPublicKeyFromString(sesSakEnvDto.getValue(), KeyAlgorithm.X25519);
    KeyPair peerChoice = keyAgreement.generateChoicePointKey();
    byte[] skakByte = keyAgreement.completeKeyAgreementNegotiation(peerChoice, initiatorPubChoice);
    SecretKey skak = KeyMapping.getAesSecretKeyFromByte(skakByte);
    sessionInformation.skak = skak;
    sessionInfo.put(sessionInformation.sessionId, sessionInformation);
    SkakWithPubKey skakWithPubKey = new SkakWithPubKey(skak, peerChoice);

    logger.trace(Trace.EXIT_METHOD_1, "skakWithPubKey", skakWithPubKey);
    return skakWithPubKey;
  }
}
