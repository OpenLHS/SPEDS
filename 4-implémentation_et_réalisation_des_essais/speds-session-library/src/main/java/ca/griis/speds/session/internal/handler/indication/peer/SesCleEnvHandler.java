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

package ca.griis.speds.session.internal.handler.indication.peer;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.KeyTransferDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.cle.SesCleRecDto;
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
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
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

  private final VersionDto version;
  private final SessionSecurityService securityService;
  private final ObjectMapper mapper;
  private final AuthorizationService projectService;
  private final TransportHostAdapter transportHost;
  private final CryptographyService cryptographyService;
  private final Map<SessionId, SessionInformation> sessions;

  public SesCleEnvHandler(HostStartupContext ctx) {
    this.transportHost = new TransportHostAdapter(ctx);
    this.mapper = ctx.sharedMapper();
    this.projectService = ctx.projectService();
    this.version = ctx.version();
    this.sessions = ctx.sessions().asMap();
    this.securityService = new SessionSecurityService(ctx.cryptographyService());
    this.cryptographyService = ctx.cryptographyService();
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_CLE_ENV;
  }

  @Override
  public Optional<String> handle(ExpandedSidu message) {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    final var entityCode = message.sidu().getContext().getDestinationCode();
    SessionId sessionId = null;

    try {
      SesCleEnvDto sesCleEnvDto =
          mapper.readValue((String) message.spdu().getContent(), SesCleEnvDto.class);

      sessionId = new SessionId(sesCleEnvDto.getSession());
      SessionInformation sessionInfo = sessions.get(sessionId);
      if (sessionInfo != null) {
        final var code = message.sidu().getContext().getSourceCode();

        if (sessionInfo.initiatorId().equals(code)) {
          final Boolean verifyStamp =
              securityService.verifyStamp(
                  Base64.getDecoder().decode(message.spdu().getStamp()),
                  ((String) message.spdu().getContent()).getBytes(StandardCharsets.UTF_8),
                  sessionInfo.skak());

          if (verifyStamp) {
            byte[] encryptedBytes = Base64.getDecoder().decode(sesCleEnvDto.getContent());
            byte[] decryptedBytes = new byte[0];

            try {
              decryptedBytes = cryptographyService.decryptSymmetric(SESSION, sessionInfo.skak(),
                  encryptedBytes);
            } catch (Exception ex) {
              transportHost.response(entityCode, sessionId, message,
                  "FAILED: Error during decryption");
            }

            if (decryptedBytes.length > 0) {
              KeyTransferDto keyTransferDto =
                  mapper.readValue(decryptedBytes, KeyTransferDto.class);
              SecretKey sdek = securityService.base64ToSecretKey(keyTransferDto.getSdek());

              SessionInformation updatedInfo = SessionInformation.builder()
                  .of(sessionInfo)
                  .pgaId(keyTransferDto.getPgaNumber())
                  .sdek(sdek)
                  .build();
              sessions.put(sessionId, updatedInfo);

              Boolean isLegit =
                  projectService.verifyEntityLegitimacy(updatedInfo.pgaId(),
                      updatedInfo.initiatorId(),
                      updatedInfo.initiatorPubKey());

              if (isLegit) {
                transportHost.response(entityCode, sessionId, message, "SUCCEED");

                var sidu = buildSidu(message, sdek, keyTransferDto, sessionId, updatedInfo);
                transportHost.request(entityCode, sessionId, sidu);
              } else {
                sessions.remove(sessionId);

                transportHost.response(entityCode, sessionId, message,
                    "FAILED: LegitimacyException");
              }
            }
          } else {
            transportHost.response(entityCode, sessionId, message,
                "FAILED: AuthenticateException");
          }
        } else {
          transportHost.response(entityCode, sessionId, message,
              "FAILED: The code from the IDU does not match the initiator ID");
        }
      } else {
        transportHost.response(entityCode, sessionId, message,
            "FAILED: UnknownSessionException");
      }
    } catch (IOException e) {
      logger.error(Error.IGNORED_ERROR, e);

      transportHost.response(entityCode, sessionId, message,
          "FAILED: Error during the processing of the message");
    }

    Optional<String> result = Optional.empty();
    logger.trace(Trace.ENTER_METHOD_1, "result", result);
    return result;
  }

  private ExpandedSidu buildSidu(ExpandedSidu message, SecretKey sdek,
      KeyTransferDto keyTransferDto,
      SessionId sessionId, SessionInformation session) throws JsonProcessingException {

    // SDU
    byte[] encryptedToken =
        cryptographyService.encryptSymmetric(SESSION, sdek,
            keyTransferDto.getToken().toString().getBytes(StandardCharsets.UTF_8));
    String encryptedTokenStr = Base64.getEncoder().encodeToString(encryptedToken);
    SesCleRecDto sesCleRecDto = new SesCleRecDto(encryptedTokenStr, sessionId.id());
    String serialSesClef = mapper.writeValueAsString(sesCleRecDto);

    // SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_CLE_REC,
        message.spdu().getHeader().getId(), false, version);

    final byte[] serialSduHash =
        cryptographyService.hash(SESSION, serialSesClef.getBytes(StandardCharsets.UTF_8));
    final String stamp = Base64.getEncoder()
        .encodeToString(
            cryptographyService.encryptSymmetric(SESSION, session.skak(), serialSduHash));
    Spdu spdu = new Spdu(spduHeader, stamp, serialSesClef);

    // SIDU
    SiduContext siduContext = new SiduContext(
        message.sidu().getContext().getDestinationCode(),
        message.sidu().getContext().getSourceCode(),
        message.sidu().getContext().getDestinationIri(),
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        message.sidu().getContext().getSourceIri(),
        false);
    Sidu sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));

    ExpandedSidu result = new ExpandedSidu(sidu, spdu, MsgType.SES_CLE_REC);
    return result;
  }
}
