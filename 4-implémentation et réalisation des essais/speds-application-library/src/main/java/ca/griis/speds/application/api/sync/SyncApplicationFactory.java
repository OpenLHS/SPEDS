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

package ca.griis.speds.application.api.sync;

import ca.griis.js2p.gen.speds.application.api.dto.InitInParamsDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.exception.ParameterException;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.presentation.api.PresentationFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.presentation.api.sync.MutablePresentationFactory;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class SyncApplicationFactory implements ApplicationFactory {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SyncApplicationFactory.class);

  private ObjectMapper sharedObject = SharedObjectMapper.getInstance().getMapper();
  private PresentationFactory presentationFactory;

  public SyncApplicationFactory(PgaService pgaService) {
    this.presentationFactory = new MutablePresentationFactory(pgaService);
  }

  @Override
  public ApplicationHost init(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    InitInParamsDto params;
    try {
      params = SharedObjectMapper.getInstance().getMapper().readValue(parameters,
          new TypeReference<InitInParamsDto>() {});

      String spedsVersion = (String) params.getOptions().get("speds.app.version");
      String spedsReference = (String) params.getOptions().get("speds.app.reference");

      if (spedsVersion == null || spedsReference == null) {
        throw new ParameterException(
            "SPEDS version or reference is missing in the initialization parameters. "
                + "spedsVersion=" + spedsVersion + " spedsReference=" + spedsReference);
      }

      PresentationHost host = initPresentationHost(parameters);
      ApplicationHost appHost =
          new ImmutableApplicationHost(host, sharedObject, spedsVersion, spedsReference);

      logger.trace(Trace.EXIT_METHOD_1, "appHost", appHost);
      return appHost;
    } catch (JsonProcessingException e) {
      logger.error("Cannot read initialization parameters: " + e.getMessage());
      throw new ParameterException("Cannot read initialization parameters");
    }
  }

  @Override
  public PresentationHost initPresentationHost(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);
    PresentationHost presHost = presentationFactory.init(parameters);
    logger.trace(Trace.EXIT_METHOD_1, "presentationHost", presHost);
    return presHost;
  }
}
