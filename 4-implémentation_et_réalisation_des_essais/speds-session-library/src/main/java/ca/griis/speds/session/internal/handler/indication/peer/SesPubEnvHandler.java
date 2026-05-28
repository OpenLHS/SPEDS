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

package ca.griis.speds.session.internal.handler.indication.peer;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.pub.SesPubEnvDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

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

  private final ObjectMapper mapper;
  private final CryptographyService cryptographyService;
  private final TransportHostAdapter transportHost;
  private final VersionDto version;
  private final Map<SessionId, SessionInformation> sessions;
  private final CertificatePrivateKeysEntry hostKeys;
  private final SessionSecurityService sessionSecurityService;

  public SesPubEnvHandler(HostStartupContext ctx) {
    this.transportHost = new TransportHostAdapter(ctx);
    this.mapper = ctx.sharedMapper();
    this.version = ctx.version();
    this.sessions = ctx.sessions().asMap();
    this.cryptographyService = ctx.cryptographyService();
    this.hostKeys = ctx.hostKeys();
    this.sessionSecurityService = new SessionSecurityService(ctx.cryptographyService());
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_PUB_ENV;
  }

  @Override
  public Optional<String> handle(ExpandedSidu message) {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    final var entityCode = message.sidu().getContext().getDestinationCode();
    SessionId sessionId = null;

    Boolean verifyStamp = message.spdu().getStamp().equals("0");
    if (!verifyStamp) {
      transportHost.response(entityCode, sessionId, message, "FAILED: InvalidStampException");
    } else {
      try {
        final SesPubEnvDto sesPubEnvDto =
            mapper.readValue((String) message.spdu().getContent(), SesPubEnvDto.class);
        final PublicKey key = sessionSecurityService.base64ToPublicKey(sesPubEnvDto.getContent());

        SessionInformation sessionInfo = SessionInformation.builder()
            .sessionId(new SessionId(sesPubEnvDto.getSession()))
            .initiatorId(message.sidu().getContext().getSourceCode())
            .initiatorIri(message.sidu().getContext().getSourceIri())
            .peerId(message.sidu().getContext().getDestinationCode())
            .peerIri(message.sidu().getContext().getDestinationIri())
            .initiatorPubKey(key)
            .build();
        sessions.put(sessionInfo.sessionId(), sessionInfo);

        sessionId = sessionInfo.sessionId();

        transportHost.response(entityCode, sessionId, message, "SUCCEED");

        // Transmettre la réponse à la couche inférieure.
        // Création SPDU
        String serialSession = sessionInfo.sessionId().id().toString();
        SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_PUB_REC,
            message.spdu().getHeader().getId(), false, version);
        final String stamp = Base64.getEncoder().encodeToString(
            cryptographyService.sign(SESSION, hostKeys.getPrivateKey(), serialSession.getBytes(
                StandardCharsets.UTF_8)));
        Spdu spdu = new Spdu(spduHeader, stamp, serialSession);

        // Création SIDU
        SiduContext siduContext = new SiduContext(
            message.sidu().getContext().getDestinationCode(),
            message.sidu().getContext().getSourceCode(),
            message.sidu().getContext().getDestinationIri(),
            Context34Dto.Service.TRANSFER,
            ServicePrimitive.REQUEST,
            message.sidu().getContext().getSourceIri(),
            false);

        final var sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));
        final var expandedSidu = new ExpandedSidu(sidu, spdu, MsgType.SES_PUB_REC);

        transportHost.request(entityCode, sessionId, expandedSidu);
      } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        logger.error(Error.IGNORED_ERROR, e);

        transportHost.response(entityCode, sessionId, message,
            "FAILED: Error during the processing of the message");
      }
    }

    Optional<String> result = Optional.empty();
    logger.trace(Trace.ENTER_METHOD_1, "result", result);
    return result;
  }
}
