/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe SessionSecurityService.
 * @brief @~english Implementation of the SessionSecurityService class.
 */

package ca.griis.speds.session.internal.security.crypto;

import static ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.SpedsLayer.SESSION;

import ca.griis.js2p.gen.speds.toolkit.api.dto.SpedsConfigItemDto.AlgorithmCategory;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.session.api.exception.ParameterException;
import ca.griis.speds.session.internal.security.CertificatePrivateKeysEntry;
import ca.griis.speds.toolkit.crypto.api.CryptographyService;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
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
import java.util.UUID;
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
 * @brief @~french Offre un service de sécurité utilisé par la couche session et qui utilise
 *        speds-toolkit directement.
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
 *      2026-03-13 [FO] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public class SessionSecurityService {
  private static final GriisLogger logger =
      GriisLoggerFactory.getLogger(SessionSecurityService.class);

  private final CryptographyService cryptographyService;

  public SessionSecurityService(CryptographyService cryptographyService) {
    this.cryptographyService = cryptographyService;
  }

  public CertificatePrivateKeysEntry getCertificatePrivateKey(String certificatePem,
      String privateKeyPem) throws ParameterException {
    final Certificate certificate;
    final PrivateKey privateKey;
    byte[] encoded = new byte[0];
    try {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      certificate = cf.generateCertificate(
          new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      encoded = Base64.getDecoder().decode(privateKeyPem);
      final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      privateKey = keyFactory.generatePrivate(keySpec);
    } catch (CertificateException | InvalidKeySpecException | NoSuchAlgorithmException e) {
      final String exception =
          "Problems encountered while importing Session certificate and private key: "
              + e.getMessage();
      throw new ParameterException(exception);
    } finally {
      SessionKeyDestroyer.destroy(encoded);
    }

    final CertificatePrivateKeysEntry certificatePrivateKeyEntry =
        new CertificatePrivateKeysEntry(certificate, privateKey);
    return certificatePrivateKeyEntry;
  }

  public PublicKey base64ToPublicKey(String base64PublicKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    var algo = cryptographyService.getAlgorithm(SESSION, AlgorithmCategory.ASYM);

    if (!algo.contains("RSA")) {
      throw new NoSuchAlgorithmException(
          algo + "Impossible to create a public key from this algo: " + algo);
    }
    /**
     * @note 2026-03-10 - Seulement RSA est disonible pour un chiffrement asymétrique avec la
     *       bibliothèque de speds-toolkit.
     */
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(spec);
  }

  public SecretKey base64ToSecretKey(String base64Key) {
    String algo = cryptographyService.getAlgorithm(SESSION, AlgorithmCategory.SYMM);
    if (!algo.contains("AES")) {
      throw new RuntimeException("Unsupported algo for secret key creation");
    }

    byte[] keyBytes = Base64.getDecoder().decode(base64Key);
    SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    SessionKeyDestroyer.destroy(keyBytes);

    return key;
  }

  public PublicKey convertToDhPublicKey(String base64PublicKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    String algo = cryptographyService.getAlgorithm(SESSION, AlgorithmCategory.DH);
    final byte[] publicBytes = Base64.getDecoder().decode(base64PublicKey);
    final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
    final KeyFactory keyFactory = KeyFactory.getInstance(algo);

    PublicKey publicKey = keyFactory.generatePublic(keySpec);
    return publicKey;
  }

  public Boolean verifyStamp(byte[] stamp, byte[] data, SecretKey secretKey) {
    Boolean result = false;

    try {
      final byte[] hashValue = cryptographyService.hash(SESSION, data);
      final byte[] decryptedStamp = cryptographyService.decryptSymmetric(SESSION, secretKey, stamp);
      result = MessageDigest.isEqual(hashValue, decryptedStamp);
    } catch (Exception ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    return result;
  }

  public Boolean verifyToken(UUID token, SecretKey secretKey, String data) {
    Boolean result = false;

    try {
      final byte[] tokenByte = token.toString().getBytes(StandardCharsets.UTF_8);
      final byte[] tokenToValidate =
          cryptographyService.decryptSymmetric(SESSION, secretKey,
              Base64.getDecoder().decode(data));
      result = MessageDigest.isEqual(tokenToValidate, tokenByte);
    } catch (Exception ex) {
      logger.error(Error.IGNORED_ERROR, ex);
    }

    return result;
  }
}
