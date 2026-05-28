/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SyncApplicationFactory.
 * @brief @~english Contains description of SyncApplicationFactory class.
 */

package ca.griis.speds.application.internal;

import ca.griis.js2p.gen.speds.application.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.application.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.ApplicationHostEvent;
import ca.griis.speds.application.api.exception.ParameterException;
import ca.griis.speds.application.internal.handler.ApplicationEventHandler;
import ca.griis.speds.application.internal.verification.InterfaceChecker;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.PresentationHostEvent;
import ca.griis.speds.presentation.api.PresentationHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @brief @~english «Brief component description (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      Factory method design pattern
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Offre une fabrique d'entités nécessaires à la couche application
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
 *      2025-02-11 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class ApplicationHostFactory implements ApplicationFactory {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(ApplicationHostFactory.class);

  private final ProjectService projectService;
  private final PresentationHostFactory presentationHostFactory;

  public ApplicationHostFactory(CryptographyService cryptographyService,
      ProjectService projectService) {
    logger.trace(Trace.ENTER_METHOD_2, "cryptographyService", cryptographyService,
        "projectService", projectService);
    this.projectService = projectService;
    this.presentationHostFactory = new PresentationHostFactory(projectService, cryptographyService);
  }

  @Override
  public ApplicationHost init(String parameters, ApplicationHostEvent consumer,
      InterfaceChecker interfaceChecker) {
    logger.trace(Trace.ENTER_METHOD_3, "parameters", parameters, "consumer", consumer,
        "interfaceChecker", interfaceChecker);

    final InitInParamsDto params;
    try {
      params = SharedObjectMapper.getInstance().getMapper().readValue(parameters,
          new TypeReference<InitInParamsDto>() {});

      final String spedsVersion = (String) params.getOptions().get("speds.app.version");
      final String spedsReference = (String) params.getOptions().get("speds.app.reference");

      if (spedsVersion == null || spedsReference == null) {
        throw new ParameterException(
            "SPEDS version or reference is missing in the initialization parameters. "
                + "spedsVersion=" + spedsVersion + " spedsReference=" + spedsReference);
      }

      final ApplicationEventHandler handler = new ApplicationEventHandler();
      final PresentationHost host = initPresentationHost(parameters, handler);
      final ApplicationHost appHost =
          new ImmutableApplicationHost(host, consumer, this.projectService,
              new VersionDto(spedsVersion, spedsReference), interfaceChecker);
      handler.register(appHost);

      logger.trace(Trace.EXIT_METHOD_1, "appHost", appHost);
      return appHost;
    } catch (JsonProcessingException e) {
      logger.error(Error.GENERIC_ERROR, e);
      throw new ParameterException("Cannot read initialization parameters");
    }
  }

  @Override
  public PresentationHost initPresentationHost(String parameters, PresentationHostEvent consumer) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    final PresentationHost presHost = presentationHostFactory.initHost(parameters, consumer);

    logger.trace(Trace.EXIT_METHOD_1, "presentationHost", presHost);
    return presHost;
  }
}
