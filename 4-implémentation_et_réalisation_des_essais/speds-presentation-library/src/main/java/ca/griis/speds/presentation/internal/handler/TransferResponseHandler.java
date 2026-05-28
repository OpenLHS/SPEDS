package ca.griis.speds.presentation.internal.handler;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.security.SecretKeyDestroyer;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import javax.crypto.SecretKey;

public final class TransferResponseHandler {
  private static final GriisLogger logger = getLogger(TransferResponseHandler.class);

  private final SessionHost host;
  private final ConcurrentMap<PresentationTracking, TrackingInformation> serverTracking;

  public TransferResponseHandler(SessionHost host,
      ConcurrentMap<PresentationTracking, TrackingInformation> serverTracking) {
    this.host = host;
    this.serverTracking = serverTracking;
  }

  public Optional<String> handle(InterfaceDataUnit12Dto idu) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Context12Dto ici = idu.getContext();
    Object options = ici.getOptions();

    String tn = "";
    if (options instanceof Map<?, ?> map) {
      tn = map.get("TN").toString();
    }
    UUID trackingNumber = UUID.fromString(tn);

    final TrackingInformation trackingInfo =
        serverTracking.remove(new PresentationTracking(trackingNumber));
    final UUID sessionTracking = trackingInfo.sessionTracking();

    final Map<String, Object> responseOptions = new HashMap<>();
    responseOptions.put("TN", sessionTracking);

    SecretKey secretKey = trackingInfo.sdek();
    byte[] keyBytes = secretKey.getEncoded();
    String serialKey = Base64.getEncoder().encodeToString(keyBytes);

    SecretKeyDestroyer.destroy(keyBytes);
    SecretKeyDestroyer.destroy(secretKey);

    Context23Dto context = new Context23Dto(
        ici.getPga(),
        ici.getSourceCode(),
        ici.getDestinationCode(),
        serialKey,
        "transfer",
        ServicePrimitive.RESPONSE,
        responseOptions);

    String sdu = idu.getMessage();
    InterfaceDataUnit23Dto sidu = new InterfaceDataUnit23Dto(context, sdu);
    String stringIdu = SharedObjectMapper.getInstance().getMapper().writeValueAsString(sidu);
    host.submitIdu(stringIdu);

    Optional<String> result = Optional.empty();
    return result;
  }
}
