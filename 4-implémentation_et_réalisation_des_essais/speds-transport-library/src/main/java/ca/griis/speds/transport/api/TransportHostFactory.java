package ca.griis.speds.transport.api;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.transport.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.transport.api.dto.Speds45Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkFactory;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.network.api.NetworkHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.transport.api.exception.ParameterException;
import ca.griis.speds.transport.internal.ImmutableTransportHost;
import ca.griis.speds.transport.internal.event.TransportEventHandler;
import ca.griis.speds.transport.internal.identification.IdentifierGenerator;
import ca.griis.speds.transport.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
public class TransportHostFactory implements TransportFactory {
  private static final GriisLogger logger = getLogger(TransportHostFactory.class);

  private final IdentifierGenerator idGenerator;
  private final ObjectMapper mapper;
  private final NetworkFactory networkFactory;
  private final CryptographyService cryptographyService;

  public TransportHostFactory(CryptographyService cryptographyService) {
    this(() -> UUID.randomUUID(), cryptographyService);
    logger.trace(Trace.ENTER_METHOD_0);
  }

  public TransportHostFactory(IdentifierGenerator identifierGenerator,
      CryptographyService cryptographyService) {
    logger.trace(Trace.ENTER_METHOD_1, "identifierGenerator", identifierGenerator);
    this.networkFactory = new NetworkHostFactory(cryptographyService);
    this.idGenerator = identifierGenerator;
    this.mapper = SharedObjectMapper.getInstance().getMapper();
    this.cryptographyService = cryptographyService;
  }

  @Override
  public TransportHost initHost(String parameters, TransportHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_1, "parmeters", parameters);

    final ImmutableTransportHost transportHost;
    try {
      final InitInParamsDto params =
          SharedObjectMapper.getInstance().getMapper()
              .readValue(parameters, InitInParamsDto.class);

      final String spedsVersion = (String) params.getOptions().get("speds.tra.version");
      final String spedsReference = (String) params.getOptions().get("speds.tra.reference");
      final Integer confirmWindowMinutes =
          (Integer) params.getOptions().get("speds.tra.confirm.window.minutes");
      final Integer responseWindowMinutes =
          (Integer) params.getOptions().get("speds.tra.response.window.minutes");

      if (spedsVersion == null || spedsReference == null || confirmWindowMinutes == null
          || responseWindowMinutes == null) {
        throw new ParameterException(
            "SPEDS parameter is missing in the initialization parameters.");
      }

      final var handler = new TransportEventHandler();
      final var networkHost = initNetworkHost(parameters, handler);

      // speds.tra.confirm.window.minutes
      final var confirmedMessages = (Cache<UUID, Boolean>) Caffeine.newBuilder()
          .expireAfterWrite(confirmWindowMinutes, TimeUnit.MINUTES)
          .maximumSize(100_000)
          .<UUID, Boolean>build();

      // speds.tra.response.window.minutes

      final var indicatedMessages = (Cache<UUID, Boolean>) Caffeine.newBuilder()
          .expireAfterWrite(responseWindowMinutes, TimeUnit.MINUTES)
          .maximumSize(100_000)
          .<UUID, Boolean>build();

      final Speds45Dto dto = new Speds45Dto(spedsVersion, spedsReference);
      transportHost =
          new ImmutableTransportHost(networkHost, dto, confirmedMessages,
              indicatedMessages, idGenerator, cryptographyService, mapper, hostEventConsumer);

      handler.register(transportHost);

    } catch (JsonProcessingException e) {
      throw new ParameterException("Cannot read initialization parameters.");
    }

    logger.trace(Trace.EXIT_METHOD_1, "transportHost", transportHost);
    return transportHost;
  }

  @Override
  public NetworkHost initNetworkHost(String parameters, NetworkHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_2, "parameters", parameters, "hostEventConsumer",
        hostEventConsumer);

    final NetworkHost networkHost = networkFactory.initHost(parameters, hostEventConsumer);

    logger.trace(Trace.EXIT_METHOD_1, "networkHost", networkHost);
    return networkHost;
  }
}
