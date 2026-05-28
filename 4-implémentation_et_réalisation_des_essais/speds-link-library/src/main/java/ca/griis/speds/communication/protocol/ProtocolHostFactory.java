
/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ProtocolHostFactory.
 * @brief @~english Implementation of the ProtocolHostFactory class.
 */

package ca.griis.speds.communication.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;

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
 * @brief @~french Offre un service de fabrique pour la création d'hôte du protocole de
 *        communication.
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
 *      2026-04-22 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ProtocolHostFactory {
  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Construit un hôte.
   * @param objectMapper Sérialisateur JSON.
   * @param address L'adresse rejoignable du serveur HTTPS.
   * @param port Port du serveur HTTPS.
   * @param sslClient Contexte TLS/SSL du client HTTPS.
   * @param sslServer Contexte TLS/SSL du serveur HTTPS.
   * @param maxContentLength Longueur maximale de contenu envoyé ou reçu.
   * @param hostEvent Classe qui reçoit les événements.
   * @return L'hôte construit.
   *
   * @par Tâches
   *      S.O.
   */
  public static ProtocolHost createHost(
      ObjectMapper objectMapper,
      String address,
      Integer port,
      SslContext sslClient,
      SslContext sslServer,
      Integer maxContentLength,
      ProtocolHostEvent hostEvent) {
    return new ProtocolHttpHost(objectMapper, address, port, sslClient, sslServer, maxContentLength,
        hostEvent);
  }
}
