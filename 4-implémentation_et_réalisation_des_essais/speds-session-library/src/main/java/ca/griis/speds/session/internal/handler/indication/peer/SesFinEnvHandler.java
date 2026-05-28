/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesFinEnvHandler.
 * @brief @~english Implementation of the SesFinEnvHandler class.
 */

package ca.griis.speds.session.internal.handler.indication.peer;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.SessionHostEvent;
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
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
 * @brief @~french Gère la réception du message SES.FIN.ENV.
 * @par Détails
 *      <p>
 *      Gère la réception d'une terminaison de session par l'initiateur de la session.
 *      </>
 *      <p>
 *      Aucun jeton n’est requis, car celui utilisé au niveau de la couche session agit plutôt comme
 *      un identifiant servant à vérifier un traitement, et non comme un jeton d’autorisation
 *      d’accès à la session. La clé SKAK, quant à elle, permet d’authentifier le message.
 *      </>
 * @par Modèle
 *      S.O.
 * @par Conception
 *      <p>
 *      L’initiateur de la session est considéré comme le contrôleur de celle-ci. Par conséquent, il
 *      est attendu que la terminaison de la session soit imposée au partenaire par cet initiateur.
 *      </>
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-06-29 [MD] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SesFinEnvHandler implements MessageHandler {
  private static final GriisLogger logger = getLogger(SesFinEnvHandler.class);

  private final CryptographyService cryptographyService;
  private final ObjectMapper mapper;
  private final VersionDto version;
  private final TransportHostAdapter transportHost;
  private final Map<SessionId, SessionInformation> sessions;
  private final SessionSecurityService securityService;
  private final SessionHostEvent sessionHostEvent;

  public SesFinEnvHandler(HostStartupContext ctx) {
    this.cryptographyService = ctx.cryptographyService();
    this.transportHost = new TransportHostAdapter(ctx);
    this.mapper = ctx.sharedMapper();
    this.version = ctx.version();
    this.sessions = ctx.sessions().asMap();
    this.securityService = new SessionSecurityService(ctx.cryptographyService());
    this.sessionHostEvent = ctx.hostEventConsumer();
  }

  @Override
  public MsgType getHandledType() {
    return MsgType.SES_FIN_ENV;
  }

  @Override
  public Optional<String> handle(ExpandedSidu message) {
    logger.trace(Trace.ENTER_METHOD_1, "message", message);

    final var entityCode = message.sidu().getContext().getDestinationCode();
    SessionId sessionId = null;

    try {
      SesFinEnvDto sesFinEnvDto =
          mapper.readValue((String) message.spdu().getContent(), SesFinEnvDto.class);
      sessionId = new SessionId(sesFinEnvDto.getSession());
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
            transportHost.response(entityCode, sessionId, message, "SUCCEED");

            SesFinRecDto sesFinRecDto = new SesFinRecDto(sesFinEnvDto.getToken(), sessionId.id());
            String serialSdu = mapper.writeValueAsString(sesFinRecDto);

            // SPDU
            SpduHeader spduHeader = new SpduHeader(HeaderDto.Msgtype.SES_FIN_REC,
                message.spdu().getHeader().getId(), false, version);

            final byte[] serialSduHash = cryptographyService.hash(SESSION, serialSdu.getBytes(
                StandardCharsets.UTF_8));
            final String stamp = Base64.getEncoder()
                .encodeToString(
                    cryptographyService.encryptSymmetric(SESSION, sessionInfo.skak(),
                        serialSduHash));
            sessions.remove(sessionInfo.sessionId());

            Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);
            // SIDU
            SiduContext siduContext = new SiduContext(
                message.sidu().getContext().getDestinationCode(),
                message.sidu().getContext().getSourceCode(),
                message.sidu().getContext().getDestinationIri(),
                Context34Dto.Service.TRANSFER,
                ServicePrimitive.REQUEST,
                message.sidu().getContext().getSourceIri(),
                false);

            sessionHostEvent.notifyPeerSessionTerminatedSuccessfully(sessionInfo.sessionId());

            var sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));
            var expSidu = new ExpandedSidu(sidu, spdu, MsgType.SES_FIN_REC);
            transportHost.request(entityCode, sessionId, expSidu);
          } else {
            transportHost.response(entityCode, sessionId, message,
                "FAILED: AuthenticateException");
          }
        } else {
          transportHost.response(entityCode, sessionId, message,
              "FAILED: The source code does not match the initiator ID");
        }
      } else {
        transportHost.response(entityCode, sessionId, message,
            "FAILED: The code from the IDU does not match the initiator ID");
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
}
