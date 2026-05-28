/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SesFinRequestHandler.
 * @brief @~english Implementation of the SesFinRequestHandler class.
 */

package ca.griis.speds.session.internal.handler.request;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;
import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.fin.SesFinEnvDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
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
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

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
 * @brief @~french Gère l'envoi du message SES.FIN.ENV.
 * @par Détails
 *      <p>
 *      Gère l'envoi d'une terminaison de session par l'initiateur de la session.
 *      </>
 *      <p>
 *      Le jeton ici agit plutôt comme un identifiant servant à vérifier un traitement, et non comme
 *      un jeton d’autorisation d’accès à la session. La clé SKAK, quant à elle, permet
 *      d’authentifier le message.
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
 *      2026-04-28 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class SesFinRequestHandler {
  private static final GriisLogger logger = getLogger(SesFinRequestHandler.class);

  private final ObjectMapper sharedMapper;
  private final CryptographyService cryptographyService;
  private final VersionDto speds;
  private final Map<SessionId, SessionInformation> sessionInformations;

  public SesFinRequestHandler(HostStartupContext hostStartupContext) {
    logger.trace(Trace.ENTER_METHOD_1, "hostStartupContext", hostStartupContext);
    this.sessionInformations = hostStartupContext.sessions().asMap();
    this.sharedMapper = hostStartupContext.sharedMapper();
    this.speds = hostStartupContext.version();
    this.cryptographyService = hostStartupContext.cryptographyService();
  }

  public ExpandedSessionSidu handle(ExpandedSidu expandedSidu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "expandedSidu", expandedSidu);

    // Incrémenter le nombre de messages traités avec succès durant la session.
    final SesMsgRecDto sesMsgRecDto =
        sharedMapper.readValue((String) expandedSidu.spdu().getContent(), SesMsgRecDto.class);

    final SessionInformation sessionInfo =
        sessionInformations.get(new SessionId(sesMsgRecDto.getSession()));

    // Procéder à l’envoi de la fin de session
    // Créer SDU
    final UUID token = UUID.randomUUID();
    final Integer numberOfMessage = sessionInfo.numberOfMessage() + 1;
    final SessionInformation updatedInfo = SessionInformation.builder()
        .of(sessionInfo)
        .numberOfMessage(numberOfMessage)
        .token(token)
        .build();
    sessionInformations.put(sessionInfo.sessionId(), updatedInfo);

    final SessionInformation updatedSessionInfo =
        sessionInformations.get(new SessionId(sesMsgRecDto.getSession()));

    final SesFinEnvDto sdu =
        new SesFinEnvDto(token, updatedSessionInfo.numberOfMessage().toString(),
            updatedSessionInfo.sessionId().id());
    final String serialSdu = sharedMapper.writeValueAsString(sdu);

    // Construire la SPDU
    final SpduHeader spduHeader =
        new SpduHeader(HeaderDto.Msgtype.SES_FIN_ENV, UUID.randomUUID(), false, speds);
    final byte[] serialSduHash = cryptographyService.hash(SESSION, serialSdu.getBytes(
        StandardCharsets.UTF_8));
    final String stamp = Base64.getEncoder()
        .encodeToString(
            cryptographyService.encryptSymmetric(SESSION, updatedSessionInfo.skak(),
                serialSduHash));
    final Spdu spdu = new Spdu(spduHeader, stamp, serialSdu);

    // Construire la SIDU
    final SiduContext siduContext = new SiduContext(
        updatedSessionInfo.initiatorId(),
        updatedSessionInfo.peerId(),
        updatedSessionInfo.initiatorIri(),
        Context34Dto.Service.TRANSFER,
        ServicePrimitive.REQUEST,
        updatedSessionInfo.peerIri(),
        false);

    final Sidu sidu = new Sidu(siduContext, sharedMapper.writeValueAsString(spdu));
    final ExpandedSessionSidu result =
        new ExpandedSessionSidu(updatedSessionInfo.sessionId(),
            new ExpandedSidu(sidu, spdu, MsgType.SES_FIN_ENV));

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
