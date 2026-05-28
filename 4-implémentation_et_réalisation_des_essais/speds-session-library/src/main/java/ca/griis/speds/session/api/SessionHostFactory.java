/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SessionHostFactory.
 * @brief @~english Implementation of the SessionHostFactory class.
 */

package ca.griis.speds.session.api;

import ca.griis.js2p.gen.speds.session.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.session.api.dto.OptionsDto;
import ca.griis.js2p.gen.speds.session.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.contract.IdentifierGenerator;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.session.internal.contract.Options;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.PendingMessage;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.event.SessionEventHandler;
import ca.griis.speds.session.internal.host.ImmutableSessionHost;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.session.internal.security.authorization.AuthorizationService;
import ca.griis.speds.session.internal.security.crypto.SessionSecurityService;
import ca.griis.speds.session.internal.serializer.SharedObjectMapper;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.TransportHostEvent;
import ca.griis.speds.transport.api.TransportHostFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * @brief @~french Offre une fabrique d'entités nécessaires à la couche session.
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
 *      2025-03-13 [FO] - Refactorisation poru SPEDS v7.
 *      2025-05-12 [CB] - Implémentation v2
 *      2025-03-17 [SSC] - Implémentation de la conception (init)
 *      2025-02-18 [MD] - Déplacé le paramètre PgaService vers constructeur </br>
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class SessionHostFactory implements SessionFactory {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SessionHostFactory.class);

  private final AuthorizationService projectService;
  private final ObjectMapper sharedMapper = SharedObjectMapper.getInstance().getMapper();
  private final ObjectMapper sharedMapper2 = new ObjectMapper();
  private final TransportFactory transportFactory;
  private final IdentifierGenerator identifierGenerator;
  private final CryptographyService cryptoService;
  private final SessionSecurityService securityService;

  public SessionHostFactory(ProjectService projectService, CryptographyService cryptoService) {
    this(projectService, cryptoService, () -> UUID.randomUUID().toString());
  }

  public SessionHostFactory(ProjectService projectService, CryptographyService cryptoService,
      IdentifierGenerator identifierGenerator) {
    logger.trace(Trace.ENTER_METHOD_3, "projectService", projectService, "identifierGenerator",
        identifierGenerator, "cryptoService", cryptoService);
    this.projectService = new AuthorizationService(projectService);
    this.identifierGenerator = identifierGenerator;
    this.cryptoService = cryptoService;
    this.transportFactory = new TransportHostFactory(cryptoService);
    this.securityService = new SessionSecurityService(cryptoService);
  }

  @Override
  public SessionHost initHost(String parameters, SessionHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_2, "parameters", parameters, "hostEventConsumer",
        hostEventConsumer);

    final var handler = new SessionEventHandler();
    final TransportHost host = initTransportHost(parameters, handler);
    final OptionsDto options = getOptions(parameters);

    final CertificatePrivateKeysEntry pair =
        securityService.getCertificatePrivateKey(options.getCertificate(), options.getPrivateKey());
    final var version = new VersionDto(options.getSpedsVersion(), options.getSpedsReference());

    Integer confirmWindowMinutes = 10;
    Integer responseWindowMinutes = 10;
    Integer sessionTimeoutMinutes = 10;
    Integer nbThreads = 8;
    Integer transportConfirmationTimeout = 8;

    try {
      final InitInParamsDto params =
          SharedObjectMapper.getInstance().getMapper()
              .readValue(parameters, InitInParamsDto.class);
      confirmWindowMinutes =
          (Integer) params.getOptions().getOrDefault("speds.ses.confirm.window.minutes", 10);
      responseWindowMinutes =
          (Integer) params.getOptions().getOrDefault("speds.ses.response.window.minutes", 10);
      sessionTimeoutMinutes =
          (Integer) params.getOptions().getOrDefault("speds.ses.timeout.minutes", 10);
      nbThreads =
          (Integer) params.getOptions().getOrDefault("speds.ses.threads", 8);
      transportConfirmationTimeout =
          (Integer) params.getOptions().getOrDefault("speds.ses.transport.confirm.timeout.seconds",
              60);
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    final ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
    final var initiatorSession = Caffeine.newBuilder()
        .expireAfterAccess(sessionTimeoutMinutes, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .removalListener(
            (RemovalListener<SessionId, SessionInformation>) (key, value, cause) -> {
              if (value != null) {
                value.cleanUp();
              }
            })
        .build();

    final var peerSession = Caffeine.newBuilder()
        .expireAfterAccess(sessionTimeoutMinutes, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .removalListener(
            (RemovalListener<SessionId, SessionInformation>) (key, value, cause) -> {
              if (value != null) {
                value.cleanUp();
              }
            })
        .build();

    final var pendingConfirmations = Caffeine.newBuilder()
        .expireAfterWrite(confirmWindowMinutes, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .<UUID, PendingMessage>build();

    final var pendingResponses = Caffeine.newBuilder()
        .expireAfterWrite(responseWindowMinutes, TimeUnit.MINUTES)
        .maximumSize(100_000)
        .<UUID, PendingMessage>build();

    final var clientContext =
        new HostStartupContext(
            host,
            sharedMapper,
            projectService,
            version,
            pair,
            identifierGenerator,
            cryptoService,
            initiatorSession,
            pendingConfirmations,
            hostEventConsumer,
            executor,
            transportConfirmationTimeout);

    final var serverContext =
        new HostStartupContext(
            host,
            sharedMapper2,
            projectService,
            version,
            pair,
            identifierGenerator,
            cryptoService,
            peerSession,
            pendingResponses,
            hostEventConsumer,
            executor,
            transportConfirmationTimeout);

    final var sessionHost =
        new ImmutableSessionHost(clientContext, serverContext, hostEventConsumer);
    handler.register(sessionHost);

    logger.trace(Trace.EXIT_METHOD_1, "sessionHost", sessionHost);
    return sessionHost;
  }

  @Override
  public TransportHost initTransportHost(String parameters, TransportHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_2, "parameters", parameters, "hostEventConsumer",
        hostEventConsumer);

    TransportHost traHost = transportFactory.initHost(parameters, hostEventConsumer);

    logger.trace(Trace.EXIT_METHOD_1, "transportHost", traHost);
    return traHost;
  }

  /**
   * @brief @~english «Description of the function»
   * @param parameters «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère les valeurs sérialisées pour initialiser la couche Session.
   * @param parameters Les paramètres sous la forme d'une chaîne de caractères.
   * @exception ParameterException Erreur soulevée lors de la récupération des paramètres
   *            d'initialisation
   * @return Les valeurs d'initialisation associées à la couche Session.
   *
   * @par Tâches
   *      S.O.
   */
  private Options getOptions(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    Options options;
    try {
      InitInParamsDto params = sharedMapper.readValue(parameters, new TypeReference<>() {});
      OptionsDto optionsDto = new OptionsDto(
          (String) params.getOptions().get("speds.ses.version"),
          (String) params.getOptions().get("speds.ses.reference"),
          (String) params.getOptions().get("speds.ses.cert"),
          (String) params.getOptions().get("speds.ses.private.key"));
      options = sharedMapper.convertValue(optionsDto, Options.class);
    } catch (JsonProcessingException e) {
      final String exception = "Cannot read initialization parameters: " + e.getMessage();
      throw new ParameterException(exception);
    }

    validateOptions(options);

    logger.trace(Trace.EXIT_METHOD_1, "options", options);
    return options;
  }

  /**
   * @brief @~english «Description of the function»
   * @param optionsDto «Parameter description»
   * @exception «exception name» «Exception description»
   *
   * @brief @~french Valide la présence des paramètres d'initialisation
   * @param optionsDto Les paramètres d'initialisation
   * @exception ParameterException Erreur soulevée lors de la récupération des paramètres
   *            d'initialisation
   *
   * @par Tâches
   *      S.O.
   */
  private void validateOptions(OptionsDto optionsDto) {
    logger.trace(Trace.ENTER_METHOD_1, "optionsDto", optionsDto);

    if (optionsDto.getSpedsVersion() == null) {
      throw new ParameterException("SPEDS version is missing in the initialization parameters.");
    }

    if (optionsDto.getSpedsReference() == null) {
      throw new ParameterException("SPEDS reference is missing in the initialization parameters.");
    }

    if (optionsDto.getCertificate() == null) {
      throw new ParameterException("Parameter speds.ses.cert is missing.");
    }

    if (optionsDto.getPrivateKey() == null) {
      throw new ParameterException("Parameter speds.ses.private.key is missing.");
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
