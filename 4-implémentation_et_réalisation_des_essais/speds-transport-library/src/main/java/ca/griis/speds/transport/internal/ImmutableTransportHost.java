package ca.griis.speds.transport.internal;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.transport.internal.dispatcher.SubmitDispatcher;
import ca.griis.speds.transport.internal.identification.IdentifierGenerator;
import ca.griis.speds.transport.internal.sync.ConfirmationRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
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
public final class ImmutableTransportHost implements TransportHost, NetworkHostEvent {
  private static final GriisLogger logger = getLogger(ImmutableTransportHost.class);

  private final NetworkHost networkHost;
  private final TransportHostEvent transportEventComsumer;
  private final SubmitDispatcher submitDispatcher;
  private final NotifyDispatcher notifyDispatcher;
  private final ObjectMapper mapper;
  private final Cache<UUID, Boolean> confirmedPendingMessagesId;
  private final Cache<UUID, Boolean> indicatedPendingMessagesId;
  private final ConfirmationRegistry confirmationRegistry;

  public ImmutableTransportHost(NetworkHost networkHost, Speds45Dto version,
      Cache<UUID, Boolean> confirmedPendingMessagesId,
      Cache<UUID, Boolean> indicatedPendingMessagesId,
      IdentifierGenerator identifierGenerator, CryptographyService service, ObjectMapper mapper,
      TransportHostEvent transportEventComsumer) {
    this.networkHost = networkHost;
    this.transportEventComsumer = transportEventComsumer;
    this.confirmedPendingMessagesId = confirmedPendingMessagesId;
    this.indicatedPendingMessagesId = indicatedPendingMessagesId;
    this.confirmationRegistry = new ConfirmationRegistry();
    this.mapper = mapper;

    this.submitDispatcher =
        new SubmitDispatcher(identifierGenerator, version, service, mapper, networkHost,
            confirmationRegistry, confirmedPendingMessagesId.asMap(),
            indicatedPendingMessagesId.asMap());
    this.notifyDispatcher = new NotifyDispatcher(service, mapper, networkHost, confirmationRegistry,
        confirmedPendingMessagesId.asMap(), indicatedPendingMessagesId.asMap());
  }

  @Override
  public CompletableFuture<Optional<String>> submitIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    var future = submitDispatcher.handle(idu);

    logger.trace(Trace.EXIT_METHOD_1, "future", future);
    return future;
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    networkHost.close();

    confirmedPendingMessagesId.invalidateAll();
    indicatedPendingMessagesId.invalidateAll();

    confirmedPendingMessagesId.cleanUp();
    indicatedPendingMessagesId.cleanUp();
    confirmationRegistry.cleanUp();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      final var nidu = mapper.readValue(idu, InterfaceDataUnit45Dto.class);
      var result = notifyDispatcher.handle(nidu);
      if (result.isPresent()) {
        transportEventComsumer.notifyIdu(result.get());
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyException(Exception exception) {
    transportEventComsumer.notifyException(exception);
  }
}
