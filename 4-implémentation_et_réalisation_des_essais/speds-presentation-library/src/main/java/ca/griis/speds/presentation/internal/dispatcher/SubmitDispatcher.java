package ca.griis.speds.presentation.internal.dispatcher;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit12Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.handler.TransferRequestHandler;
import ca.griis.speds.presentation.internal.handler.TransferResponseHandler;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public final class SubmitDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SubmitDispatcher.class);

  private final TransferRequestHandler transferRequestHandler;
  private final TransferResponseHandler transferResponseHandler;

  public SubmitDispatcher(VersionDto version, CryptographyService service, SessionHost host,
      ConcurrentMap<PresentationTracking, TrackingInformation> serverTracking) {
    this.transferRequestHandler = new TransferRequestHandler(version, service, host);
    this.transferResponseHandler = new TransferResponseHandler(host, serverTracking);
  }

  public Optional<String> handle(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);
    Optional<String> nidu = Optional.empty();

    try {
      InterfaceDataUnit12Dto nsdu =
          SharedObjectMapper.getInstance().getMapper().readValue(idu, InterfaceDataUnit12Dto.class);
      Context12Dto context = nsdu.getContext();

      if (context.getServicePrimitive() == ServicePrimitive.REQUEST) {
        nidu = transferRequestHandler.handle(nsdu);
      } else if (context.getServicePrimitive() == ServicePrimitive.RESPONSE) {
        nidu = transferResponseHandler.handle(nsdu);
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (Exception e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "nidu", nidu);
    return nidu;
  }
}
