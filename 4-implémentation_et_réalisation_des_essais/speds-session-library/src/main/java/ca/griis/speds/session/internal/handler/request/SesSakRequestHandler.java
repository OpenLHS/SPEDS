
package ca.griis.speds.session.internal.handler.request;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.ExpandedSessionSidu;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public final class SesSakRequestHandler {
  private static final GriisLogger logger = getLogger(SesSakRequestHandler.class);

  private final ObjectMapper mapper;
  private final CryptographyService cryptographyService;
  private final VersionDto version;
  private final Map<SessionId, SessionInformation> sessionInformations;
  private final CertificatePrivateKeysEntry hostKeys;

  public SesSakRequestHandler(HostStartupContext hostStartupContext) {
    logger.trace(Trace.ENTER_METHOD_1, "hostStartupContext", hostStartupContext);

    this.sessionInformations = hostStartupContext.sessions().asMap();
    this.hostKeys = hostStartupContext.hostKeys();
    this.mapper = hostStartupContext.sharedMapper();
    this.version = hostStartupContext.version();
    this.cryptographyService = hostStartupContext.cryptographyService();
  }

  public ExpandedSessionSidu handle(SessionId sessionId) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "sessionId", sessionId);

    final SessionInformation session = sessionInformations.get(sessionId);

    // Créer une SDU de type SES.SAK.ENV
    // Déterminer le choix de la clef
    final KeyPair choiceKeyPair = cryptographyService.chooseDiffieHellmanValue(SESSION);
    final String choice =
        Base64.getEncoder().encodeToString(choiceKeyPair.getPublic().getEncoded());
    final String serialId = session.sessionId().id().toString();
    final SesSakEnvDto sdu = new SesSakEnvDto(choice, serialId);
    final String serialSdu = mapper.writeValueAsString(sdu);

    final SessionInformation updatedInfo = SessionInformation.builder()
        .of(session)
        .firstChoice(choiceKeyPair)
        .build();
    sessionInformations.put(session.sessionId(), updatedInfo);

    // Construire la SPDU
    final SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_SAK_ENV, UUID.randomUUID(),
        false, version);
    final String stamp = Base64.getEncoder().encodeToString(
        cryptographyService.sign(SESSION, hostKeys.getPrivateKey(), serialSdu.getBytes(
            StandardCharsets.UTF_8)));
    final Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);

    // Construire le SIDU
    final SiduContext siduContext = new SiduContext(
        updatedInfo.initiatorId(),
        updatedInfo.peerId(),
        updatedInfo.initiatorIri(),
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        updatedInfo.peerIri(),
        false);
    final Sidu sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));

    final ExpandedSessionSidu result =
        new ExpandedSessionSidu(updatedInfo.sessionId(),
            new ExpandedSidu(sidu, spdu, MsgType.SES_SAK_ENV));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
