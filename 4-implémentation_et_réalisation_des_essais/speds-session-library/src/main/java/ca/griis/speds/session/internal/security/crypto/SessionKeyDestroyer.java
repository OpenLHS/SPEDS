/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SessionKeyDestroyer.
 * @brief @~english Implementation of the SessionKeyDestroyer class.
 */

package ca.griis.speds.session.internal.security.crypto;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import java.security.PrivateKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;

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
 * @brief @~french Fournit un service de destruction de clé, dans les limites des capacités offertes
 *        par Java.
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2026-04-24 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class SessionKeyDestroyer {
  private static final GriisLogger logger = getLogger(SessionKeyDestroyer.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Détruit si possible une clé privée.
   * @param key La clé privée.
   *
   * @par Tâches
   *      S.O.
   */
  public static void destroy(PrivateKey key) {
    if (key != null) {
      try {
        key.destroy();
      } catch (DestroyFailedException ex) {
        logger.debug(Error.IGNORED_ERROR, "ex", ex);
      }
    }
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Détruit si possible une clé secrète.
   * @param key La clé secrète.
   *
   * @par Tâches
   *      S.O.
   */
  public static void destroy(SecretKey key) {
    if (key != null) {
      try {
        key.destroy();
      } catch (DestroyFailedException ex) {
        logger.debug(Error.IGNORED_ERROR, "ex", ex);
      }
    }
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Détruit si possible une clé secrète sous un tableau d'octets.
   * @param key La clé secrète.
   *
   * @par Tâches
   *      S.O.
   */
  public static void destroy(byte[] key) {
    if (key != null) {
      Arrays.fill(key, (byte) 0);
    }
  }
}
