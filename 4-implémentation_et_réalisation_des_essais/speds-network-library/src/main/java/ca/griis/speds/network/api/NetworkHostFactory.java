/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe NetworkHostFactory.
 * @brief @~english NetworkHostFactory class implementation.
 */

package ca.griis.speds.network.api;

import static ca.griis.logger.GriisLoggerFactory.getLogger;
import static ca.griis.speds.network.internal.security.CertificatePrivateKeyPair.importFromPem;

import ca.griis.js2p.gen.speds.network.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.network.api.dto.OptionsDto;
import ca.griis.js2p.gen.speds.network.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.api.HostFactory;
import ca.griis.speds.link.api.factory.ImmutableDataLinkFactory;
import ca.griis.speds.network.api.exception.ParameterException;
import ca.griis.speds.network.internal.ImmutableNetworkHost;
import ca.griis.speds.network.internal.event.NetworkEventHandler;
import ca.griis.speds.network.internal.identification.IdentifierGenerator;
import ca.griis.speds.network.internal.serialization.SharedObjectMapper;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
 * @brief @~french Offre une fabrique pour construire les entités de la couche réseau.
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
public class NetworkHostFactory implements NetworkFactory {
  private static final GriisLogger logger = getLogger(NetworkHostFactory.class);

  private final IdentifierGenerator idGenerator;
  private final ObjectMapper mapper;
  private final HostFactory factory;
  private final CryptographyService cryptographyService;

  public NetworkHostFactory(CryptographyService cryptographyService) {
    this(() -> UUID.randomUUID(), cryptographyService);
  }

  public NetworkHostFactory(IdentifierGenerator identifierGenerator,
      CryptographyService cryptographyService) {
    logger.trace(Trace.ENTER_METHOD_1, "identifierGenerator", identifierGenerator);

    this.idGenerator = identifierGenerator;
    this.factory = new ImmutableDataLinkFactory();
    this.mapper = SharedObjectMapper.getInstance().getMapper();
    this.cryptographyService = cryptographyService;
  }

  @Override
  public NetworkHost initHost(String parameters, NetworkHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_2, "parameters", parameters, "hostEventConsumer",
        hostEventConsumer);

    final var params = getInitParams(parameters);
    final var spedsVersion = params.getSpedsVersion();
    final var spedsReference = params.getSpedsReference();
    final var certificatePem = params.getCertificate();
    final var privateKeyPem = params.getPrivateKey();
    final var responseWindowsMin = params.getResponseWindowMinutes();

    final var handler = new NetworkEventHandler();
    final var linkHost = initDataLinkHost(parameters, handler);
    final var keyPair = importFromPem(certificatePem, privateKeyPem);
    final var version = new VersionDto(spedsVersion, spedsReference);

    final var indicatedMessages = (Cache<UUID, Boolean>) Caffeine.newBuilder()
        .expireAfterWrite(responseWindowsMin, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .<UUID, Boolean>build();

    final var host =
        new ImmutableNetworkHost(linkHost, version, indicatedMessages, keyPair, idGenerator,
            cryptographyService, mapper, hostEventConsumer);

    handler.register(host);

    logger.trace(Trace.EXIT_METHOD_1, "host", host);
    return host;
  }

  @Override
  public Host initDataLinkHost(String parameters, HostEvent hostEventConsumer) {
    return factory.init(parameters, hostEventConsumer);
  }

  /**
   * @brief @~english «Description of the function»
   * @param parameters «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Récupère les valeurs sérialisées pour initialiser la couche Réseau.
   * @param parameters Les paramètres sous la forme d'une chaîne de caractères.
   * @return Les valeurs d'initialisation associées à la couche Réseau.
   *
   * @par Tâches
   *      S.O.
   */
  private OptionsDto getInitParams(String parameters) {
    final OptionsDto optionsDto;
    try {
      InitInParamsDto params = mapper.readValue(parameters, new TypeReference<>() {});
      optionsDto = mapper.convertValue(params.getOptions(), OptionsDto.class);
    } catch (JsonProcessingException e) {
      final String exception = "Cannot read initialization parameters: " + e.getMessage();
      throw new ParameterException(exception);
    }

    logger.trace(Trace.EXIT_METHOD_1, "optionsDto", optionsDto);
    return optionsDto;
  }
}
