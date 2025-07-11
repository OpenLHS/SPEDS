/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ImmutableSessionClient.
 * @brief @~english Implementation of the ImmutableSessionClient class.
 */

package ca.griis.speds.session.api.sync;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.js2p.gen.speds.session.api.dto.InterfaceDataUnit23Dto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.SessionClient;
import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.internal.contract.Pidu;
import ca.griis.speds.session.internal.domain.HostStartupContext;
import ca.griis.speds.session.internal.domain.SessionId;
import ca.griis.speds.session.internal.model.SessionInformation;
import ca.griis.speds.session.internal.processing.Poller;
import ca.griis.speds.session.internal.service.ClientSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
 * @brief @~french Offre les services d'un client immutable de la couche session.
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
public final class ImmutableSessionClient implements SessionClient {
  private static final GriisLogger logger = getLogger(ImmutableSessionClient.class);

  private static final InterfaceDataUnit23Dto poisonPill = new InterfaceDataUnit23Dto(null, null);

  private final ObjectMapper sharedMapper;
  private final LinkedBlockingQueue<InterfaceDataUnit23Dto> pids;
  private final ClientSession clientSession;
  private final ExecutorService executor;

  public ImmutableSessionClient(HostStartupContext hostStartupContext, Poller poller) {
    this.sharedMapper = hostStartupContext.sharedMapper();
    this.pids = new LinkedBlockingQueue<>();
    this.clientSession = new ClientSession(hostStartupContext, poller, pids);
    this.executor = Executors.newFixedThreadPool(10);
  }

  @Override
  public void request(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    requestFuture(idu);

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public CompletableFuture<Void> requestFuture(String idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);
    Pidu pidu;

    try {
      pidu = sharedMapper.readValue(idu, Pidu.class);
    } catch (JsonProcessingException e) {
      throw new DeserializationException("Message is not a PIDU", e);
    }

    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
      try {
        clientSession.sendMessageWithManagedSession(pidu);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }, executor);

    logger.trace(Trace.EXIT_METHOD_1, "future", future);
    return future;
  }

  @Override
  public String confirm() {
    logger.trace(Trace.ENTER_METHOD_0);
    String pidu;

    try {
      InterfaceDataUnit23Dto pendingConfirmation = take();
      pidu = sharedMapper.writeValueAsString(pendingConfirmation);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    return pidu;
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    try {
      if (pids.isEmpty()) {
        pids.put(poisonPill);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    clientSession.close();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  @Override
  public void closePreservingSessionStates() {
    clientSession.closePreservingSessionStates();
  }

  @Override
  public void clearSessionStates() {
    clientSession.clearSessionStates();
  }

  public void open() {
    logger.trace(Trace.ENTER_METHOD_0);

    // TODO
    // this.clientSession.loadSessionState();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public Map<SessionId, SessionInformation> getClientInfo() {
    return this.clientSession.getSessionInfo();
  }

  protected InterfaceDataUnit23Dto take() {
    InterfaceDataUnit23Dto idu = poisonPill;
    try {
      idu = pids.take();

      /**
       * @todo 2025-07-04 [FO] - Il est possible un scénario que la pilule soit reçue avant une
       *       réponse valide. On laisse une seconde chance seulement.
       * 
       *       Envisager une meilleure approche.
       */
      if (idu.equals(poisonPill)) {
        idu = pids.poll(500, TimeUnit.MILLISECONDS);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (idu.equals(poisonPill)) {
      throw new RuntimeException("Poison pill !!");
    }

    return idu;
  }
}
