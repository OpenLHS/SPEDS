/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe SpedsFactory.
 * @brief @~english Contains description of SpedsFactory class.
 */

package ca.griis.speds.api.factory;

import ca.griis.js2p.gen.speds.application.api.dto.InitInParamsDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.application.api.ApplicationFactory;
import ca.griis.speds.application.api.ApplicationHost;
import ca.griis.speds.application.api.sync.SyncApplicationFactory;
import ca.griis.speds.application.serializer.SharedObjectMapper;
import ca.griis.speds.session.api.PgaService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
 * @brief @~french Offre une fabrique d'entités du protocole SPEDS.
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
 *      2025-04-07 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SpedsFactory {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(SpedsFactory.class);

  private final ObjectMapper mapper;
  private final ApplicationFactory factory;

  public SpedsFactory(PgaService pgaService) {
    logger.trace(Trace.ENTER_METHOD_1, "pgaService", pgaService);

    this.factory = new SyncApplicationFactory(pgaService);
    this.mapper = SharedObjectMapper.getInstance().getMapper();
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit un hôte du protocole SPEDS.
   * @param parameters Les paramètres de l'hôte des couches.
   * @return L'interface hôte du protocole SPEDS.
   */
  public ApplicationHost init(InitInParamsDto parameters) throws JsonProcessingException {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    String params = mapper.writeValueAsString(parameters);
    ApplicationHost host = init(params);

    logger.trace(Trace.EXIT_METHOD_1, "host", host);
    return host;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit un hôte du protocole SPEDS.
   * @param parameters Les paramètres de l'hôte des couches.
   * @return L'interface hôte du protocole SPEDS.
   */
  public ApplicationHost init(String parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    ApplicationHost host = factory.init(parameters);

    logger.trace(Trace.EXIT_METHOD_1, "host", host);
    return host;
  }
}
