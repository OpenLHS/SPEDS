/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SyncSessionFactory.
 * @brief @~english Implementation of the SyncSessionFactory class.
 */

package ca.griis.speds.session.api.sync;

import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.js2p.gen.speds.session.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.session.api.dto.OptionsDto;
import ca.griis.js2p.gen.speds.session.api.dto.SPEDSDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionFactory;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.contract.IdentifierGenerator;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.session.internal.contract.Options;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.service.serializer.SharedObjectMapper;
import ca.griis.speds.session.internal.util.KeyMapping;
import ca.griis.speds.transport.api.TransportFactory;
import ca.griis.speds.transport.api.TransportHost;
import ca.griis.speds.transport.api.sync.SyncTransportFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
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
 *      2025-05-12 [CB] - Implémentation v2
 *      2025-03-17 [SSC] - Implémentation de la conception (init)
 *      2025-02-18 [MD] - Déplacé le paramètre PgaService vers constructeur </br>
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class SyncSessionFactory implements SessionFactory {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SyncSessionFactory.class);

  private final PgaService pgaService;
  private final ObjectMapper sharedMapper = SharedObjectMapper.getInstance().getMapper();
  private final ObjectMapper sharedMapper2 = new ObjectMapper();
  private final TransportFactory transportFactory;
  private final IdentifierGenerator identifierGenerator;

  public SyncSessionFactory(PgaService pgaService) {
    this(pgaService, () -> UUID.randomUUID().toString());
  }

  public SyncSessionFactory(PgaService pgaService, IdentifierGenerator identifierGenerator) {
    logger.trace(Trace.ENTER_METHOD_2, "pgaService", pgaService, "identifierGenerator",
        identifierGenerator);
    this.pgaService = pgaService;
    this.identifierGenerator = identifierGenerator;
    this.transportFactory = new SyncTransportFactory();
  }

  @Override
  public SessionHost init(String parameters) throws ParameterException {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    TransportHost host = initTransportHost(parameters);
    OptionsDto options = getOptions(parameters);

    CertificatePrivateKeysEntry hostSecrets =
        KeyMapping.getCertificatePrivateKey(options.getCertificate(),
            options.getPrivateKey());

    // todo md 2025-07-02 - Le mettre en paramètres de l'application
    Duration serverPollingInterval = Duration.ofMillis(200);
    Duration responseTimeout = Duration.ofSeconds(300);
    SPEDSDto speds = new SPEDSDto(options.getSpedsVersion(), options.getSpedsReference());
    // todo md 2025-07-02 - J'ai splitter en deux pour le sharedMapper.
    // Idéalement 1 seul HostContext
    HostStartupContext hostStartupContext = new HostStartupContext(host, sharedMapper, pgaService,
        speds, hostSecrets, identifierGenerator, serverPollingInterval, responseTimeout);

    HostStartupContext hostStartupContext2 = new HostStartupContext(host, sharedMapper2, pgaService,
        speds, hostSecrets, identifierGenerator, serverPollingInterval, responseTimeout);
    SessionHost sessionHost = new ImmutableSessionHost(hostStartupContext, hostStartupContext2);

    logger.trace(Trace.EXIT_METHOD_1, "sessionHost", sessionHost);
    return sessionHost;
  }


  @Override
  public TransportHost initTransportHost(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);
    TransportHost traHost = transportFactory.init(parameters);
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
  private Options getOptions(String parameters) throws ParameterException {
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
      logger.error(exception);
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
