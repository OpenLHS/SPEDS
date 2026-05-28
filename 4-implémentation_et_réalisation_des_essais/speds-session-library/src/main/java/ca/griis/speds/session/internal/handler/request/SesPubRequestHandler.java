
package ca.griis.speds.session.internal.handler.request;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.pub.SesPubEnvDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Pidu;
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
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;

public final class SesPubRequestHandler {
  private static final GriisLogger logger = getLogger(SesPubRequestHandler.class);

  private final ObjectMapper mapper;
  private final VersionDto version;
  private final AuthorizationService projectService;
  private final CertificatePrivateKeysEntry pair;
  private final Map<SessionId, SessionInformation> sessions;
  private final SessionSecurityService securityService;

  public SesPubRequestHandler(HostStartupContext hostStartupContext) {
    logger.trace(Trace.ENTER_METHOD_1, "hostStartupContext", hostStartupContext);

    this.projectService = hostStartupContext.projectService();
    this.sessions = hostStartupContext.sessions().asMap();
    this.pair = hostStartupContext.hostKeys();
    this.mapper = hostStartupContext.sharedMapper();
    this.version = hostStartupContext.version();
    this.securityService = new SessionSecurityService(hostStartupContext.cryptographyService());
  }

  public ExpandedSessionSidu handle(Pidu pidu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "pidu", pidu);

    final SessionId sessionId = new SessionId(UUID.randomUUID());
    final PublicKey publicKey = pair.getCertficate().getPublicKey();
    final String initiatorIri = projectService.getEntityIri(pidu.getContext().getPga(),
        pidu.getContext().getSourceCode()).toString();
    final String peerIri = projectService.getEntityIri(pidu.getContext().getPga(),
        pidu.getContext().getDestinationCode()).toString();

    final SecretKey secretKey = securityService.base64ToSecretKey(pidu.getContext().getSdek());

    final SessionInformation sessionInfo = SessionInformation.builder()
        .sessionId(sessionId)
        .initiatorId(pidu.getContext().getSourceCode())
        .initiatorIri(initiatorIri)
        .initiatorPubKey(publicKey)
        .sdek(secretKey)
        .peerId(pidu.getContext().getDestinationCode())
        .peerIri(peerIri)
        .piduMessage(pidu.getMessage())
        .pgaId(pidu.getContext().getPga())
        .numberOfMessage(0)
        .build();
    sessions.put(sessionId, sessionInfo);
    final SessionInformation currentSession = sessions.get(sessionId);

    // Créer une SDU SES.PUB.ENV pour transmettre la clé publique avec le numéro de la session

    // Créer une SDU de type SES_PUB_ENV
    final String pubKey =
        Base64.getEncoder().encodeToString(currentSession.initiatorPubKey().getEncoded());
    final SesPubEnvDto sesPubEnvDto = new SesPubEnvDto(pubKey, currentSession.sessionId().id());

    // Construire la SPDU
    final SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_PUB_ENV, UUID.randomUUID(),
        false, version);
    final Spdu spdu = new Spdu(spduHeader, "0", mapper.writeValueAsString(sesPubEnvDto));

    // Construire le SIDU
    final String sourceIri = currentSession.initiatorIri();
    final String destinationIri = currentSession.peerIri();
    final SiduContext siduContext = new SiduContext(
        pidu.getContext().getSourceCode(),
        pidu.getContext().getDestinationCode(),
        sourceIri,
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        destinationIri,
        false);

    final Sidu sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));
    final ExpandedSessionSidu result =
        new ExpandedSessionSidu(sessionId, new ExpandedSidu(sidu, spdu, MsgType.SES_PUB_ENV));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
