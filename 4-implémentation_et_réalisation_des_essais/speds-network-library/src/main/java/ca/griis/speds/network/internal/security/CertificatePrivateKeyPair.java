/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe CertificatePrivateKeyPair.
 * @brief @~english CertificatePrivateKeyPair class implementation.
 */

package ca.griis.speds.network.internal.security;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.speds.network.api.exception.ParameterException;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
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
 * @brief @~french Représente une clé privée cryptographique et son certificat associé.
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
 *      2026-02-16 [FO] - Refactorisation.
 *      2025-03-04 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class CertificatePrivateKeyPair {
  private static final GriisLogger logger = getLogger(CertificatePrivateKeyPair.class);

  private final Certificate certificate;
  private final String authentification;
  private PrivateKey privateKey;

  public static CertificatePrivateKeyPair importFromPem(String certificatePem,
      String privateKeyPem) {

    final var certificate = loadCertificatePem(certificatePem);
    final var privateKey = loadPrivateKeyPem(privateKeyPem);
    final var authentification = toBase64(certificate);

    final var pair = new CertificatePrivateKeyPair(certificate, privateKey, authentification);
    return pair;
  }

  private static Certificate loadCertificatePem(String certificatePem) {
    final Certificate certificate;

    try {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      certificate =
          cf.generateCertificate(
              new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));
    } catch (CertificateException | IllegalArgumentException e) {
      final String exception =
          "Problems encountered while importing Network certificate: " + e.getMessage();
      throw new ParameterException(exception);
    }

    return certificate;
  }

  private static PrivateKey loadPrivateKeyPem(String privateKeyPem) {
    PrivateKey privateKey;

    try {
      privateKey = loadPrivateKeyPem(privateKeyPem, "RSA");
    } catch (ParameterException e) {
      logger.error(Error.IGNORED_ERROR, e);

      try {
        privateKey = loadPrivateKeyPem(privateKeyPem, "Ed25519");
      } catch (ParameterException ex) {
        throw new ParameterException(ex.getMessage(), ex);
      }
    }

    return privateKey;
  }

  private static PrivateKey loadPrivateKeyPem(String privateKeyPem, String algo) {
    final PrivateKey privateKey;
    byte[] encoded = new byte[0];
    try {
      final KeyFactory keyFactory = KeyFactory.getInstance(algo);
      encoded = Base64.getDecoder().decode(privateKeyPem);
      final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      privateKey = keyFactory.generatePrivate(keySpec);
      Arrays.fill(encoded, (byte) 0);

      return privateKey;
    } catch (IllegalArgumentException | InvalidKeySpecException | NoSuchAlgorithmException e) {
      Arrays.fill(encoded, (byte) 0);
      final String exception =
          "Problems encountered while importing private key: " + e.getMessage();
      throw new ParameterException(exception);
    }
  }

  private static String toBase64(Certificate certificate) {
    final String authentification;
    try {
      authentification = Base64.getEncoder().encodeToString(certificate.getEncoded());
      return authentification;
    } catch (CertificateEncodingException e) {
      throw new ParameterException(e.getMessage(), e);
    }
  }

  private CertificatePrivateKeyPair(Certificate certificate, PrivateKey privateKey,
      String authentification) {
    this.certificate = certificate;
    this.privateKey = privateKey;
    this.authentification = authentification;
  }

  public Certificate getCertificate() {
    return certificate;
  }

  public PrivateKey getPrivateKey() {
    return privateKey;
  }

  public String getAuthentification() {
    return authentification;
  }

  public void cleanUp() {
    try {
      privateKey.destroy();
      privateKey = null;
    } catch (DestroyFailedException ex) {
      logger.debug(Error.IGNORED_ERROR, "ex", ex);
    }
  }
}
