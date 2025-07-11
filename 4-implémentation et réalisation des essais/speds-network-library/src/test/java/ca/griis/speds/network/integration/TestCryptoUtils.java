package ca.griis.speds.network.integration;

import ca.griis.cryptography.asymmetric.signature.entity.DigitalSignature;
import ca.griis.cryptography.asymmetric.signature.signing.RsaSigning;
import ca.griis.cryptography.asymmetric.signature.verification.RsaVerifySigning;
import ca.griis.speds.network.signature.CertificatePrivateKeyPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIException;
import org.apache.jena.iri.IRIFactory;

public class TestCryptoUtils {
  private static final String CERTIFICATE_ALGORITHM = "X.509";
  private final ObjectMapper objectMapper;

  public TestCryptoUtils() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * @brief @~english «Description of the function»
   * @param value «Parameter description»
   * @param seal «Parameter description»
   * @param key «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Crée un sceau à partir d'une valeur donnée.
   * @param value La valeur servant à la création du sceau.
   * @param seal Le type de sceau créé.
   * @param key la clé privée utilisée pour créer le sceau.
   * @return Le sceau créé à partir de la valeur.
   *
   * @par Tâches
   *      S.O.
   */
  public String createSeal(Object value, String seal, String key) {

    // Décoder le Base64
    byte[] keyBytes = Base64.getDecoder().decode(key);

    final String result;
    try {
      // Créer la spécification PKCS#8
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

      // Créer l'instance PrivateKey
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PrivateKey privateKey = keyFactory.generatePrivate(keySpec);


      final String message =
          value instanceof String ? (String) value : objectMapper.writeValueAsString(value);
      result = Base64.getEncoder()
          .encodeToString(new RsaSigning((RSAPrivateKey) privateKey).sign(message.getBytes(
              StandardCharsets.UTF_8)).getBytes());
    } catch (Exception e) {
      final String exception =
          "Cannot serialize " + seal + " seal: " + e.getMessage();

      throw new RuntimeException(exception);
    }

    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param value «Parameter description»
   * @param seal «Parameter description»
   * @param certificatePem «Parameter description»
   * @param signature «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Vérifie l'intégrité d'un objet en validant sa signature associée à l'aide d'un
   *        certificat cryptographique.
   * @param value La valeur dont on veut vérifier l'intégrité.
   * @param seal Le type de sceau fourni.
   * @param certificatePem le certificat associé à la clé privée ayant servi à la création de la
   *        signature.
   * @param signature la signature fournie pour valider l'intégrité de la valeur en Base64.
   * @return Valeur booléenne vraie si la valeur est intègre, et fausse sinon.
   *
   * @par Tâches
   *      S.O.
   */
  public Boolean verifySeal(Object value, String seal, String certificatePem,
      String signature) {
    final Boolean result;
    try {
      final CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_ALGORITHM);
      final Certificate certificate =
          cf.generateCertificate(
              new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      final byte[] message =
          value instanceof String ? ((String) value).getBytes(StandardCharsets.UTF_8)
              : objectMapper.writeValueAsString(value).getBytes(StandardCharsets.UTF_8);
      result = new RsaVerifySigning((RSAPublicKey) certificate.getPublicKey()).verify(message,
          new DigitalSignature(
              Base64.getDecoder().decode(signature.getBytes(StandardCharsets.UTF_8))));
    } catch (SecurityException | JsonProcessingException | CertificateException e) {
      final String exception =
          "Cannot verify " + seal + " seal: " + e.getMessage();

      throw new RuntimeException(exception);
    }

    return result;
  }


  /**
   * @brief @~english «Description of the function»
   * @param certificatePem «Parameter description»
   * @param iri «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Vérifie la validité du certificat cryptographique.
   * @param certificatePem le certificat.
   * @param iri L'iri de l'hôte propriétaire du certificat.
   * @return Valeur booléenne vraie si le certificat est valide, et fausse sinon.
   *
   * @par Tâches
   *      S.O.
   */
  public boolean verifyCertificate(String certificatePem, String iri) {
    boolean verify = true;
    try {
      final CertificateFactory cf =
          CertificateFactory.getInstance(CertificatePrivateKeyPair.CERTIFICATE_ALGORITHM);

      X509Certificate certificate = (X509Certificate) cf.generateCertificate(
          new ByteArrayInputStream(Base64.getDecoder().decode(certificatePem)));

      if (certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal())) {
        certificate.verify(certificate.getPublicKey());
        IRIFactory iriFactory = IRIFactory.iriImplementation();

        try {
          IRI currentIri = iriFactory.construct(iri);
          if (!getCommonName(certificate).equals(currentIri.getASCIIHost())) {
            verify = false;
          }
        } catch (IRIException | MalformedURLException | InvalidNameException e) {
          verify = false;
        }
      } else {
        verify = false;
      }
    } catch (CertificateException | InvalidKeyException | NoSuchAlgorithmException
        | NoSuchProviderException | SignatureException e) {
      verify = false;
    }
    return verify;
  }


  /**
   * @brief @~english «Description of the function»
   * @param cert «Parameter description»
   * @return «Return description»
   *
   * @brief @~french Obtenir le nom commun du certificat cryptographique.
   * @param cert le certificat.
   * @return Nom commun du certificat.
   *
   * @par Tâches
   *      S.O.
   */
  private String getCommonName(X509Certificate cert) throws InvalidNameException {
    String dn = cert.getSubjectX500Principal().getName();
    LdapName ldapDN = new LdapName(dn);
    for (Rdn rdn : ldapDN.getRdns()) {
      if (rdn.getType().equalsIgnoreCase("CN")) {
        return (String) rdn.getValue();
      }
    }
    return ""; // CN non trouvé
  }
}
