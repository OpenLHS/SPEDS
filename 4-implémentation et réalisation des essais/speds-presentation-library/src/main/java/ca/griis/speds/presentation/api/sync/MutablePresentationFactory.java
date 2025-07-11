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

package ca.griis.speds.presentation.api.sync;

import ca.griis.js2p.gen.speds.presentation.api.dto.InitInParamsDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.exception.ParameterException;
import ca.griis.speds.presentation.api.PresentationFactory;
import ca.griis.speds.presentation.api.PresentationHost;
import ca.griis.speds.session.api.PgaService;
import ca.griis.speds.session.api.SessionHost;
import ca.griis.speds.session.api.sync.ImmutableSessionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class MutablePresentationFactory implements PresentationFactory {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(MutablePresentationFactory.class);
  private final ObjectMapper mapper;
  private final ImmutableSessionFactory factory;

  public MutablePresentationFactory(PgaService pgaService) {
    this.mapper = new ObjectMapper();
    this.factory = new ImmutableSessionFactory(pgaService);
  }

  @Override
  public PresentationHost init(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    SessionHost host = initSessionHost(parameters);
    InitInParamsDto params;
    try {
      params = mapper.readValue(parameters, InitInParamsDto.class);

      String spedsVersion = (String) params.getOptions().get("speds.pre.version");
      String spedsReference = (String) params.getOptions().get("speds.pre.reference");

      if (spedsVersion == null || spedsReference == null) {
        throw new ParameterException(
            "SPEDS version or reference is missing in the initialization parameters. "
                + "spedsVersion=" + spedsVersion + " spedsReference=" + spedsReference);
      }

      PresentationHost presentationHost =
          new ImmutablePresentationHost(host, spedsVersion, spedsReference, mapper);

      logger.trace(Trace.EXIT_METHOD_1, "presentationHost", presentationHost);
      return presentationHost;
    } catch (JsonProcessingException e) {
      throw new ParameterException("Cannot read initialization parameters" + e.getMessage());
    }
  }

  @Override
  public SessionHost initSessionHost(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);
    SessionHost sessionHost = this.factory.init(parameters);
    logger.trace(Trace.EXIT_METHOD_1, "sessionHost", sessionHost);
    return sessionHost;
  }
}
