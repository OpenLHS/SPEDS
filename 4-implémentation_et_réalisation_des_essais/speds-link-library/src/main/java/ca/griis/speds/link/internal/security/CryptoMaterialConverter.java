/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe CryptoMaterialConverter.
 * @brief @~english Implementation of the CryptoMaterialConverter class.
 */

package ca.griis.speds.link.internal.security;

import ca.griis.speds.link.api.exception.ParameterException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

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
 * @brief @~french Fournit un service permettant de convertir un certificat et une clé privée
 *        encodés en Base64 en objets Java.
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
 *      2026-04-21 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class CryptoMaterialConverter {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Convertir une chaine base64 en certificat.
   * @param base64Certificate La chaine encodée en base64
   * @return Le certificat.
   *
   * @par Tâches
   *      S.O.
   */
  public X509Certificate toCertificate(String base64Certificate) {
    try {
      byte[] certificateBytes = Base64.getDecoder().decode(base64Certificate);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      X509Certificate x509Certificate = null;
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateBytes)) {
        x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
      }

      return x509Certificate;
    } catch (IOException | CertificateException | IllegalArgumentException e) {
      throw new ParameterException("Invalid Base64 encoded Certificate");
    }
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Convertir une chaine base64 en clé privée.
   * @param base64PrivateKey La chaine encodée en base64
   * @return La clé privée.
   *
   * @par Tâches
   *      S.O.
   */
  public PrivateKey toPrivateKey(String base64PrivateKey) {
    byte[] privateKeyBytes = new byte[0];
    try {
      privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);

      PrivateKeyInfo keyInfo =
          PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(privateKeyBytes));
      PrivateKey privateKey = new JcaPEMKeyConverter()
          .getPrivateKey(keyInfo);

      Arrays.fill(privateKeyBytes, (byte) 0);

      return privateKey;
    } catch (IllegalArgumentException | IOException e) {
      Arrays.fill(privateKeyBytes, (byte) 0);

      throw new ParameterException("Invalid Base64 encoded Key");
    }
  }
}
