package ca.griis.speds.transport.internal.handler;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Context45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.checker.PduCheckerResult;
import ca.griis.speds.transport.internal.checker.TransportPduChecker;
import com.fasterxml.jackson.core.type.TypeReference;
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
public final class EnvPduTransferIndicationHandler {
  private static GriisLogger logger =
      GriisLoggerFactory.getLogger(EnvPduTransferIndicationHandler.class);

  private final ObjectMapper objectMapper;
  private final TransportPduChecker checker;
  private final NetworkHost networkHost;
  private final Map<UUID, Boolean> indicatedMessages;

  public EnvPduTransferIndicationHandler(CryptographyService service, ObjectMapper objectMapper,
      NetworkHost networkHost, Map<UUID, Boolean> indicatedMessages) {
    this.objectMapper = objectMapper;
    this.checker = new TransportPduChecker(service);
    this.networkHost = networkHost;
    this.indicatedMessages = indicatedMessages;
  }

  public Optional<String> handle(InterfaceDataUnit45Dto idu, ProtocolDataUnit4TraDto pdu) {
    Optional<String> result = Optional.empty();

    final var opts = objectMapper.convertValue(
        idu.getContext().getOptions(),
        new TypeReference<Map<String, String>>() {});

    final PduCheckerResult checkResult = checker.check(pdu.getHeader().getDestinationCode(), pdu);
    final Context45Dto ticiResp = new Context45Dto(
        idu.getContext().getSourceIri(),
        idu.getContext().getDestinationIri(),
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.RESPONSE,
        opts);

    try {
      if (checkResult.isValid()) {
        final InterfaceDataUnit45Dto tiduResp =
            new InterfaceDataUnit45Dto(ticiResp, checkResult.message());
        networkHost.submitIdu(objectMapper.writeValueAsString(tiduResp));

        final var headerDto = pdu.getHeader();
        final var msgId = pdu.getHeader().getId();
        final var tnKey = UUID.fromString(msgId.toString());

        final var options = Map.of("TN", tnKey);
        var ici =
            new Context34Dto(headerDto.getSourceCode(),
                headerDto.getDestinationCode(),
                idu.getContext().getSourceIri(),
                Context34Dto.Service.TRANSFER,
                ServicePrimitive.INDICATION,
                idu.getContext().getDestinationIri(),
                options);
        var nsdu = new InterfaceDataUnit34Dto(ici, pdu.getContent());

        indicatedMessages.putIfAbsent(tnKey, true);
        result = Optional.of(objectMapper.writeValueAsString(nsdu));
      } else {
        final InterfaceDataUnit45Dto tiduResp =
            new InterfaceDataUnit45Dto(ticiResp, checkResult.message());
        networkHost.submitIdu(objectMapper.writeValueAsString(tiduResp));
      }
    } catch (Exception ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    return result;
  }
}
