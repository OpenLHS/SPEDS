/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesCleEnvHandler.
 * @brief @~english Implementation of the SesCleEnvHandler class.
 */

package ca.griis.speds.session.internal.handler.peer;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.cryptography.encryption.Encryptor;
import ca.griis.cryptography.symmetric.encryption.AesGcmDecryptor;
import ca.griis.cryptography.symmetric.encryption.AesGcmEncryptor;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.exception.VerifyException;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
 * @brief @~french Implémentation du gestionnaire de message SES.CLE.ENV
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
public class SesCleEnvHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesCleEnvHandler.class);

  private final SPEDSDto speds;
  private final ObjectMapper sharedMapper;
  private final SealVerifier sealVerifier;
  private final PgaService pgaService;
  private final SealCreator sealCreator;
  private final TransportHost transportHost;
  private final Map<SessionId, SessionInformation> sessionInfo;

  public SesCleEnvHandler(SPEDSDto speds, ObjectMapper sharedObject, PgaService pgaService,
      TransportHost transportHost, Map<SessionId, SessionInformation> sessionInfo) {
    logger.trace(Trace.ENTER_METHOD_5, "speds", speds, "sharedObject", sharedObject, "pgaService",
        pgaService, "transportHost", transportHost, "sessionInfo", sessionInfo);
    this.sharedMapper = sharedObject;
    this.pgaService = pgaService;
    this.sealCreator = new SealCreator();
    this.sealVerifier = new SealVerifier();
    this.speds = speds;
    this.transportHost = transportHost;
    this.sessionInfo = sessionInfo;
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_CLE_ENV;
  }

  @Override
  public void handle(ExpandedSidu message) throws SilentIgnoreException {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    // Récupérer les informations de la session.
    SesCleEnvDto sesCleEnvDto = null;
    try {
      sesCleEnvDto =
          sharedMapper.readValue((String) message.spdu().getContent(), SesCleEnvDto.class);
    } catch (JsonProcessingException e) {
      throw new SilentIgnoreException("Session required");
    }

    SessionId sessionId = new SessionId(sesCleEnvDto.getSession());
    SessionInformation sessionInformation = sessionInfo.get(sessionId);
    if (sessionInformation == null) {
      throw new SilentIgnoreException("Session required");
    }

    // Authentifier la source de l’envoi.
    // Stamp
    verifyStamp(message, sessionInformation, sesCleEnvDto, sessionId);

    // Traiter le contenu
    AesGcmDecryptor aesGcmDecryptor = new AesGcmDecryptor(sessionInformation.skak);
    byte[] encryptedBytes = Base64.getDecoder().decode(sesCleEnvDto.getContent());
    byte[] decryptedBytes = aesGcmDecryptor.decrypt(encryptedBytes);
    KeyTransferDto keyTransferDto;
    try {
      keyTransferDto = sharedMapper.readValue(decryptedBytes, KeyTransferDto.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    byte[] keyBytes = Base64.getDecoder().decode(keyTransferDto.getSdek());
    SecretKey sdek = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

    SessionInformation updatedInfo = SessionInformation.builder()
        .of(sessionInformation)
        .pgaId(keyTransferDto.getPgaNumber())
        .sdek(sdek)
        .build();
    sessionInfo.put(sessionId, updatedInfo);

    // Vérifier légitimité
    verifyLegit(updatedInfo, sessionId);

    // Transmettre la réponse
    Sidu sidu = buildSidu(message, sdek, keyTransferDto, sessionId, sessionInformation);

    // dispatch
    dispatch(sidu);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  private Sidu buildSidu(ExpandedSidu message, SecretKey sdek, KeyTransferDto keyTransferDto,
      SessionId sessionId, SessionInformation sessionInformation) {
    // SDU
    Encryptor encryptor = new AesGcmEncryptor(sdek);
    byte[] encryptedToken =
        encryptor.encrypt(keyTransferDto.getToken().toString().getBytes(StandardCharsets.UTF_8));
    String encryptedTokenStr = Base64.getEncoder().encodeToString(encryptedToken);
    SesCleRecDto sesCleRecDto = new SesCleRecDto(encryptedTokenStr, sessionId.id());
    String serialSesClef;
    try {
      serialSesClef = sharedMapper.writeValueAsString(sesCleRecDto);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_CLE_REC,
        message.spdu().getHeader().getId(),
        false,
        this.speds);
    String stamp = sealCreator.createSymmetricalSeal(serialSesClef, sessionInformation.skak,
        sharedMapper);
    Spdu spdu = null;
    spdu = new Spdu(spduHeader, stamp, serialSesClef);

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
    return sidu;
  }

  private void verifyStamp(ExpandedSidu message, SessionInformation sessionInformation,
      SesCleEnvDto sesCleEnvDto, SessionId sessionId) throws SilentIgnoreException {
    boolean verifyStamp = sealVerifier.verifySymmetricalSeal(sesCleEnvDto,
        sessionInformation.skak,
        message.spdu().getStamp(),
        sharedMapper);
    if (!verifyStamp) {
      sessionInfo.remove(sessionId);
      throw new SilentIgnoreException("Stamp is invalid");
    }
  }

  private void verifyLegit(SessionInformation updatedInfo, SessionId sessionId) {
    boolean isLegit = pgaService.verifyLegitimacy(updatedInfo.pgaId, updatedInfo.initiatorId,
        updatedInfo.initiatorPubKey);

    if (!isLegit) {
      sessionInfo.remove(sessionId);
      throw new VerifyException("Untrusted source: rejected by the PGA.");
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
