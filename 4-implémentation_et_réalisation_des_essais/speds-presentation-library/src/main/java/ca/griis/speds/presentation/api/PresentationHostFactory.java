/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe MutablePresentationFactory.
 * @brief @~english Implementation of the MutablePresentationFactory class.
 */

package ca.griis.speds.presentation.api;

import ca.griis.js2p.gen.speds.presentation.api.dto.InitInParamsDto;
import ca.griis.js2p.gen.speds.presentation.api.dto.VersionDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.presentation.api.exception.ParameterException;
import ca.griis.speds.presentation.entity.PresentationTracking;
import ca.griis.speds.presentation.entity.TrackingInformation;
import ca.griis.speds.presentation.internal.ImmutablePresentationHost;
import ca.griis.speds.presentation.internal.event.PresentationEventHandler;
import ca.griis.speds.presentation.internal.serialization.SharedObjectMapper;
import ca.griis.speds.session.api.SessionFactory;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.SessionHostEvent;
import ca.griis.speds.session.api.SessionHostFactory;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import ca.griis.speds.toolkit.project.ProjectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import java.util.concurrent.TimeUnit;

/**
 * @brief @~englisription (class, interface, ...)»
 * @par Details
 *      «Detailed description of the component (optional)»
 * @par Model
 *      «Model (Abstract, automation, etc.) (optional)»
 * @par Conception
 *      «Conception description (criteria and constraints) (optional)»
 * @par Limits
 *      «Limits description (optional)»
 *
 * @brief @~french Offre une fabrique d'entités nécessaires à la couche présentation.
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
 *      2025-02-18 [MD] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class PresentationHostFactory implements PresentationFactory {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(PresentationHostFactory.class);
  private final SessionFactory factory;
  private final CryptographyService service;

  public PresentationHostFactory(ProjectService projectService, CryptographyService service) {
    this.service = service;
    this.factory = new SessionHostFactory(projectService, service);
  }

  @Override
  public PresentationHost initHost(String parameters, PresentationHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_2, "parameters", parameters, "hostEventConsumer",
        hostEventConsumer);

    final var handler = new PresentationEventHandler();
    final SessionHost host = initSessionHost(parameters, handler);
    InitInParamsDto params;
    try {
      params =
          SharedObjectMapper.getInstance().getMapper().readValue(parameters, InitInParamsDto.class);
      String spedsVersion = (String) params.getOptions().get("speds.pre.version");
      String spedsReference = (String) params.getOptions().get("speds.pre.reference");
      Integer responseWindowMinutes =
          (Integer) params.getOptions().getOrDefault("speds.pre.response.window.minutes", 10);

      VersionDto version = new VersionDto(spedsVersion, spedsReference);

      if (spedsVersion == null || spedsReference == null) {
        throw new ParameterException(
            "SPEDS version or reference is missing in the initialization parameters. "
                + "spedsVersion=" + spedsVersion + " spedsReference=" + spedsReference);
      }

      final var pendingResponses = Caffeine.newBuilder()
          .expireAfterWrite(responseWindowMinutes, TimeUnit.MINUTES)
          .maximumSize(100_000)
          .removalListener(
              (RemovalListener<PresentationTracking, TrackingInformation>) (key, value, cause) -> {
                if (value != null) {
                  value.destroy();
                }
              })
          .build();

      final var presentationHost =
          new ImmutablePresentationHost(host, version, service, hostEventConsumer,
              pendingResponses);

      handler.register(presentationHost);

      logger.trace(Trace.EXIT_METHOD_1, "presentationHost", presentationHost);
      return presentationHost;
    } catch (JsonProcessingException e) {
      throw new ParameterException("Cannot read initialization parameters" + e.getMessage());
    }
  }

  @Override
  public SessionHost initSessionHost(String parameters, SessionHostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    SessionHost host = factory.initHost(parameters, hostEventConsumer);

    logger.trace(Trace.EXIT_METHOD_1, "host", host);
    return host;
  }
}
