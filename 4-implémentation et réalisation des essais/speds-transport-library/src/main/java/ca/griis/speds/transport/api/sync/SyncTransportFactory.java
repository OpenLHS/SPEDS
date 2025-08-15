/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableTransportFactory.
 * @brief @~english Implementation of the ImmutableTransportFactory class.
 */

package ca.griis.speds.transport.api.sync;

import ca.griis.js2p.gen.speds.transport.api.dto.InitInParamsDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.network.api.sync.SyncNetworkFactory;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.exception.ParameterException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.IdentifierGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import java.util.UUID;

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
 * @brief @~french Offre une fabrique d'entités nécessaires à la couche transport.
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
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class SyncTransportFactory implements TransportFactory {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SyncTransportFactory.class);

  private final SyncNetworkFactory networkFactory;
  private final IdentifierGenerator identifierGenerator;

  public SyncTransportFactory() {
    this(() -> UUID.randomUUID().toString());
    logger.trace(Trace.ENTER_METHOD_0);
  }

  public SyncTransportFactory(IdentifierGenerator identifierGenerator) {
    logger.trace(Trace.ENTER_METHOD_1, "identifierGenerator", identifierGenerator);
    this.networkFactory = new SyncNetworkFactory();
    this.identifierGenerator = identifierGenerator;
  }

  @Override
  public TransportHost init(String parameters) throws ParameterException {
    logger.trace(Trace.ENTER_METHOD_1, "parmeters", parameters);

    final ImmutableTransportHost traHost;
    try {
      final InitInParamsDto params =
          SharedObjectMapper.getInstance().getMapper().readValue(parameters, InitInParamsDto.class);

      final String spedsVersion = (String) params.getOptions().get("speds.tra.version");
      final String spedsReference = (String) params.getOptions().get("speds.tra.reference");

      if (spedsVersion == null) {
        throw new ParameterException(
            "SPEDS version is missing in the initialization parameters.");
      }

      if (spedsReference == null) {
        throw new ParameterException(
            "SPEDS reference is missing in the initialization parameters.");
      }

      final Integer pollerMaxQueueCapacity =
          (Integer) params.getOptions().getOrDefault("speds.tra.poller.maxQueueCapacity", 100);
      final Integer pollerNbThreads =
          (Integer) params.getOptions().getOrDefault("speds.tra.poller.nbThreads", 1);
      final Integer pollerSleepMs =
          (Integer) params.getOptions().getOrDefault("speds.tra.poller.sleepMs", 200);

      final NetworkHost host = Optional.ofNullable(initNetworkHost(parameters))
          .orElseThrow(() -> new ParameterException("Host is null."));
      traHost = new ImmutableTransportHost(host, spedsVersion, spedsReference, identifierGenerator,
          pollerMaxQueueCapacity, pollerNbThreads, pollerSleepMs);
    } catch (JsonProcessingException e) {
      throw new ParameterException("Cannot read initialization parameters" + e.getMessage());
    }

    logger.trace(Trace.EXIT_METHOD_1, "traHost", traHost);
    return traHost;
  }

  @Override
  public NetworkHost initNetworkHost(String parameters) throws ParameterException {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    final NetworkHost networkHost = networkFactory.initHost(parameters);

    logger.trace(Trace.EXIT_METHOD_1, "networkHost", networkHost);
    return networkHost;
  }
}
