/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 * 
 * @licence @@GRIIS_LICENCE@@
 * 
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe JavaKeyStore
 * @brief @~english «File description»
 */

package ca.griis.speds.link.internal.security;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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
 * @brief @~french Magasin de clés.
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
 *      2020-01-16 [SHD] - Implémentation initiale.
 *
 * @note 2020-01-20 [SHD] - pkcs12 comme type de magasin de clés.
 *
 *       <h1>Explication</h1>
 *       Ce type est du code source libre et est compatible avec OpenSSL.
 *
 *       <h1>Référence</h1>
 *       https://myarch.com/cert-book/keystore_best_practices.html#use-the-pkcs12-format-for-keystores
 *
 * @note 2020-02-13 [SHD] - KeyStore Explorer
 *       http://keystore-explorer.org/
 *       Outil intéressant pour gérer les clés.
 *
 * @par Tâches
 *      S.O.
 */
public class JavaKeyStore {
  private static final GriisLogger logger = getLogger(JavaKeyStore.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Crée une instance de connexion au magasin de clés.
   * @param uri l'URI du fichier contenant le magasin de clés
   * @param type le type de magasin de clés
   * @param password le mot de passe pour se connecter au magasin de clés
   * @return le magasin de clés
   *
   * @par Tâches
   *      S.O.
   */
  public static KeyStore createKeyStore(String uri, String type, char[] password) {
    logger.trace(Trace.ENTER_METHOD_3, "uri", uri, "type", type, "password", "********");

    KeyStore ks = null;

    try {
      ks = KeyStore.getInstance(type);

      try (InputStream is = getKeyStoreInputStream(uri)) {
        ks.load(is, password);
      }

    } catch (IOException | CertificateException e) {
      throw new SecurityException("Failed to load keystore", e);

    } catch (KeyStoreException | NoSuchAlgorithmException e) {
      throw new SecurityException("Can't create KeyStore", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "keyStore", ks);
    return ks;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère l'URL du fichier contenant le magasin de clés
   * @param uri l'URI du fichier contenant le magasin de clés
   * @return l'URL du fichier contenant le magasin de clés
   * @throws FileNotFoundException Dans le cas que le fichier existe, mais qu'on ne peut faire de
   *         stream.
   *
   * @par Tâches
   *      S.O.
   */
  private static InputStream getKeyStoreInputStream(String uri) throws IOException {
    InputStream is = null;

    Path path = Paths.get(uri);
    if (path.isAbsolute()) {
      if (!Files.exists(path)) {
        throw new FileNotFoundException("System keystore not found: " + uri);
      }

      is = Files.newInputStream(path);
    } else {
      Path baseDir = Paths.get("config/keystores").toAbsolutePath().normalize();
      Path resolved = baseDir.resolve(uri).normalize();

      if (!resolved.startsWith(baseDir)) {
        throw new SecurityException("Path traversal detected");
      }

      if (!Files.exists(resolved)) {
        throw new FileNotFoundException("Keystore not found: " + uri);
      }

      is = Files.newInputStream(resolved);
    }

    return is;
  }
}
