package ca.griis.speds.transport.internal.dispatcher;

import ca.griis.js2p.gen.speds.transport.api.dto.Context34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit34Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ServicePrimitive;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.internal.handler.TransferRequestHandler;
import ca.griis.speds.transport.internal.handler.TransferResponseHandler;
import ca.griis.speds.transport.internal.identification.IdentifierGenerator;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
public final class SubmitDispatcher {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SubmitDispatcher.class);

  private final TransferRequestHandler transferRequestHandler;
  private final TransferResponseHandler transferResponseHandler;
  private final ObjectMapper objectMapper;

  public SubmitDispatcher(IdentifierGenerator generator, Speds45Dto version,
      CryptographyService service, ObjectMapper objectMapper,
      NetworkHost networkHost, ConfirmationRegistry confirmationRegistry,
      Map<UUID, Boolean> confirmedPendingMessagesId,
      Map<UUID, Boolean> indicatedPendingMessagesId) {
    this.objectMapper = objectMapper;
    this.transferRequestHandler =
        new TransferRequestHandler(generator, version, objectMapper, networkHost,
            service, confirmedPendingMessagesId, confirmationRegistry);
    this.transferResponseHandler =
        new TransferResponseHandler(objectMapper, networkHost, indicatedPendingMessagesId, version,
            service);
  }

  public CompletableFuture<Optional<String>> handle(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    CompletableFuture<Optional<String>> tidu = CompletableFuture.completedFuture(Optional.empty());

    try {
      final var tsdu = objectMapper.readValue(idu, InterfaceDataUnit34Dto.class);
      final var context = tsdu.getContext();

      if (context.getService() != Context34Dto.Service.TRANSFER) {
        final var ex = new RuntimeException("FAILED: Unknown service");
        logger.error(Error.IGNORED_ERROR, ex);
      } else if (context.getServicePrimitive() == ServicePrimitive.REQUEST) {
        tidu = transferRequestHandler.handle(tsdu);
      } else if (context.getServicePrimitive() == ServicePrimitive.RESPONSE) {
        tidu = transferResponseHandler.handle(tsdu);
      } else {
        final var ex = new RuntimeException("FAILED: Unknown primitive service");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (Exception e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "tidu", tidu);
    return tidu;
  }
}
