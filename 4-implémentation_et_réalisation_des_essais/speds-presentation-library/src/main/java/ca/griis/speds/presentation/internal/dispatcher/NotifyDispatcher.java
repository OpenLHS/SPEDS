package ca.griis.speds.presentation.internal.dispatcher;

import static ca.griis.logger.statuscode.Error.IGNORED_ERROR;

import ca.griis.js2p.gen.speds.presentation.api.dto.Context23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.InterfaceDataUnit23Dto;
import ca.griis.js2p.gen.speds.presentation.api.dto.ServicePrimitive;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.handler.TransferIndicationHandler;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public final class NotifyDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(NotifyDispatcher.class);

  private final TransferIndicationHandler transferIndicationHandler;

  public NotifyDispatcher(CryptographyService service, SessionHost host,
      ConcurrentMap<PresentationTracking, TrackingInformation> serverTracking) {
    this.transferIndicationHandler = new TransferIndicationHandler(service, host, serverTracking);
  }

  public Optional<String> handle(InterfaceDataUnit23Dto idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Optional<String> preIdu = Optional.empty();

    try {
      final Context23Dto context = idu.getContext();

      if (!context.getService().equals("transfer")) {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(IGNORED_ERROR, ex);
      } else if (context.getServicePrimitive() == ServicePrimitive.INDICATION) {
        preIdu = transferIndicationHandler.handle(idu);
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(IGNORED_ERROR, ex);
      }
    } catch (JsonProcessingException e) {
      logger.error(IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "preIdu", preIdu);
    return preIdu;

  }
}
