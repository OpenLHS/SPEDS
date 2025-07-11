/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableDataLinkFactory.
 * @brief @~english Implementation of the ImmutableDataLinkFactory class.
 */

package ca.griis.speds.link.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.cryptography.truststore.JavaTrustStore;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.https.HttpsHost;
import ca.griis.speds.link.api.DataLinkFactory;
import ca.griis.speds.link.api.DataLinkHost;
import ca.griis.speds.link.api.dto.InitInParamsDto;
import ca.griis.speds.link.api.exception.ParameterException;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.link.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

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
 * @brief @~french Offre une fabriques d'entités nécessaires à la couche liaison.
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
 *      2025-02-10 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableDataLinkFactory implements DataLinkFactory {
  private static final GriisLogger logger = getLogger(ImmutableDataLinkFactory.class);

  private final ObjectMapper sharedObject = SharedObjectMapper.getInstance().getMapper();

  @Override
  public DataLinkHost init(String parametersJson) {
    logger.trace(Trace.ENTER_METHOD_1, "parametersJson", parametersJson);

    // Valider et parser les paramètres
    Map<String, Object> parameters = deserializeParameters(parametersJson);

    // Extraire les paramètres
    String protocol = (String) parameters.get("speds.dl.protocol");
    String address = (String) parameters.get("speds.dl.https.server.host");
    Integer port = (Integer) parameters.get("speds.dl.https.server.port");
    String trustManagerMode = (String) parameters
        .getOrDefault("speds.dl.https.client.cert.trustmanager.mode", "insecure");

    if (protocol == null || address == null || port == null) {
      throw new ParameterException("missing parameters : " + "protocol=" + protocol + " address="
          + address + " port=" + port);
    }

    if (!protocol.equalsIgnoreCase("https")) {
      throw new ProtocolException("unsupported protocol!");
    }

    // Configurer les contextes SSL
    SslContext sslContextServer = buildServerSslContext(parameters);
    SslContext sslContextClient = buildClientSslContext(trustManagerMode);

    ImmutableDataLinkHost dataLinkHost = new ImmutableDataLinkHost(sharedObject,
        new HttpsHost(sharedObject, address, port, sslContextClient, sslContextServer));

    logger.trace(Trace.EXIT_METHOD_1, "dataLinkHost", dataLinkHost);
    return dataLinkHost;
  }


  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Deserialiser les paramètres de configuration.
   * @param parametersJson Les paramètres de configuration sous le
   *        format JSON.
   * @return Les paramètres de configuration.
   *
   * @par Tâches
   *      S.O.
   */
  private Map<String, Object> deserializeParameters(String parametersJson) {
    logger.trace(Trace.ENTER_METHOD_1, "parametersJson", parametersJson);

    InitInParamsDto parameters;

    try {
      parameters = SharedObjectMapper.getInstance().getMapper().readValue(parametersJson,
          InitInParamsDto.class);
    } catch (JsonProcessingException e) {
      throw new ParameterException("Invalid parameters format", e);
    }

    Map<String, Object> options = parameters.getOptions();

    logger.trace(Trace.EXIT_METHOD_1, "options", options);

    return options;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construire le contexte ssl du serveur.
   * @param parameters Les paramètres de configuration
   * @return Le contexte ssl du serveur.
   *
   * @par Tâches
   *      S.O.
   */
  private SslContext buildServerSslContext(Map<String, Object> parameters) {
    logger.trace(Trace.ENTER_METHOD_1, "parameters", parameters);

    SslContext sslContext = null;

    try {
      X509Certificate certificate = base64ToCertificate((String) Objects.requireNonNull(
          parameters.get("speds.dl.https.server.cert"), "Server certificate is required"));
      PrivateKey privateKey = base64ToPrivateKey((String) Objects.requireNonNull(
          parameters.get("speds.dl.https.server.private.key"), "Server private key is required"));

      sslContext = SslContextBuilder.forServer(privateKey, certificate)
          .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
    } catch (SSLException e) {
      throw new ProtocolException("Error initiating SSL context for server", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "sslContext", sslContext);
    return sslContext;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construire le contexte ssl du client.
   * @param trustManagerMode Le mode de gestion des certificats
   * @return Le contexte ssl du client.
   *
   * @par Tâches
   *      S.O.
   */
  private SslContext buildClientSslContext(String trustManagerMode) {
    logger.trace(Trace.ENTER_METHOD_1, "trustManagerMode", trustManagerMode);

    SslContext sslContext = null;
    try {
      TrustManagerFactory trustManagerFactory =
          "default".equals(trustManagerMode) ? JavaTrustStore.getDefaultTrustFactoryManager()
              : InsecureTrustManagerFactory.INSTANCE;

      sslContext = SslContextBuilder.forClient().trustManager(trustManagerFactory).build();
    } catch (SSLException e) {
      throw new ProtocolException("Error initiating SSL context for client", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "sslContext", sslContext);
    return sslContext;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Parser une chaine base64 en certificat.
   * @param base64Certificate La chaine encodée en base64
   * @return Le certificat.
   *
   * @par Tâches
   *      S.O.
   */
  private X509Certificate base64ToCertificate(String base64Certificate) {
    logger.trace(Trace.ENTER_METHOD_1, "base64Certificate", base64Certificate);

    X509Certificate x509Certificate = null;

    try {
      byte[] certificateBytes = Base64.getDecoder().decode(base64Certificate);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateBytes)) {
        x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
      }
    } catch (IOException | CertificateException | IllegalArgumentException e) {
      throw new ParameterException("Invalid Base64 encoded Certificate");
    }

    logger.trace(Trace.EXIT_METHOD_1, "x509Certificate", x509Certificate);
    return x509Certificate;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Parser une chaine base64 en clé privée.
   * @param base64PrivateKey La chaine encodée en base64
   * @return La clé privée.
   *
   * @par Tâches
   *      S.O.
   */
  private PrivateKey base64ToPrivateKey(String base64PrivateKey) {
    logger.trace(Trace.ENTER_METHOD_1, "base64PrivateKey", base64PrivateKey);

    PrivateKey privateKey = null;
    try {
      byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);
      PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      privateKey = keyFactory.generatePrivate(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
      throw new ParameterException("Invalid Base64 encoded Key");
    }

    logger.trace(Trace.EXIT_METHOD_1, "privateKey", privateKey);
    return privateKey;
  }
}
