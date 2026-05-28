/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 * 
 * @licence @@GRIIS_LICENCE@@
 * 
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe JavaTrustStore
 * @brief @~english «File description»
 */

package ca.griis.speds.link.internal.security;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

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
 * @brief @~french Magasin de confiance.
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
 *      2021-08-05 [FO] - Implémentation initiale.
 *
 * @par Tâches
 *      S.O.
 */
public class JavaTrustStore {
  private static final GriisLogger logger = getLogger(JavaTrustStore.class);

  /**
   * @brief @~english «Description of the method»
   * @return «Return description»
   *
   * @brief @~french Récupère le gestionnaire de confiance par défaut défini par
   *        javax.net.ssl.trustStore.
   * @return Gestionnaire de confiance au format X.509.
   */
  public static X509TrustManager getDefaultTrustManager() {
    logger.trace(Trace.ENTER_METHOD_0);

    X509TrustManager tm = null;

    TrustManagerFactory factory = getDefaultTrustFactoryManager();
    TrustManager[] managers = factory.getTrustManagers();

    tm = (X509TrustManager) managers[0];

    logger.trace(Trace.EXIT_METHOD_1, "trustManager", tm);
    return tm;
  }

  /**
   * @brief @~english «Description of the method»
   * @return «Return description»
   *
   * @brief @~french Récupère la fabrique du gestionnaire de confiance par défaut défini par
   *        javax.net.ssl.trustStore.
   * @return Fabrique de gestionnaire de confiance au format X.509.
   */
  public static TrustManagerFactory getDefaultTrustFactoryManager() {
    logger.trace(Trace.ENTER_METHOD_0);

    TrustManagerFactory tmf = null;

    try {
      String cacerts =
          Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts").toString();
      String uri = System.getProperty("javax.net.ssl.trustStore", cacerts);
      String type = System.getProperty("javax.net.ssl.trustStoreType", "JKS");

      char[] password = null;
      String pwd = System.getProperty("javax.net.ssl.trustStorePassword");
      if (pwd != null) {
        password = pwd.toCharArray();
      }

      KeyStore ks = JavaKeyStore.createKeyStore(uri, type, password);
      tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);
    } catch (KeyStoreException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to initialize TrustManagerFactory", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "trustManagerFactory", tmf);
    return tmf;
  }
}
