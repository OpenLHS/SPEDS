/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableSessionServer.
 * @brief @~english Implementation of the ImmutableSessionServer class.
 */

package ca.griis.speds.session.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.link.api.exception.ProtocolException;
import ca.griis.speds.session.api.SessionServer;
import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.Poller;
import ca.griis.speds.session.internal.service.ServerSession;
import ca.griis.speds.session.internal.service.seal.SealCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

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
 * @brief @~french Offre les services d'un server immutable de la couche session.
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
 *      2025-03-03 [FO] - Première ébauche.
 *
 * @par Tâches
 *      S.O.
 */
public final class ImmutableSessionServer implements SessionServer {
  private static final GriisLogger logger = getLogger(ImmutableSessionServer.class);

  private final ObjectMapper sharedMapper;
  private final ServerSession serverSession;

  public ImmutableSessionServer(HostStartupContext hostStartupContext, Poller poller) {
    this.sharedMapper = hostStartupContext.sharedMapper();
    this.serverSession = new ServerSession(hostStartupContext, poller, new SealCreator());
  }

  @Override
  public String indicateDataExchange() {
    return indication();
  }

  @Override
  public String indication() {
    logger.trace(Trace.ENTER_METHOD_0);
    Pidu pidu = this.serverSession.getPendingMessage();
    String serialPidu;
    try {
      serialPidu = sharedMapper.writeValueAsString(pidu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    return serialPidu;
  }

  @Override
  public void response(String idu) throws ProtocolException {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    Pidu pidu;
    try {
      pidu = sharedMapper.readValue(idu, Pidu.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Unable to read IDU", e);
    }

    serverSession.sendReply(pidu);
    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    serverSession.close();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void closePreservingSessionStates() {
    serverSession.closePreservingSessionStates();
  }

  @Override
  public void clearSessionStates() {
    serverSession.clearSessionStates();
  }

  public Map<SessionId, SessionInformation> getServerInfo() {
    return this.serverSession.getSessionInfo();
  }
}
