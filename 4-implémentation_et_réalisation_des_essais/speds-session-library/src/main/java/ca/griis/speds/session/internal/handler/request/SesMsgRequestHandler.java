
package ca.griis.speds.session.internal.handler.request;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgEnvDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.InvalidTokenException;
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
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public final class SesMsgRequestHandler {
  private static final GriisLogger logger = getLogger(SesMsgRequestHandler.class);

  private final ObjectMapper mapper;
  private final CryptographyService cryptographyService;
  private final VersionDto version;
  private final Map<SessionId, SessionInformation> sessions;

  public SesMsgRequestHandler(HostStartupContext hostStartupContext) {
    logger.trace(Trace.ENTER_METHOD_1, "hostStartupContext", hostStartupContext);

    this.sessions = hostStartupContext.sessions().asMap();
    this.mapper = hostStartupContext.sharedMapper();
    this.version = hostStartupContext.version();
    this.cryptographyService = hostStartupContext.cryptographyService();
  }

  public ExpandedSessionSidu handle(SessionId sessionId, ExpandedSidu expandedSidu)
      throws JsonProcessingException, InvalidTokenException {
    logger.trace(Trace.ENTER_METHOD_2, "sessionId", sessionId, "expandedSidu", expandedSidu);

    final SessionInformation session = sessions.get(sessionId);
    final SesCleRecDto sesCleRecDto =
        mapper.readValue((String) expandedSidu.spdu().getContent(), SesCleRecDto.class);

    try {
      final byte[] encryptedToken = Base64.getDecoder().decode(sesCleRecDto.getContent());
      final byte[] decryptedToken =
          cryptographyService.decryptSymmetric(SESSION, session.sdek(), encryptedToken);
      final UUID token = UUID.fromString(new String(decryptedToken, StandardCharsets.UTF_8));
      if (token.compareTo(session.token()) != 0) {
        throw new InvalidTokenException("Token is invalid");
      }
    } catch (Exception e) {
      throw new InvalidTokenException("Impossible to retrieve the token");
    }

    // Procéder à l’envoi du message à transmettre
    // Créer une SDU SES.MSG.ENV
    final SesMsgEnvDto sdu = new SesMsgEnvDto(session.piduMessage(), session.sessionId().id());
    final String serialSdu = mapper.writeValueAsString(sdu);

    // Construire la SPDU
    final SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_MSG_ENV,
        UUID.randomUUID(),
        false,
        version);
    final byte[] serialSduHash = cryptographyService.hash(SESSION, serialSdu.getBytes(
        StandardCharsets.UTF_8));
    final String stamp = Base64.getEncoder()
        .encodeToString(
            cryptographyService.encryptSymmetric(SESSION, session.skak(), serialSduHash));
    final Spdu spdu = new Spdu(spduHeader, stamp, mapper.writeValueAsString(sdu));

    // Construire la SIDU
    final SiduContext siduContext = new SiduContext(
        session.initiatorId(),
        session.peerId(),
        session.initiatorIri(),
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        session.peerIri(),
        false);
    final Sidu sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));

    sessions.put(session.sessionId(), session);

    final ExpandedSessionSidu result =
        new ExpandedSessionSidu(session.sessionId(),
            new ExpandedSidu(sidu, spdu, MsgType.SES_MSG_ENV));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
