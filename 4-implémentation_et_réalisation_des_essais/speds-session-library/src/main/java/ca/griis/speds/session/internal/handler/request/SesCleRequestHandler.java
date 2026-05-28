
package ca.griis.speds.session.internal.handler.request;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
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
import ca.griis.speds.session.internal.security.crypto.SessionKeyDestroyer;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;

public final class SesCleRequestHandler {
  private static final GriisLogger logger = getLogger(SesCleRequestHandler.class);

  private final ObjectMapper mapper;
  private final CryptographyService cryptographyService;
  private final VersionDto version;
  private final Map<SessionId, SessionInformation> sessionInformations;
  private final SessionSecurityService securityService;

  public SesCleRequestHandler(HostStartupContext hostStartupContext) {
    logger.trace(Trace.ENTER_METHOD_1, "hostStartupContext", hostStartupContext);

    this.sessionInformations = hostStartupContext.sessions().asMap();
    this.mapper = hostStartupContext.sharedMapper();
    this.version = hostStartupContext.version();
    this.cryptographyService = hostStartupContext.cryptographyService();
    this.securityService = new SessionSecurityService(hostStartupContext.cryptographyService());
  }

  public ExpandedSessionSidu handle(SessionId sessionId, ExpandedSidu expandedSidu)
      throws JsonProcessingException, NoSuchAlgorithmException, InvalidKeySpecException {
    logger.trace(Trace.ENTER_METHOD_2, "sessionId", sessionId, "expandedSidu", expandedSidu);
    final SessionInformation session = sessionInformations.get(sessionId);

    // Déterminer / mémoriser SKAK
    final SesSakRecDto sesSakRecDto =
        mapper.readValue((String) expandedSidu.spdu().getContent(), SesSakRecDto.class);

    final var peerChoice =
        securityService.convertToDhPublicKey(sesSakRecDto.getValue());
    final SecretKey skak =
        cryptographyService.getDiffieHellmanSecretKey(SESSION, session.firstChoice(),
            peerChoice);

    final UUID sessionToken = UUID.randomUUID();
    final SessionInformation updatedInfo = SessionInformation.builder()
        .of(session)
        .skak(skak)
        .token(sessionToken)
        .build();
    sessionInformations.put(session.sessionId(), updatedInfo);

    // Procéder à l’envoi de la clé de chiffrement SDEK
    // Créer SDU
    final byte[] keyBytes = updatedInfo.sdek().getEncoded();
    final String serialKey = Base64.getEncoder().encodeToString(keyBytes);
    SessionKeyDestroyer.destroy(keyBytes);

    final KeyTransferDto keyTransferDto =
        new KeyTransferDto(serialKey, updatedInfo.pgaId(), sessionToken);
    final byte[] cryptedKeyTrans = cryptographyService.encryptSymmetric(SESSION, skak,
        mapper.writeValueAsBytes(keyTransferDto));
    final String serialKeyTrans = Base64.getEncoder().encodeToString(cryptedKeyTrans);
    final SesCleEnvDto sdu = new SesCleEnvDto(serialKeyTrans, updatedInfo.sessionId().id());
    final String serialSdu = mapper.writeValueAsString(sdu);

    // Construire la SPDU
    final SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_CLE_ENV,
        UUID.randomUUID(), false, version);

    final byte[] serialSduHash = cryptographyService.hash(SESSION, serialSdu.getBytes(
        StandardCharsets.UTF_8));
    final String stamp = Base64.getEncoder()
        .encodeToString(cryptographyService.encryptSymmetric(SESSION, skak, serialSduHash));
    final Spdu spdu = new Spdu(spduHeader, stamp, mapper.writeValueAsString(sdu));

    // Construire la SIDU
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
            new ExpandedSidu(sidu, spdu, MsgType.SES_CLE_ENV));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
