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

package ca.griis.speds.link.api.factory;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.ProtocolHost;
import ca.griis.speds.communication.protocol.ProtocolHostFactory;
import ca.griis.speds.link.api.Host;
import ca.griis.speds.link.api.HostEvent;
import ca.griis.speds.link.api.HostFactory;
import ca.griis.speds.link.api.dto.InitInParamsDto;
import ca.griis.speds.link.api.exception.ParameterException;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.link.internal.ImmutableDataLinkHost;
import ca.griis.speds.link.internal.event.ProtocolEventhandler;
import ca.griis.speds.link.internal.security.CryptoMaterialConverter;
import ca.griis.speds.link.internal.security.TlsContextFactory;
import ca.griis.speds.link.internal.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public final class ImmutableDataLinkFactory implements HostFactory {
  private static final GriisLogger logger = getLogger(ImmutableDataLinkFactory.class);

  private final ObjectMapper sharedObject;
  private final CryptoMaterialConverter converter;

  public ImmutableDataLinkFactory() {
    this.sharedObject = SharedObjectMapper.getInstance().getMapper();
    this.converter = new CryptoMaterialConverter();
  }

  @Override
  public Host init(String parametersJson, HostEvent hostEventConsumer) {
    logger.trace(Trace.ENTER_METHOD_2, "parametersJson", parametersJson, "hostEventConsumer",
        hostEventConsumer);

    // Valider et parser les paramètres
    final Map<String, Object> parameters = deserializeParameters(parametersJson);

    // Extraire les paramètres
    final String protocol = (String) parameters.get("speds.dl.protocol");
    final String address = (String) parameters.get("speds.dl.https.server.host");
    final Integer port = (Integer) parameters.get("speds.dl.https.server.port");
    final String httpsMode = (String) parameters.getOrDefault("speds.dl.https.mode", "mTLS");
    final Integer maxContentLengthBytes =
        (Integer) parameters.getOrDefault("speds.dl.https.max.content.length.bytes", 1048576);

    @SuppressWarnings("unchecked")
    final var certs =
        (List<String>) parameters.getOrDefault("speds.dl.https.root.certs", List.of());

    if (protocol == null || address == null || port == null) {
      throw new ParameterException("missing parameters : " + "protocol=" + protocol + " address="
          + address + " port=" + port);
    }

    if (!protocol.equalsIgnoreCase("https")) {
      throw new ProtocolException("unsupported protocol!");
    }

    final X509Certificate certificate = converter.toCertificate((String) Objects.requireNonNull(
        parameters.get("speds.dl.https.cert"), "Server certificate is required"));
    final PrivateKey privateKey = converter.toPrivateKey((String) Objects.requireNonNull(
        parameters.get("speds.dl.https.private.key"), "Server private key is required"));

    final List<X509Certificate> rootCerts = new ArrayList<>();
    for (String cert : certs) {
      final var x509Cert = converter.toCertificate(cert);
      rootCerts.add(x509Cert);
    }

    // Configurer les contextes TLS
    final SslContext sslContextServer =
        TlsContextFactory.createForServer(httpsMode, certificate, privateKey, rootCerts);
    final SslContext sslContextClient =
        TlsContextFactory.createForClient(httpsMode, certificate, privateKey, rootCerts);

    final ProtocolEventhandler eventhandler = new ProtocolEventhandler();
    final ProtocolHost httpsHost =
        ProtocolHostFactory.createHost(sharedObject, address, port, sslContextClient,
            sslContextServer,
            maxContentLengthBytes, eventhandler);

    final ImmutableDataLinkHost host =
        new ImmutableDataLinkHost(sharedObject, httpsHost, hostEventConsumer);

    eventhandler.register(host);

    logger.trace(Trace.EXIT_METHOD_1, "dataLinkHost", host);
    return host;
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
}
