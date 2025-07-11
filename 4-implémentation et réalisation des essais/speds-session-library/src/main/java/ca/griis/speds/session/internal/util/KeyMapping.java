/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Contient la description de la classe KeyMapping.java
 * @brief @~english Contains description of KeyMapping.java class.
 */

package ca.griis.speds.session.internal.util;

import ca.griis.cryptography.algorithm.SecretKeyGeneratorAlgorithm;
import ca.griis.cryptography.asymmetric.keypair.CertificatePrivateKeysEntry;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.CipherException;
import ca.griis.speds.session.api.exception.ParameterException;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
 * @brief @~french Transformation d'une clé exprimée en chaîne de caratères à un object java
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
 *      2025-03-18 [SSC] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class KeyMapping {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(KeyMapping.class);
  public static final String CERTIFICATE_ALGORITHM = "X.509";

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Obtenir un objet PublicKey à partir d'une chaîne de caratères
   * @param publicK une chaîne de caractères au format Base64 représentant une clé publique
   *        respectant la norme X.509
   * @param keyAlgorithm Algorithme de clé constituant la clé publique à récupérer
   * @exception CipherException Erreur soulevée lors de la récupération de la clé publique
   * @return Une clé publique
   */
  public static PublicKey getPublicKeyFromString(String publicK, KeyAlgorithm keyAlgorithm) {
    logger.trace(Trace.ENTER_METHOD_2, "publicK", publicK, "keyAlgorithm", keyAlgorithm);
    final PublicKey pubKey;
    try {
      final byte[] publicBytes = Base64.getDecoder().decode(publicK);
      final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
      final KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm.name());
      pubKey = keyFactory.generatePublic(keySpec);
    } catch (Exception ex) {
      throw new CipherException("Public Key is of the wrong format", ex);
    }

    logger.trace(Trace.EXIT_METHOD_1, "pubKey", pubKey);
    return pubKey;
  }

  /**
   * @brief @~english «Description of the function»
   * @param secretKey «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Récupère la clé secrète à partir d'un tableau d'octet
   * @param secretKey la clé secrète sous forme de tableau d'octet
   * @return La clé secrète en objet java
   *
   * @par Tâches
   *      S.O.
   */
  public static SecretKey getAesSecretKeyFromByte(byte[] secretKey) {
    logger.trace(Trace.ENTER_METHOD_1, "secretKey", secretKey);

    final SecretKey sk = new SecretKeySpec(secretKey, 0, secretKey.length,
        SecretKeyGeneratorAlgorithm.AES.getAlgorithm());

    logger.trace(Trace.EXIT_METHOD_1, "sk", sk);
    return sk;
  }

  /**
   * @brief @~english «Description of the function»
   * @param certificatePem «Parameter description»
   * @param privateKeyPem «Parameter description»
   * @exception ParameterException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Importe le certificat et la clé privée associée permettant de gérer
   *        les signatures des messages Session.
   * @param certificatePem Le certificat à importer en chaîne de caractères de format PEM.
   * @param privateKeyPem La clé privée à importer en chaîne de caractères de format PEM.
   * @exception ParameterException Erreur soulevée lors de l'importation des éléments
   *            cryptographiques de la couche Session.
   * @return Le certificat de la couche Session et sa clé privée associée.
   *
   * @par Tâches
   *      S.O.
   */
  public static CertificatePrivateKeysEntry getCertificatePrivateKey(String certificatePem,
      String privateKeyPem) {
    logger.trace(Trace.ENTER_METHOD_3, "certificatePem", certificatePem, "privateKeyPem",
        privateKeyPem);

    final Certificate certificate;
    final PrivateKey privateKey;
    try {
      final CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_ALGORITHM);
      certificate = cf.generateCertificate(
          new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      final byte[] encoded = Base64.getDecoder().decode(privateKeyPem);
      final KeyFactory keyFactory = KeyFactory.getInstance(KeyAlgorithm.RSA.name());
      final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      privateKey = keyFactory.generatePrivate(keySpec);
    } catch (CertificateException | InvalidKeySpecException | NoSuchAlgorithmException e) {
      final String exception =
          "Problems encountered while importing Session certificate and private key: "
              + e.getMessage();
      logger.error(exception);
      throw new ParameterException(exception);
    }

    final CertificatePrivateKeysEntry certificatePrivateKeyEntry =
        new CertificatePrivateKeysEntry(certificate, privateKey, null);
    logger.trace(Trace.EXIT_METHOD_1, "certificatePrivateKeyEntry", certificatePrivateKeyEntry);
    return certificatePrivateKeyEntry;
  }
}
