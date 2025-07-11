/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ClientSession.
 * @brief @~english Implementation of the ClientSession class.
 */

package ca.griis.speds.session.internal.service;

import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.cryptography.symmetric.encryption.AesGcmDecryptor;
import ca.griis.cryptography.symmetric.encryption.AesGcmEncryptor;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Debug;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.api.exception.InvalidTokenException;
import ca.griis.speds.session.api.exception.SessionTerminaisonFailedException;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.PiduContext;
import ca.griis.speds.session.internal.contract.SesPubEnvDto;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.PiduAndSession;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.session.internal.handler.initiator.InitiatorHandlerRegistry;
import ca.griis.speds.session.internal.model.PendingConfirmation;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.Poller;
import ca.griis.speds.session.internal.service.crypto.KeyAgreement;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import ca.griis.speds.session.internal.util.KeyAlgorithm;
import ca.griis.speds.session.internal.util.KeyMapping;
import ca.griis.speds.transport.api.TransportHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
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
 * @brief @~french Implémente la logique métier d’un client gérant la couche session
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
 *      2025-03-17 [SSC] - Implémentation initiale<br>
 *      2025-05-14 [CB] - Refact - itération 2<br>
 *      2025-06-29 [MD] - Refact - itération 4<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class ClientSession {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(ClientSession.class);

  private static final ExpandedSidu poisonPill = new ExpandedSidu(null, null, null);

  private final PgaService pgaService;
  private final ObjectMapper sharedMapper;
  private final SealCreator sealCreator;
  private final SPEDSDto speds;
  private final TransportHost transportHost;
  private final Poller poller;

  private final ConcurrentLinkedQueue<PendingConfirmation> pendingResponse;
  private final BlockingQueue<ExpandedSidu> messageWeHandle;
  private final Map<SessionId, SessionInformation> sessionInformations;
  private final CertificatePrivateKeysEntry hostCertificate;
  private final Duration clientTimeout;
  private final LinkedBlockingQueue<InterfaceDataUnit23Dto> pidus;

  public ClientSession(HostStartupContext hostStartupContext, Poller poller,
      LinkedBlockingQueue<InterfaceDataUnit23Dto> pidus) {
    logger.trace(Trace.ENTER_METHOD_2, "hostStartupContext", hostStartupContext, "poller", poller);
    this.messageWeHandle = new LinkedBlockingDeque<>();
    this.pendingResponse = new ConcurrentLinkedQueue<>();
    this.pidus = pidus;
    this.pgaService = hostStartupContext.pgaService();
    this.sessionInformations = new ConcurrentHashMap<>();
    this.hostCertificate = hostStartupContext.hostKeys();
    this.sharedMapper = hostStartupContext.sharedMapper();
    this.speds = hostStartupContext.spedsDto();
    this.transportHost = hostStartupContext.transportHost();
    this.clientTimeout = hostStartupContext.responseTimeout();
    this.poller = poller;
    HandlerRegistry handlerRegistry = new InitiatorHandlerRegistry(sharedMapper, pgaService,
        messageWeHandle, sessionInformations);
    this.poller.registerHandlers(handlerRegistry);
    this.sealCreator = new SealCreator();
  }

  public void close() {
    closePreservingSessionStates();
    sessionInformations.clear();
  }

  public void closePreservingSessionStates() {
    saveSessionState();

    poller.stop();
    transportHost.close();
    pendingResponse.clear();
    messageWeHandle.clear();

    try {
      messageWeHandle.put(poisonPill);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  public void clearSessionStates() {
    sessionInformations.clear();
  }

  public void sendMessageWithManagedSession(Pidu pidu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "pidu", pidu);
    // init a session
    SessionId sessionId = new SessionId(UUID.randomUUID());

    PublicKey publicKey = hostCertificate.getCertficate().getPublicKey();
    final String initiatorPubKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
    final String initiatorIri = pgaService.getIri(pidu.getContext().getPga(),
        pidu.getContext().getSourceCode());
    final String peerIri = pgaService.getIri(pidu.getContext().getPga(),
        pidu.getContext().getDestinationCode());

    byte[] keyBytes = Base64.getDecoder().decode(pidu.getContext().getSdek());
    SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

    SessionInformation sessionInfo = SessionInformation.builder()
        .sessionId(sessionId)
        .initiatorId(pidu.getContext().getSourceCode())
        .initiatorIri(initiatorIri)
        .initiatorPubKey(initiatorPubKey)
        .sdek(secretKey)
        .peerId(pidu.getContext().getDestinationCode())
        .peerIri(peerIri)
        .piduMessage(pidu.getMessage())
        .pgaId(pidu.getContext().getPga())
        .numberOfMessage(0)
        .build();
    this.sessionInformations.put(sessionId, sessionInfo);

    // call initSession
    sendPubEnv(pidu, sessionId);

    Optional<Pidu> outPidu = Optional.empty();

    while (!pendingResponse.isEmpty()) {
      try {
        ExpandedSidu expandedSidu =
            messageWeHandle.poll(clientTimeout.getSeconds(), TimeUnit.SECONDS);
        Objects.requireNonNull(expandedSidu);

        if (!expandedSidu.equals(poisonPill)) {
          PiduAndSession msgResponse = handleSession(expandedSidu);

          if (outPidu.isEmpty() && msgResponse != null) {
            outPidu = Optional.ofNullable(msgResponse.pidu());
            pidus.add(outPidu.get());
          }
        } else {
          pendingResponse.clear();
          logger.debug(Debug.VARIABLE_LOGGING_1, "poisonPill", poisonPill);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  private PiduAndSession handleSession(ExpandedSidu expandedSidu) throws JsonProcessingException {
    PiduAndSession msgResponse = null;
    switch (expandedSidu.msgType()) {
      case SES_PUB_REC -> sendSkak(expandedSidu);
      case SES_SAK_REC -> sendSdek(expandedSidu);
      case SES_CLE_REC -> sendMsg(expandedSidu);
      case SES_MSG_REC -> msgResponse = retrieveAnswer(expandedSidu);
      case SES_FIN_REC -> closeSession(expandedSidu);
      default -> throw new IllegalArgumentException("Unexpected message type: "
          + expandedSidu.msgType());
    }

    if (msgResponse != null) {
      // Supprimer du Tableau des réponses en attente, l’entrée de SES.MSG.ENV d’identifiant
      // id.
      PendingConfirmation match = this.pendingResponse.stream()
          .filter(x -> x.uniqueId().equals(expandedSidu.spdu().getHeader().getId()))
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Element not found"));
      this.pendingResponse.remove(match);

      // Session is done we close
      sendClose(msgResponse);
    }

    return msgResponse;
  }

  public void sendPubEnv(Pidu pidu, SessionId sessionId) {
    logger.trace(Trace.ENTER_METHOD_2, "pidu", pidu, "sessionId", sessionId);

    SessionInformation currentSession = this.sessionInformations.get(sessionId);

    // Créer une SDU SES.PUB.ENV pour transmettre la clé publique avec le numéro de la session

    // Créer une SDU de type SES_PUB_ENV
    String pubKey = currentSession.initiatorPubKey;
    SesPubEnvDto sesPubEnvDto = new SesPubEnvDto(pubKey, currentSession.sessionId.id());

    // Construire la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_PUB_ENV, UUID.randomUUID(),
        false,
        speds);
    Spdu spdu = null;
    try {
      spdu = new Spdu(spduHeader, "0", sharedMapper.writeValueAsString(sesPubEnvDto));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Construire le SIDU
    String sourceIri = currentSession.initiatorIri;
    String destinationIri = currentSession.peerIri;
    SiduContext siduContext = new SiduContext(
        pidu.getContext().getSourceCode(),
        pidu.getContext().getDestinationCode(),
        sourceIri,
        spduHeader.getId(),
        destinationIri,
        false);

    String serialSidu;
    try {
      Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
      serialSidu = sharedMapper.writeValueAsString(sidu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Transmettre la IDU à la couche inférieure et attendre qu’une réponse de type [SES.PUB.REC] et
    // d’identifiant le id
    pendingResponse
        .add(new PendingConfirmation(spduHeader.getId(), sessionId, MsgType.SES_PUB_ENV));

    logger.trace(Trace.ALGORITHM_1, "serialSidu", serialSidu);

    transportHost.dataRequest(serialSidu);
    transportHost.dataConfirm();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void sendSkak(ExpandedSidu expandedSidu) throws JsonProcessingException {
    // Supprimer du Tableau des réponses en attente, l’entrée de SES.PUB.ENV d’identifiant id.
    PendingConfirmation match = this.pendingResponse.stream()
        .filter(x -> x.uniqueId().equals(expandedSidu.spdu().getHeader().getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Element not found"));
    this.pendingResponse.remove(match);
    SessionInformation session = this.sessionInformations.get(match.sessionId());

    // Créer une SDU de type SES.SAK.ENV
    // Déterminer le choix de la clef
    KeyAgreement keyAgreement = new KeyAgreement();
    KeyPair choiceKeyPair = keyAgreement.generateChoicePointKey();
    String choice = Base64.getEncoder().encodeToString(choiceKeyPair.getPublic().getEncoded());
    String serialId = session.sessionId.id().toString();
    SesSakEnvDto sdu = new SesSakEnvDto(choice, serialId);
    String serialSdu = sharedMapper.writeValueAsString(sdu);
    session.firstChoice = choiceKeyPair;

    // Construire la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_SAK_ENV, UUID.randomUUID(),
        false, speds);
    String stamp = sealCreator.createSeal(serialSdu, hostCertificate.getPrivateKey(), sharedMapper);
    Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);

    // Construire le SIDU
    SiduContext siduContext = new SiduContext(
        session.initiatorId,
        session.peerId,
        session.initiatorIri,
        spduHeader.getId(),
        session.peerIri,
        false);
    String serialSidu;
    try {
      Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
      serialSidu = sharedMapper.writeValueAsString(sidu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Transmettre la IDU à la couche inférieure && inscrire qu'on attend une réponse pour
    // SES_SAK_ENV
    pendingResponse
        .add(new PendingConfirmation(spduHeader.getId(), session.sessionId, MsgType.SES_SAK_ENV));

    transportHost.dataRequest(serialSidu);
    transportHost.dataConfirm();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void sendSdek(ExpandedSidu expandedSidu) throws JsonProcessingException {
    // Supprimer du Tableau des réponses en attente, l’entrée de SES.SAK.ENV d’identifiant id.
    PendingConfirmation match = this.pendingResponse.stream()
        .filter(x -> x.uniqueId().equals(expandedSidu.spdu().getHeader().getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Element not found"));
    this.pendingResponse.remove(match);
    SessionInformation session = this.sessionInformations.get(match.sessionId());

    // Déterminer / mémoriser SKAK
    KeyAgreement keyAgreement = new KeyAgreement();
    SesSakRecDto sesSakRecDto =
        sharedMapper.readValue((String) expandedSidu.spdu().getContent(), SesSakRecDto.class);
    PublicKey peerChoice =
        KeyMapping.getPublicKeyFromString(sesSakRecDto.getValue(), KeyAlgorithm.X25519);
    byte[] skakByte = keyAgreement.completeKeyAgreementNegotiation(session.firstChoice,
        peerChoice);
    SecretKey skak = KeyMapping.getAesSecretKeyFromByte(skakByte);
    session.skak = skak;

    UUID sessionToken = UUID.randomUUID();
    SessionInformation updatedInfo = SessionInformation.builder()
        .of(session)
        .token(sessionToken)
        .build();
    sessionInformations.put(match.sessionId(), updatedInfo);

    // Procéder à l’envoi de la clé de chiffrement SDEK
    // Créer SDU
    byte[] keyBytes = session.sdek.getEncoded();
    String serialKey = Base64.getEncoder().encodeToString(keyBytes);
    KeyTransferDto keyTransferDto = new KeyTransferDto(serialKey, session.pgaId, sessionToken);
    AesGcmEncryptor symmetricEncrypt = new AesGcmEncryptor(skak);
    byte[] cryptedKeyTrans =
        symmetricEncrypt.encrypt(sharedMapper.writeValueAsBytes(keyTransferDto));
    String serialKeyTrans = Base64.getEncoder().encodeToString(cryptedKeyTrans);
    SesCleEnvDto sdu = new SesCleEnvDto(serialKeyTrans, session.sessionId.id());

    // Construire la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_CLE_ENV,
        UUID.randomUUID(), false, speds);

    String stamp = sealCreator.createSymmetricalSeal(sdu, skak, sharedMapper);
    Spdu spdu = new Spdu(spduHeader, stamp, sharedMapper.writeValueAsString(sdu));

    // Construire la SIDU
    SiduContext siduContext = new SiduContext(
        session.initiatorId,
        session.peerId,
        session.initiatorIri,
        spdu.getHeader().getId(),
        session.peerIri,
        false);
    String serialSidu;
    try {
      Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
      serialSidu = sharedMapper.writeValueAsString(sidu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Transmettre la IDU à la couche inférieur && inscrire qu'on attend une réponse pour
    // SES_CLE_ENV
    pendingResponse
        .add(new PendingConfirmation(spduHeader.getId(), session.sessionId, MsgType.SES_CLE_ENV));
    logger.trace(Trace.ALGORITHM_1, serialSidu, serialSidu);

    transportHost.dataRequest(serialSidu);
    transportHost.dataConfirm();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void sendMsg(ExpandedSidu expandedSidu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);

    // Supprimer du Tableau des réponses en attente, l’entrée de SES.CLE.ENV d’identifiant id.
    PendingConfirmation match = this.pendingResponse.stream()
        .filter(x -> x.uniqueId().equals(expandedSidu.spdu().getHeader().getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Element not found"));
    this.pendingResponse.remove(match);
    SessionInformation session = this.sessionInformations.get(match.sessionId());
    SesCleRecDto sesCleRecDto =
        sharedMapper.readValue((String) expandedSidu.spdu().getContent(), SesCleRecDto.class);

    try {
      byte[] encryptedToken = Base64.getDecoder().decode(sesCleRecDto.getContent());
      AesGcmDecryptor aesGcmDecryptor = new AesGcmDecryptor(session.sdek);
      byte[] decryptedToken = aesGcmDecryptor.decrypt(encryptedToken);
      UUID token = UUID.fromString(new String(decryptedToken, StandardCharsets.UTF_8));
      if (token.compareTo(session.token) != 0) {
        throw new InvalidTokenException("Token is invalid");
      }
    } catch (Exception e) {
      throw new InvalidTokenException("Impossible to retrieve the token");
    }

    // Procéder à l’envoi du message à transmettre
    // Créer une SDU SES.MSG.ENV
    SesMsgEnvDto sdu = new SesMsgEnvDto(session.piduMessage, session.sessionId.id());

    // Construire la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_MSG_ENV,
        UUID.randomUUID(),
        false,
        speds);
    String stamp = sealCreator.createSymmetricalSeal(sdu, session.skak, sharedMapper);
    Spdu spdu = new Spdu(spduHeader, stamp, sharedMapper.writeValueAsString(sdu));

    // Construire la SIDU
    SiduContext siduContext = new SiduContext(
        session.initiatorId,
        session.peerId,
        session.initiatorIri,
        spduHeader.getId(),
        session.peerIri,
        false);
    String serialSidu;
    try {
      Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
      serialSidu = sharedMapper.writeValueAsString(sidu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    session.trackingNumber = siduContext.getTrackingNumber();
    this.sessionInformations.put(session.sessionId, session);

    // Transmettre la IDU à la couche inférieur && inscrire qu'on attend une réponse pour
    // SES_MSG_ENV
    pendingResponse
        .add(new PendingConfirmation(spduHeader.getId(), session.sessionId, MsgType.SES_MSG_ENV));
    logger.trace(Trace.ALGORITHM_1, serialSidu, serialSidu);

    transportHost.dataRequest(serialSidu);
    transportHost.dataConfirm();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public PiduAndSession retrieveAnswer(ExpandedSidu expandedSidu) {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);

    // Incrémenter le nombre de messages traités avec succès durant la session.
    SesMsgRecDto sesMsgRecDto = null;
    try {
      sesMsgRecDto =
          sharedMapper.readValue((String) expandedSidu.spdu().getContent(), SesMsgRecDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    SessionInformation session = sessionInformations.get(new SessionId(sesMsgRecDto.getSession()));
    session.numberOfMessage += 1;
    sessionInformations.put(session.sessionId, session);

    // Transmettre la réponse attendue à la couche supérieure
    // PIDU
    // Transmettre la PIDU à la couche supérieure.
    byte[] keyBytes = session.sdek.getEncoded();
    String serialSdek = Base64.getEncoder().encodeToString(keyBytes);
    PiduContext contextDto = new PiduContext(
        session.pgaId,
        session.initiatorId,
        session.peerId,
        serialSdek,
        session.trackingNumber,
        false);
    Pidu pidu = new Pidu(contextDto, sesMsgRecDto.getContent());

    PiduAndSession piduAndSession = new PiduAndSession(pidu, session);
    logger.trace(Trace.EXIT_METHOD_1, "piduAndSession", piduAndSession);
    return piduAndSession;
  }

  public void sendClose(PiduAndSession msgResponse) {
    logger.trace(Trace.ENTER_METHOD_1, "msgResponse", msgResponse);

    // Procéder à l’envoi de la fin de session
    // Créer SDU
    SessionInformation session = msgResponse.sessionInformation();
    UUID token = UUID.randomUUID();

    SessionInformation updatedInfo = SessionInformation.builder()
        .of(session)
        .token(token)
        .build();
    sessionInformations.put(msgResponse.sessionInformation().sessionId, updatedInfo);

    SesFinEnvDto sdu = new SesFinEnvDto(token,
        session.numberOfMessage.toString(), session.sessionId.id());
    String serialSdu;
    try {
      serialSdu = sharedMapper.writeValueAsString(sdu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Construire la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_FIN_ENV,
        UUID.randomUUID(),
        false,
        speds);
    String stamp = sealCreator.createSymmetricalSeal(serialSdu, session.skak, sharedMapper);
    Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);

    // Construire la SIDU
    SiduContext siduContext = new SiduContext(
        session.initiatorId,
        session.peerId,
        session.initiatorIri,
        spdu.getHeader().getId(),
        session.peerIri,
        false);

    try {
      Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
      String serialSidu = sharedMapper.writeValueAsString(sidu);
      pendingResponse.add(new PendingConfirmation(token, session.sessionId,
          MsgType.SES_FIN_ENV));
      logger.trace(Trace.ALGORITHM_1, "serialSidu", serialSidu);

      transportHost.dataRequest(serialSidu);
      transportHost.dataConfirm();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void closeSession(ExpandedSidu expandedSidu) {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);
    // Supprimer du Tableau des réponses en attente, l’entrée de SES.FIN.ENV d’identifiant id.

    SesFinRecDto sdu;
    try {
      sdu = sharedMapper.readValue((String) expandedSidu.spdu().getContent(), SesFinRecDto.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException(e);
    }

    PendingConfirmation match = this.pendingResponse.stream()
        .filter(x -> x.uniqueId().equals(sdu.getToken()))
        .findFirst()
        .orElseThrow(() -> new SessionTerminaisonFailedException("Element not found"));
    this.pendingResponse.remove(match);
    SessionInformation session = this.sessionInformations.get(match.sessionId());

    // Retirer les informations de session
    this.sessionInformations.remove(session.sessionId);

    // Vérifier la correspondance avec le message d'envoi
    if (!sdu.getToken().equals(match.uniqueId())) {
      throw new SessionTerminaisonFailedException("Token does not match original token - "
          + "orignal=" + match.uniqueId() + " response=" + sdu.getToken());
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void loadSessionState() {
    // TODO
  }

  public void saveSessionState() {
    // TODO
  }

  public Map<SessionId, SessionInformation> getSessionInfo() {
    return Map.copyOf(this.sessionInformations);
  }

  public ConcurrentLinkedQueue<PendingConfirmation> getPendingResponse() {
    return pendingResponse;
  }
}
