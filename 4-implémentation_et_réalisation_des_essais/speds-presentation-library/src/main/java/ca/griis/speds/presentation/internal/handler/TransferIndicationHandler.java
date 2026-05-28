package ca.griis.speds.presentation.internal.handler;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.HeaderDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ProtocolDataUnit2PreDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto;
import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.security.SecretKeyDestroyer;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class TransferIndicationHandler {
  private static final GriisLogger logger = getLogger(TransferRequestHandler.class);
  private final SessionHost host;
  private final CryptographyService service;
  private final ConcurrentMap<PresentationTracking, TrackingInformation> serverTracking;

  public TransferIndicationHandler(CryptographyService service, SessionHost host,
      ConcurrentMap<PresentationTracking, TrackingInformation> serverTracking) {
    this.service = service;
    this.host = host;
    this.serverTracking = serverTracking;
  }

  public Optional<String> handle(InterfaceDataUnit23Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> result = Optional.empty();

    final Context23Dto preIci = idu.getContext();
    final String preSdu = idu.getMessage();

    SecretKey key = null;
    UUID msgId = null;
    HeaderDto.Msgtype msgType = null;

    try {
      ProtocolDataUnit2PreDto pdu = SharedObjectMapper.getInstance().getMapper().readValue(preSdu,
          ProtocolDataUnit2PreDto.class);

      msgId = pdu.getHeader().getId();
      msgType = pdu.getHeader().getMsgtype();
      Map<String, Object> upperOptions = new HashMap<>();
      upperOptions.put("TN", pdu.getHeader().getId());

      final Context12Dto upperContext = new Context12Dto(
          preIci.getPga(),
          preIci.getSourceCode(),
          preIci.getDestinationCode(),
          "transfer",
          ServicePrimitive.INDICATION,
          upperOptions);

      String sdekSerial = idu.getContext().getSdek();
      byte[] keyBytes = Base64.getDecoder().decode(sdekSerial);
      String algo =
          service.getAlgorithm(SpedsConfigItemDto.SpedsLayer.PRESENTATION, AlgorithmCategory.SYMM);
      if (algo.contains("AES")) {
        algo = "AES";
      } else {
        SecretKeyDestroyer.destroy(keyBytes);

        throw new RuntimeException("Unsupported algo for secret key creation");
      }

      key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algo);
      SecretKeyDestroyer.destroy(keyBytes);

      final byte[] decryptedBytes =
          service.decryptSymmetric(SpedsConfigItemDto.SpedsLayer.PRESENTATION, key,
              Base64.getDecoder().decode(pdu.getContent()));

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", preIci.getDestinationCode(),
          "msgId", msgId,
          "msgType", msgType,
          "dataDecrypted", true,
          "message", "Successful data decryption");

      /**
       * @note [JM] -L'utilisation des option pour avoir un numéro de suivi est utilisé afin de
       *       gérer les messages et le SDEK
       */
      Object options = preIci.getOptions();
      String tn = "";
      if (options instanceof Map<?, ?> map) {
        tn = map.get("TN").toString();
      }

      UUID sessionTrackingNumber = UUID.fromString(tn);
      serverTracking.put(
          new PresentationTracking(pdu.getHeader().getId()),
          new TrackingInformation(sessionTrackingNumber, key));

      String decipheredSdu = new String(decryptedBytes, StandardCharsets.UTF_8);
      InterfaceDataUnit12Dto aidu = new InterfaceDataUnit12Dto(upperContext, decipheredSdu);
      String indication = SharedObjectMapper.getInstance().getMapper().writeValueAsString(aidu);
      result = Optional.of(indication);
    } catch (RuntimeException | JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);

      SecretKeyDestroyer.destroy(key);

      logger.info(Info.VARIABLE_LOGGING_5,
          "entityCode", preIci.getDestinationCode(),
          "msgId", msgId,
          "msgType", msgType,
          "dataDecrypted", true,
          "message", "Data decryption failed");

      String failed = "FAILED: Error during transmission";
      Context23Dto context = new Context23Dto(
          preIci.getPga(),
          preIci.getSourceCode(),
          preIci.getDestinationCode(),
          "transfer",
          ServicePrimitive.RESPONSE,
          preIci.getOptions());

      InterfaceDataUnit23Dto siduDto = new InterfaceDataUnit23Dto(context, failed);
      String sidu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(siduDto);
      host.submitIdu(sidu);
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }
}
