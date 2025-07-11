/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableDataLinkHost.
 * @brief @~english Implementation of the ImmutableDataLinkHost class.
 */

package ca.griis.speds.link.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.ProtocolIdu;
import ca.griis.speds.communication.protocol.https.HttpsHost;
import ca.griis.speds.link.api.DataLinkHost;
import com.fasterxml.jackson.databind.ObjectMapper;

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
 * @brief @~french Offre les services d'un hôte de la couche liaison.
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
 *      2025-02-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableDataLinkHost implements DataLinkHost {
  private static final GriisLogger logger = getLogger(ImmutableDataLinkHost.class);

  private final HttpsHost host;
  private final ImmutableDataLinkClient client;
  private final ImmutableDataLinkServer server;

  public ImmutableDataLinkHost(ObjectMapper objectMapper, HttpsHost host) {
    this.host = host;
    this.client = new ImmutableDataLinkClient(objectMapper);
    this.server = new ImmutableDataLinkServer(objectMapper);
  }

  @Override
  public void close() {
    client.close();
    server.close();
    host.close();
  }

  @Override
  public String request(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    ProtocolIdu protocolIdu = client.request(idu);
    host.request(protocolIdu);
    String sdu = protocolIdu.sdu();

    logger.trace(Trace.EXIT_METHOD_1, "sdu", sdu);
    return sdu;
  }

  @Override
  public String confirm() {
    logger.trace(Trace.ENTER_METHOD_0);

    ProtocolIdu protocolIdu = host.confirm();
    String iduJson = client.confirm(protocolIdu);

    logger.trace(Trace.EXIT_METHOD_1, "iduJson", iduJson);
    return iduJson;
  }

  @Override
  public String indication() {
    logger.trace(Trace.ENTER_METHOD_0);

    ProtocolIdu protocolIdu = host.indicate();
    String iduJson = server.indication(protocolIdu);

    logger.trace(Trace.EXIT_METHOD_1, "iduJson", iduJson);
    return iduJson;
  }

  @Override
  public String response(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    ProtocolIdu protocolIdu = server.response(idu);
    host.response(protocolIdu);
    String sdu = protocolIdu.sdu();

    logger.trace(Trace.EXIT_METHOD_1, "sdu", sdu);
    return sdu;
  }

  public HttpsHost getHost() {
    return host;
  }

  public ImmutableDataLinkClient getClient() {
    return client;
  }

  public ImmutableDataLinkServer getServer() {
    return server;
  }
}
