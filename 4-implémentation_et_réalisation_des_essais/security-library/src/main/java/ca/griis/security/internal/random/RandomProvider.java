/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Description de la classe RandomProvider.
 * @brief @~english Implements the RandomProvider class.
 */

package ca.griis.security.internal.random;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Warn;
import ca.griis.security.api.domain.spec.csprng.CsprngSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
 * @brief @~french Fournisseur de générateur aléatoire sécurisé.
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
 *      2025-12-17 [BD] - Implémentation initiale.<br>
 *
 * @par Tâches
 *      S.O.
 */
public class RandomProvider {
  private static final GriisLogger logger = getLogger(RandomProvider.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Crée un générateur aléatoire sécurisé.
   * @param csprngSpec spécification de générateur aléatoire
   * @return la signature
   * @par Tâches S.O.
   */
  public static SecureRandom getSecureRandom(CsprngSpec csprngSpec) {
    SecureRandom secureRandom;

    try {
      secureRandom = SecureRandom.getInstance(csprngSpec.getAlgo(), csprngSpec.getParams());
    } catch (NoSuchAlgorithmException e) {
      secureRandom = new SecureRandom();

      logger.warn(Warn.VARIABLE_LOGGING_1, "defaultSecureRandom", true);
    }

    return secureRandom;
  }
}
