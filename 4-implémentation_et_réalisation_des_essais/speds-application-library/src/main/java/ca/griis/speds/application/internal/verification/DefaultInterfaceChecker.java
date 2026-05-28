/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe DefaultInterfaceChecker.
 * @brief @~english DefaultInterfaceChecker class implementation.
 */

package ca.griis.speds.application.internal.verification;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.application.serializer.SharedObjectMapper;
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
 * @brief @~french Permet de vérifier le format du contenu d'une interface reçue par l'utilisateur
 *        de la couche application.
 * @par Détails
 *      S.O.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-03-05 [CB] - Implémentation initiale<br>
 *
 * @note 2026-04-23 [FO] - Actuellement, la vérification ne fait que vérifier si le contenu est dans
 *       un format JSON.
 *
 * @par Tâches
 *      S.O.
 */
public final class DefaultInterfaceChecker implements InterfaceChecker {
  private static final GriisLogger logger = getLogger(DefaultInterfaceChecker.class);

  @Override
  public boolean test(String s) {
    final ObjectMapper objectMapper = SharedObjectMapper.getInstance().getMapper();

    Boolean predicate = false;
    try {
      objectMapper.readTree(s);
      predicate = true;
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }

    logger.info(Info.VARIABLE_LOGGING_1, "predicate", predicate);

    return predicate;
  }
}
