
package ca.griis.speds.session.internal.handler.response;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;

import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.session.api.dto.Context34Dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.session.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.session.api.dto.msg.SesMsgRecDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.session.internal.contract.MsgType;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.contract.Sidu;
import ca.griis.speds.session.internal.contract.SiduContext;
import ca.griis.speds.session.internal.contract.Spdu;
import ca.griis.speds.session.internal.contract.SpduHeader;
import ca.griis.speds.session.internal.domain.ExpandedSidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.PendingMessage;
import ca.griis.speds.session.internal.domain.PendingResponse;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.transport.TransportHostAdapter;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

public final class SesTransferResponseHandler {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SesTransferResponseHandler.class);

  private final CryptographyService cryptographyService;
  private final VersionDto version;
  private final ObjectMapper mapper;
  private final TransportHostAdapter transportHost;
  private final Map<SessionId, SessionInformation> sessions;
  private final Map<UUID, PendingMessage> pendingResponses;

  public SesTransferResponseHandler(HostStartupContext hostStartupContext) {
    this.sessions = hostStartupContext.sessions().asMap();
    this.pendingResponses = hostStartupContext.pendingMessage().asMap();
    this.version = hostStartupContext.version();
    this.mapper = hostStartupContext.sharedMapper();
    this.cryptographyService = hostStartupContext.cryptographyService();
    this.transportHost = new TransportHostAdapter(hostStartupContext);
  }

  public void handle(Pidu pidu) {
    try {
      final var ctx = pidu.getContext();
      final String sdu = pidu.getMessage();
      final var options = mapper.convertValue(ctx.getOptions(),
          new TypeReference<Map<String, String>>() {});

      final String tn = options.get("TN");
      final UUID msgId = UUID.fromString(tn);

      final PendingMessage removedTn = pendingResponses.remove(msgId);
      if (sdu.startsWith("FAILED")) {
        logger.error(Error.IGNORED_ERROR, new RuntimeException(sdu));
      } else if (removedTn == null) {
        final String exMessage =
            "This tracking number from this transfer response does not match any received message ";
        logger.error(Error.IGNORED_ERROR, new RuntimeException(exMessage));
      } else {
        final var pendingResponse = (PendingResponse) removedTn;
        final SessionId sessionId = pendingResponse.sessionID();

        if (sessionId == null) {
          final String exMessage = "This session id is null.";
          logger.error(Error.IGNORED_ERROR, new RuntimeException(exMessage));
        } else if (sessions.containsKey(sessionId) == false) {
          final String exMessage =
              "This session id from this transfer response does not match any received message ";
          logger.error(Error.IGNORED_ERROR, new RuntimeException(exMessage));
        } else {
          final SessionInformation sessionInfo = sessions.get(sessionId);

          final var code = pidu.getContext().getSourceCode();
          if (sessionInfo.initiatorId().equals(code)) {
            // Créer la SPDU
            SpduHeader spduHeader =
                new SpduHeader(HeaderDto.Msgtype.SES_MSG_REC, msgId, options, version);

            final SesMsgRecDto msgRecDto = new SesMsgRecDto("ACK", sessionInfo.sessionId().id());
            final String msgRec = mapper.writeValueAsString(msgRecDto);
            final byte[] serialSduHash =
                cryptographyService.hash(SESSION, msgRec.getBytes(StandardCharsets.UTF_8));

            final String stamp = Base64.getEncoder()
                .encodeToString(
                    cryptographyService.encryptSymmetric(SESSION, sessionInfo.skak(),
                        serialSduHash));
            Spdu spdu = new Spdu(spduHeader, stamp, mapper.writeValueAsString(msgRecDto));

            SiduContext siduContext = new SiduContext(
                sessionInfo.peerId(),
                sessionInfo.initiatorId(),
                sessionInfo.peerIri(),
                Context34Dto.Service.TRANSFER,
                ServicePrimitive.REQUEST,
                sessionInfo.initiatorIri(),
                false);

            final var entityCode = sessionInfo.peerId();
            final var sidu = new Sidu(siduContext, mapper.writeValueAsString(spdu));
            final var expandedSidu = new ExpandedSidu(sidu, spdu, MsgType.SES_MSG_REC);
            transportHost.request(entityCode, sessionId, expandedSidu);
          } else {
            final String exMessage =
                "The source code from this response does not match "
                    + "the the initiator ID in the session";
            logger.error(Error.IGNORED_ERROR, new RuntimeException(exMessage));
          }
        }
      }
    } catch (JsonProcessingException ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    } catch (RuntimeException ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }
  }
}
