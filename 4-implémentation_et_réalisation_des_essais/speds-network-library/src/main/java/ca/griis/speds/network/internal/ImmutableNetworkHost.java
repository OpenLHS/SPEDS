/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableNetworkHost.
 * @brief @~english ImmutableNetworkHost class implementation.
 */

package ca.griis.speds.network.internal;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.network.api.dto.InterfaceDataUnit56Dto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.NetworkHostEvent;
import ca.griis.speds.network.internal.dispatcher.NotifyDispatcher;
import ca.griis.speds.network.internal.dispatcher.SubmitDispatcher;
import ca.griis.speds.network.internal.identification.IdentifierGenerator;
import ca.griis.speds.network.internal.security.CertificatePrivateKeyPair;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Offre les services d'un hôte immutable de la couche réseau.
 * @par Details
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-02-18 [FO] - Refactorisation et simplification.
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableNetworkHost implements NetworkHost, HostEvent {
  private static final GriisLogger logger = getLogger(ImmutableNetworkHost.class);

  private final Host host;
  private final NetworkHostEvent networkEventConsumer;
  private final SubmitDispatcher submitDispatcher;
  private final NotifyDispatcher notifyDispatcher;
  private final ObjectMapper mapper;
  private final Cache<UUID, Boolean> indicatedMessages;
  private final CertificatePrivateKeyPair pair;

  public ImmutableNetworkHost(Host host, VersionDto version, Cache<UUID, Boolean> indicatedMessages,
      CertificatePrivateKeyPair pair, IdentifierGenerator generator, CryptographyService service,
      ObjectMapper mapper, NetworkHostEvent networkEventConsumer) {
    this.host = host;
    this.networkEventConsumer = networkEventConsumer;
    this.indicatedMessages = indicatedMessages;
    this.submitDispatcher =
        new SubmitDispatcher(generator, version, pair, service, mapper, host,
            indicatedMessages.asMap());
    this.notifyDispatcher = new NotifyDispatcher(service, mapper, host, indicatedMessages.asMap());
    this.mapper = mapper;
    this.pair = pair;
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    host.close();
    indicatedMessages.invalidateAll();
    indicatedMessages.cleanUp();
    pair.cleanUp();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public CompletableFuture<Optional<String>> submitIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    var result = submitDispatcher.handle(idu);
    var future = CompletableFuture.completedFuture(result);

    logger.trace(Trace.EXIT_METHOD_1, "future", future);
    return future;
  }

  @Override
  public void notifyIdu(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    try {
      final var didu = mapper.readValue(idu, InterfaceDataUnit56Dto.class);
      var result = notifyDispatcher.handle(didu);
      if (result.isPresent()) {
        networkEventConsumer.notifyIdu(result.get());
      } else {
        var ex =
            new RuntimeException("An IDU56 was received by the host, with no resulting IDU45.");
        logger.error(Error.IGNORED_ERROR, ex);
      }
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void notifyException(Exception exception) {
    networkEventConsumer.notifyException(exception);
  }
}
