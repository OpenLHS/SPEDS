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

package ca.griis.speds.session.internal.handler.indication.peer;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.sak.SesSakRecDto;
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
import ca.griis.speds.session.internal.domain.SkakWithPubKey;
import ca.griis.speds.session.internal.handler.indication.MessageHandler;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
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

  private final ObjectMapper mapper;
  private final VersionDto version;
  private final TransportHostAdapter transportHost;
  private final CertificatePrivateKeysEntry hostCertKey;
  private final Map<SessionId, SessionInformation> sessions;
  private final CryptographyService cryptographyService;
  private final SessionSecurityService securityService;

  public SesSakEnvHandler(HostStartupContext ctx) {
    this.transportHost = new TransportHostAdapter(ctx);
    this.mapper = ctx.sharedMapper();
    this.version = ctx.version();
    this.sessions = ctx.sessions().asMap();
    this.hostCertKey = ctx.hostKeys();
    this.cryptographyService = ctx.cryptographyService();
    this.securityService = new SessionSecurityService(ctx.cryptographyService());
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_SAK_ENV;
  }

  @Override
  public Optional<String> handle(ExpandedSidu message) {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    final var enityCode = message.sidu().getContext().getDestinationCode();
    SessionId sessionId = null;

    try {
      final SesSakEnvDto sesSakEnvDto =
          mapper.readValue((String) message.spdu().getContent(), SesSakEnvDto.class);
      final UUID sessionUuid = UUID.fromString(sesSakEnvDto.getSession());
      sessionId = new SessionId(sessionUuid);
      SessionInformation sessionInfo = sessions.get(sessionId);

      if (sessionInfo != null) {
        final var code = message.sidu().getContext().getSourceCode();

        if (sessionInfo.initiatorId().equals(code)) {
          var data = ((String) message.spdu().getContent()).getBytes(StandardCharsets.UTF_8);
          Boolean verifyStamp =
              cryptographyService.checkSignatureValidity(
                  SESSION,
                  Base64.getDecoder().decode(message.spdu().getStamp()),
                  sessionInfo.initiatorPubKey(),
                  data);

          if (verifyStamp) {
            transportHost.response(enityCode, sessionId, message, "SUCCEED");

            // Traiter le contenu
            // Création de Skak
            SkakWithPubKey skakWithPubKey = createSkak(sesSakEnvDto, sessionInfo);

            // Transmettre la réponse à la couche inférieur
            var sidu = buildSidu(message, skakWithPubKey, sessionId);
            transportHost.request(enityCode, sessionId, sidu);
          } else {
            sessions.remove(sessionId);

            transportHost.response(enityCode, sessionId, message,
                "FAILED: AuthenticateException");
          }
        } else {
          transportHost.response(enityCode, sessionId, message,
              "FAILED: The code from the IDU does not match the initiator ID");
        }
      } else {
        transportHost.response(enityCode, sessionId, message,
            "FAILED: UnknownSessionException");
      }
    } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
      logger.error(Error.IGNORED_ERROR, e);

      transportHost.response(enityCode, sessionId, message,
          "FAILED: Error during the processing of the message");
    }

    Optional<String> result = Optional.empty();
    logger.trace(Trace.ENTER_METHOD_1, "result", result);
    return result;
  }

  private ExpandedSidu buildSidu(ExpandedSidu message, SkakWithPubKey skakWithPubKey,
      SessionId sessionId)
      throws JsonProcessingException {
    // Créer une SDU SES.SAK.REC
    String serialChoice = Base64.getEncoder()
        .encodeToString(skakWithPubKey.choice().getPublic().getEncoded());
    SesSakRecDto sdu = new SesSakRecDto(serialChoice, sessionId.id());

    // Construire la SPDU
    SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_SAK_REC,
        message.spdu().getHeader().getId(), false, version);
    final String serialSdu = mapper.writeValueAsString(sdu);
    final String stamp = Base64.getEncoder().encodeToString(
        cryptographyService.sign(SESSION, hostCertKey.getPrivateKey(), serialSdu.getBytes(
            StandardCharsets.UTF_8)));
    Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);

    // Création SIDU
    SiduContext siduContext = new SiduContext(
        message.sidu().getContext().getDestinationCode(),
        message.sidu().getContext().getSourceCode(),
        message.sidu().getContext().getDestinationIri(),
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        message.sidu().getContext().getSourceIri(),
        false);

    Sidu sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));
    ExpandedSidu result = new ExpandedSidu(sidu, spdu, MsgType.SES_SAK_REC);
    return result;
  }

  private SkakWithPubKey createSkak(SesSakEnvDto sesSakEnvDto, SessionInformation sessionInfo)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PublicKey initiatorPubChoice =
        securityService.convertToDhPublicKey(sesSakEnvDto.getValue());
    KeyPair peerChoice = cryptographyService.chooseDiffieHellmanValue(SESSION);
    SecretKey skak =
        cryptographyService.getDiffieHellmanSecretKey(SESSION, peerChoice, initiatorPubChoice);

    final SessionInformation updatedInfo = SessionInformation.builder()
        .of(sessionInfo)
        .skak(skak)
        .build();
    sessions.put(sessionInfo.sessionId(), updatedInfo);

    SkakWithPubKey skakWithPubKey = new SkakWithPubKey(skak, peerChoice);
    return skakWithPubKey;
  }
}
