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

package ca.griis.speds.network.signature;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.service.exception.ParameterException;
import ca.griis.speds.network.service.exception.SerializationException;
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
import java.util.Base64;

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
 *      2025-03-04 [CB] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public class CertificatePrivateKeyPair {
  private static final GriisLogger logger = getLogger(CertificatePrivateKeyPair.class);

  public static final String CERTIFICATE_ALGORITHM = "X.509";
  public static final String PRIVATE_KEY_ALGORITHM = "RSA";

  private final Certificate certificate;
  private final PrivateKey privateKey;

  public CertificatePrivateKeyPair(Certificate certificate, PrivateKey privateKey) {
    logger.trace(Trace.ENTER_METHOD_2, "certificate", certificate, "privateKey", privateKey);
    this.certificate = certificate;
    this.privateKey = privateKey;
  }

  /**
   * @brief @~english «Description of the function»
   * @param certificatePem «Parameter description»
   * @param privateKeyPem «Parameter description»
   * @exception ParameterException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Importe un certificat d'identité cryptographique et sa clé privée associée à
   *        partir d'un format sérialisé.
   * @param certificatePem Le certificat à importer en chaîne de caractères de format PEM.
   * @param privateKeyPem La clé privée à importer en chaîne de caractères de format PEM.
   * @exception ParameterException Erreur soulevée lors de problèmes durant l'importation
   *            des éléments cryptographiques de la couche Réseau.
   * @return Le certificat de la couche Réseau et sa clé privée associée.
   *
   * @par Tâches
   *      S.O.
   */
  public static CertificatePrivateKeyPair importFromPem(String certificatePem,
      String privateKeyPem) throws ParameterException {
    logger.trace(Trace.ENTER_METHOD_2, "certificatePem", certificatePem, "privateKeyPem",
        privateKeyPem);

    final Certificate certificate;
    final PrivateKey privateKey;
    try {
      final CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_ALGORITHM);
      certificate =
          cf.generateCertificate(
              new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      final byte[] encoded = Base64.getDecoder().decode(privateKeyPem);
      final KeyFactory keyFactory = KeyFactory.getInstance(PRIVATE_KEY_ALGORITHM);
      final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
      privateKey = keyFactory.generatePrivate(keySpec);
    } catch (CertificateException | IllegalArgumentException | InvalidKeySpecException
        | NoSuchAlgorithmException e) {
      final String exception =
          "Problems encountered while importing Network certificate and private key: "
              + e.getMessage();
      logger.error(exception);
      throw new ParameterException(exception);
    }

    final CertificatePrivateKeyPair certificatePrivateKeyPair =
        new CertificatePrivateKeyPair(certificate, privateKey);
    logger.trace(Trace.EXIT_METHOD_1, "certificatePrivateKeyPair", certificatePrivateKeyPair);
    return certificatePrivateKeyPair;
  }

  public PrivateKey privateKey() {
    return privateKey;
  }

  /**
   * @brief @~english «Description of the function»
   * @exception SerializationException «Exception description»
   * @return «Return description»
   *
   * @brief @~french Récupère le certificat cryptographique d'identité sérialisé en chaîne de
   *        caractères au format Base64.
   * @exception SerializationException Erreur survenue lors de la sérialisation du certificat.
   * @return Le certificat cryptographique d'identité sérialisé.
   *
   * @par Tâches
   *      S.O.
   */
  public String getAuthentification() throws SerializationException {
    logger.trace(Trace.ENTER_METHOD_0);

    final String authentification;
    try {
      authentification = Base64.getEncoder().encodeToString(this.certificate.getEncoded());
    } catch (CertificateEncodingException e) {
      final String exception =
          "Cannot serialize Network layer certificate: " + e.getMessage();
      logger.error(exception);
      throw new SerializationException(exception);
    }
    logger.trace(Trace.EXIT_METHOD_1, "authentification", authentification);
    return authentification;
  }
}
