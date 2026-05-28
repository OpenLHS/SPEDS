package ca.griis.speds.transport.internal.dispatcher;

import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.handler.EnvPduTransferIndicationHandler;
import ca.griis.speds.transport.internal.handler.RecPduTransferIndicationHandler;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * "Description brève du composant (classe, interface, ...)"
 *
 * <h3>Historique</h3>
 * <p>
 * XXXX-XX-XX [AS] - Implémentation initiale<br>
 * </p>
 *
 * <h3>Tâches</h3>
 * S.O.
 *
 * @author [AS] ameni.souid@usherbrooke.ca
 * @since
 */
public final class NotifyDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(NotifyDispatcher.class);

  private final EnvPduTransferIndicationHandler indicationEnvHandler;
  private final RecPduTransferIndicationHandler indicationRecHandler;
  private final ObjectMapper objectMapper;

  public NotifyDispatcher(CryptographyService service, ObjectMapper objectMapper,
      NetworkHost networkHost, ConfirmationRegistry registry,
      Map<UUID, Boolean> clientPendingMessagesId, Map<UUID, Boolean> serverPendingMessagesId) {

    this.indicationEnvHandler =
        new EnvPduTransferIndicationHandler(service, objectMapper, networkHost,
            serverPendingMessagesId);
    this.indicationRecHandler =
        new RecPduTransferIndicationHandler(service, objectMapper, networkHost,
            clientPendingMessagesId, registry);
    this.objectMapper = objectMapper;
  }

  public Optional<String> handle(InterfaceDataUnit45Dto idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> tidu = Optional.empty();
    try {
      final var context = idu.getContext();

      if (context.getService() != Context45Dto.Service.TRANSFER) {
        final var ex = new RuntimeException("FAILED: Unknown service");
        logger.error(Error.IGNORED_ERROR, ex);
      } else if (context.getServicePrimitive() == ServicePrimitive.INDICATION) {
        var pdu = objectMapper.readValue(idu.getMessage(), ProtocolDataUnit4TraDto.class);
        if (pdu.getHeader().getMsgtype() == Header45Dto.Msgtype.TRA_MSG_ENV) {
          tidu = indicationEnvHandler.handle(idu, pdu);
        } else if (pdu.getHeader().getMsgtype() == Header45Dto.Msgtype.TRA_MSG_REC) {
          tidu = indicationRecHandler.handle(idu, pdu);
        } else {
          final var ex = new RuntimeException("FAILED: msgType not supported");
          logger.error(Error.IGNORED_ERROR, ex);
        }
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "tidu", tidu);
    return tidu;
  }
}
