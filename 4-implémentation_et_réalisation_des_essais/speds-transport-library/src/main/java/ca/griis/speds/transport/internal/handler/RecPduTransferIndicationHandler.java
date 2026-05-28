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
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.checker.PduCheckerResult;
import ca.griis.speds.transport.internal.checker.TransportPduChecker;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public final class RecPduTransferIndicationHandler {
  private static GriisLogger logger =
      GriisLoggerFactory.getLogger(RecPduTransferIndicationHandler.class);

  private final ObjectMapper objectMapper;
  private final TransportPduChecker checker;
  private final NetworkHost networkHost;
  private final Map<UUID, Boolean> indicatedMessages;
  private final ConfirmationRegistry confirmationRegistry;

  public RecPduTransferIndicationHandler(CryptographyService service, ObjectMapper objectMapper,
      NetworkHost networkHost, Map<UUID, Boolean> indicatedMessages,
      ConfirmationRegistry confirmationRegistry) {
    this.objectMapper = objectMapper;
    this.checker = new TransportPduChecker(service);
    this.networkHost = networkHost;
    this.indicatedMessages = indicatedMessages;
    this.confirmationRegistry = confirmationRegistry;
  }

  public Optional<String> handle(InterfaceDataUnit45Dto idu, ProtocolDataUnit4TraDto pdu) {
    Optional<String> result = Optional.empty();

    final var msgId = pdu.getHeader().getId();
    final var id = UUID.fromString(msgId.toString());
    final var opts = objectMapper.convertValue(
        idu.getContext().getOptions(),
        new TypeReference<Map<String, String>>() {});
    final Context45Dto ticiResp = new Context45Dto(
        idu.getContext().getSourceIri(),
        idu.getContext().getDestinationIri(),
        Context45Dto.Service.TRANSFER,
        ServicePrimitive.RESPONSE,
        opts);

    var isRequestedMsg = indicatedMessages.remove(id);

    try {
      if (isRequestedMsg == null) {
        confirmationRegistry.remove(id);

        final var failed = "FAILED: Unknown message id";

        logger.info(Info.VARIABLE_LOGGING_5,
            "entityCode", pdu.getHeader().getSourceCode(),
            "msgId", pdu.getHeader().getId(),
            "msgType", pdu.getHeader().getMsgtype(),
            "check", false,
            "message", failed);

        final InterfaceDataUnit45Dto tiduResp = new InterfaceDataUnit45Dto(ticiResp, failed);
        networkHost.submitIdu(objectMapper.writeValueAsString(tiduResp));
      } else {
        PduCheckerResult checkResult = checker.check(pdu.getHeader().getSourceCode(), pdu);

        if (checkResult.isValid()) {
          final InterfaceDataUnit45Dto tiduResp =
              new InterfaceDataUnit45Dto(ticiResp, checkResult.message());
          networkHost.submitIdu(objectMapper.writeValueAsString(tiduResp));

          Context34Dto dto = new Context34Dto(
              pdu.getHeader().getSourceCode(),
              pdu.getHeader().getDestinationCode(),
              idu.getContext().getSourceIri(),
              Context34Dto.Service.TRANSFER,
              ServicePrimitive.CONFIRM,
              idu.getContext().getDestinationIri(),
              Boolean.FALSE);

          InterfaceDataUnit34Dto confirm34 = new InterfaceDataUnit34Dto(dto, "SUCCEED");
          String confirm34Json = objectMapper.writeValueAsString(confirm34);

          confirmationRegistry.confirm(id, confirm34Json);
        } else {
          final InterfaceDataUnit45Dto tiduResp =
              new InterfaceDataUnit45Dto(ticiResp, checkResult.message());
          networkHost.submitIdu(objectMapper.writeValueAsString(tiduResp));
        }
      }
    } catch (Exception ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    return result;
  }
}
