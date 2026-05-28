package ca.griis.speds.presentation.internal.handler;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ProtocolDataUnit2PreDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.internal.security.SecretKeyDestroyer;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;

public final class TransferRequestHandler {
  private static final GriisLogger logger = getLogger(TransferRequestHandler.class);
  private final SessionHost host;
  private final VersionDto version;
  private final CryptographyService service;

  public TransferRequestHandler(VersionDto version, CryptographyService service, SessionHost host) {
    this.version = version;
    this.host = host;
    this.service = service;
  }

  public Optional<String> handle(InterfaceDataUnit12Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> result = Optional.empty();

    try {
      String sidu = createRequest(idu);
      final var future = host.submitIdu(sidu);

      String sessionConfirm = future.get().get();

      InterfaceDataUnit23Dto confirmIdu =
          SharedObjectMapper.getInstance().getMapper().readValue(sessionConfirm,
              InterfaceDataUnit23Dto.class);

      String confirm = createConfirm(idu, confirmIdu.getMessage());
      result = Optional.of(confirm);
    } catch (Exception e) {
      logger.error(Error.IGNORED_ERROR, e);

      String failed = "FAILED: Error during transmission";
      String confirm = createConfirm(idu, failed);
      result = Optional.of(confirm);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  private String createRequest(InterfaceDataUnit12Dto aidu) throws JsonProcessingException {
    final Context12Dto upperIci = aidu.getContext();
    final String upperSdu = aidu.getMessage();

    SecretKey secretKey = service.generateSymmetricKey(SpedsConfigItemDto.SpedsLayer.PRESENTATION);
    byte[] encryptedMessageBytes =
        service.encryptSymmetric(SpedsConfigItemDto.SpedsLayer.PRESENTATION, secretKey,
            upperSdu.getBytes(StandardCharsets.UTF_8));

    final String cipheredSdu = Base64.getEncoder().encodeToString(encryptedMessageBytes);
    final UUID presentationId = UUID.randomUUID();
    final HeaderDto.Msgtype msgtype = HeaderDto.Msgtype.PRE_MSG_ENV;

    logger.info(Info.VARIABLE_LOGGING_5,
        "entityCode", upperIci.getSourceCode(),
        "msgId", presentationId,
        "msgType", msgtype,
        "dataEncrypted", true,
        "message", "Successful data encryption");

    byte[] keyBytes = secretKey.getEncoded();
    final String serialKey = Base64.getEncoder().encodeToString(keyBytes);

    SecretKeyDestroyer.destroy(keyBytes);
    SecretKeyDestroyer.destroy(secretKey);

    final Boolean options = Boolean.FALSE;
    final HeaderDto preMessageHeader =
        new HeaderDto(msgtype, presentationId, options, this.version);
    final ProtocolDataUnit2PreDto preMsgEnv =
        new ProtocolDataUnit2PreDto(preMessageHeader, cipheredSdu);

    final String serializePdu =
        SharedObjectMapper.getInstance().getMapper().writeValueAsString(preMsgEnv);

    Context23Dto context = new Context23Dto(
        upperIci.getPga(),
        upperIci.getSourceCode(),
        upperIci.getDestinationCode(),
        serialKey,
        "delegate",
        ServicePrimitive.REQUEST,
        false);

    final InterfaceDataUnit23Dto preIduDto = new InterfaceDataUnit23Dto(context, serializePdu);
    final String pidu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(preIduDto);

    return pidu;
  }

  private String createConfirm(InterfaceDataUnit12Dto idu, String message)
      throws JsonProcessingException {
    Context12Dto ici = new Context12Dto(idu.getContext().getPga(), idu.getContext().getSourceCode(),
        idu.getContext().getDestinationCode(), "delegate", ServicePrimitive.CONFIRM,
        idu.getContext().getOptions());

    InterfaceDataUnit12Dto confirmIdu = new InterfaceDataUnit12Dto(ici, message);
    String confirm = SharedObjectMapper.getInstance().getMapper().writeValueAsString(confirmIdu);
    return confirm;
  }
}
