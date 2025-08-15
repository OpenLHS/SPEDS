/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe Poller.
 * @brief @~english Poller class implementation.
 */

package ca.griis.speds.transport.service;

import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Debug;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import ca.griis.speds.transport.service.client.ExchangeDataConfirmation;
import ca.griis.speds.transport.service.server.ExchangeDataReply;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;

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
 * @brief @~french Traite les messages reçus de la couche transport.
 * @par Détails
 *      Respecte l'architecture de producteur/consommateur.
 * 
 *      Le producteur est un ou des fils d'exécution qui récupère des messages via la couche réseau,
 *      et traitent ceux-ci en fonction du type de message transport. Quand il y a des données à
 *      échanger avec le consommateur, il les ajoute à une file bloquante.
 * 
 *      Le consommateur est celui qui récupère les données, et c'est celui consomme la file de
 *      données bloquantes. Quand il n'y pas de données, il attend d'en reçevoir.
 * 
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-06-20 [FO] - Seconde refactorisation majeure<br>
 *      2025-06-20 [FO] - Refactorisation majeure<br>
 *      2025-05-14 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class Poller implements AutoCloseable {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(Poller.class);

  private static final DataReplyMessages poisonPill = new DataReplyMessages("", "");

  private final LinkedBlockingQueue<DataReplyMessages> data;
  private final NetworkHost networkHost;
  private final ExecutorService service;
  private final AtomicBoolean alreadyStarted;
  private final AtomicBoolean closed;
  private final ExchangeDataReply indication;
  private final ExchangeDataConfirmation confirm;
  private final Integer sleepMs;

  public Poller(NetworkHost networkHost, ExchangeDataReply indication,
      ExchangeDataConfirmation confirm, Integer maxQueueCapacity, Integer nbThreads,
      Integer sleepMs) {
    logger.trace(Trace.ENTER_METHOD_6, "networkHost", networkHost, "indication", indication,
        "confirm", confirm, "maxQueueCapacity", maxQueueCapacity, "nbThreads", nbThreads, "sleepMs",
        sleepMs);

    this.data = new LinkedBlockingQueue<>(maxQueueCapacity);
    this.networkHost = networkHost;
    this.service = Executors.newFixedThreadPool(nbThreads);
    this.alreadyStarted = new AtomicBoolean(false);
    this.closed = new AtomicBoolean(false);
    this.indication = indication;
    this.confirm = confirm;
    this.sleepMs = sleepMs;
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    closed.set(true);

    try {
      networkHost.close();
    } catch (Exception ex) {
      logger.error(Error.GENERIC_ERROR, ex);
    }

    shutdown();

    data.clear();

    try {
      data.put(poisonPill);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Consomme une donnée.
   * @param queue Le file bloquante de données.
   * @exception RuntimeException En cas d'une erreur d'interruption.
   *
   * @par Tâches
   *      S.O.
   */
  public DataReplyMessages poll() {
    logger.trace(Trace.ENTER_METHOD_0);

    start();

    DataReplyMessages result = null;
    try {
      result = data.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    if (result == null) {
      throw new IllegalStateException("BlockingQueue returned null, which should be impossible");
    }

    if (result.equals(poisonPill)) {
      throw new RuntimeException("Poison pill");
    }

    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french DÉmarre le traitement les messages de la couche transport reçus.
   *
   * @par Tâches
   *      S.O.
   */
  public void start() {
    if (alreadyStarted.compareAndSet(false, true)) {
      service.execute(() -> {
        while (closed.get() == false) {
          try {
            String idu = networkHost.indication();

            if (StringUtils.isNotEmpty(idu)) {
              try {
                final InterfaceDataUnit45Dto receivedIdu = SharedObjectMapper.getInstance()
                    .getMapper().readValue(idu, InterfaceDataUnit45Dto.class);

                final String pdu = receivedIdu.getMessage();
                final ProtocolDataUnit4TraDto receivedPdu =
                    SharedObjectMapper.getInstance().getMapper()
                        .readValue(pdu, ProtocolDataUnit4TraDto.class);
                final Header45Dto receivedHeader = receivedPdu.getHeader();
                final Header45Dto.Msgtype msgType = receivedHeader.getMsgtype();

                if (msgType.equals(Header45Dto.Msgtype.TRA_MSG_ENV)) {
                  DataReplyMessages result = indication.indication(receivedIdu);
                  data.put(result);
                } else if (msgType.equals(Header45Dto.Msgtype.TRA_MSG_REC)) {
                  confirm.confirm(receivedIdu);
                } else {
                  logger.error(Error.IGNORED_ERROR,
                      "PDU with this message type is ignored: " + receivedHeader.getId());
                }
              } catch (SilentIgnoredException ex) {
                logger.error(Error.IGNORED_ERROR, ex);
              } catch (JsonProcessingException | InterruptedException ex) {
                logger.error(Error.IGNORED_ERROR, ex);
              }
            }

            sleep();
          } catch (Exception ex) {
            logger.error(Error.IGNORED_ERROR, ex);
          }
        }
      });
    }
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Suspendre le producteur.
   *
   * @note 2025-06-20 - Cela est discutable, mais cela permet de laisser du répis aux autres fils
   *       d'exécution.
   *
   * @par Tâches
   *      S.O.
   */
  protected void sleep() {
    logger.trace(Trace.ENTER_METHOD_0);

    try {
      Thread.sleep(sleepMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Désalloue le producteur non bloquant.
   *
   * @note 2024-02-20 - Comment correctement gérer l'exception d'interruption durant la
   *       désallocation du service d'exécution:
   *       https://www.baeldung.com/java-executor-wait-for-threads
   *
   * @par Tâches
   *      S.O.
   */
  protected void shutdown() {
    logger.trace(Trace.ENTER_METHOD_0);

    service.shutdown();
    try {
      if (!service.awaitTermination(5, TimeUnit.SECONDS)) {
        service.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.debug(Debug.VARIABLE_LOGGING_1, "e", e);

      service.shutdownNow();

      Thread.currentThread().interrupt();
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
