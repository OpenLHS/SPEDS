/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe TlsContextFactory.
 * @brief @~english Implementation of the TlsContextFactory class.
 */

package ca.griis.speds.link.internal.security;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Warn;
import ca.griis.speds.link.api.exception.ProtocolException;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLException;

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
 * @brief @~french Offre un service de création de contexte TLS.
 * @par Details
 *      <P>
 *      Pour le modes mTLS ou TLS standard, si aucun certificat racine n’est défini, les
 *      certificats racines fournis par le JDK de Java sont utilisés par défaut.
 *      </p>
 * 
 *      <P>
 *      Seules les versions 1.2 et 1.3 du protocole TLS sont supportées.
 *      </p>
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
public final class TlsContextFactory {
  private static final GriisLogger logger = getLogger(TlsContextFactory.class);

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit le contexte TLS du serveur.
   * @param mode Le mode de vérification de certificat dans TLS.
   * @param certificate Certificat du serveur.
   * @param privateKey La clé privée du serveur.
   * @param rootCerts Les certificats racines.
   * @return Le contexte TLS du serveur.
   *
   * @par Tâches
   *      S.O.
   */
  public static SslContext createForServer(String mode, X509Certificate certificate,
      PrivateKey privateKey, List<X509Certificate> rootCerts) {

    try {
      var builder =
          SslContextBuilder.forServer(privateKey, certificate).protocols("TLSv1.2", "TLSv1.3");
      if (mode.equals("insecure")) {
        builder = builder.trustManager(InsecureTrustManagerFactory.INSTANCE);

        logger.warn(Warn.VARIABLE_LOGGING_1, "trustManagerFactory",
            "Insecure TLS client context. Use only for testing.");
      } else if (mode.equals("standard") || mode.equals("mTLS")) {
        if (rootCerts.isEmpty()) {
          builder = builder.trustManager(JavaTrustStore.getDefaultTrustFactoryManager());
        } else {
          builder = builder.trustManager(rootCerts);
        }

        if (mode.equals("mTLS")) {
          builder = builder.clientAuth(ClientAuth.REQUIRE);
        }
      } else {
        throw new ProtocolException("Unknown tls mode: " + mode);
      }

      SslContext sslContext = builder.build();
      return sslContext;
    } catch (SSLException e) {
      throw new ProtocolException("Error initiating TLS context for server", e);
    }
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit le contexte TLS du client.
   * @param mode Le mode de vérification de certificat dans TLS.
   * @param certificate Certificat du client.
   * @param privateKey La clé privée du client.
   * @param rootCerts Les certificats racines.
   * @return Le contexte TLS du client.
   *
   * @par Tâches
   *      S.O.
   */
  public static SslContext createForClient(String mode, X509Certificate certificate,
      PrivateKey privateKey, List<X509Certificate> rootCerts) {
    try {
      var builder = SslContextBuilder.forClient().protocols("TLSv1.2", "TLSv1.3");
      if (mode.equals("insecure")) {
        builder = builder.trustManager(InsecureTrustManagerFactory.INSTANCE);

        logger.warn(Warn.VARIABLE_LOGGING_1, "trustManagerFactory",
            "Insecure TLS server context. Use only for testing.");
      } else if (mode.equals("standard") || mode.equals("mTLS")) {
        if (rootCerts.isEmpty()) {
          builder = builder.trustManager(JavaTrustStore.getDefaultTrustFactoryManager());
        } else {
          builder = builder.trustManager(rootCerts);
        }

        if (mode.equals("mTLS")) {
          builder = builder.keyManager(privateKey, certificate);
        }
      } else {
        throw new ProtocolException("Unknown tls mode: " + mode);
      }

      SslContext sslContext = builder.build();
      return sslContext;
    } catch (SSLException e) {
      throw new ProtocolException("Error initiating TLS context for client", e);
    }
  }
}
