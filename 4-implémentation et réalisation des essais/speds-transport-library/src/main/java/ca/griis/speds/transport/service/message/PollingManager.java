/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe PollingManager.
 * @brief @~english PollingManager class implementation.
 */

package ca.griis.speds.transport.service.message;


import ca.griis.js2p.gen.speds.transport.api.dto.Header45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.InterfaceDataUnit45Dto;
import ca.griis.js2p.gen.speds.transport.api.dto.ProtocolDataUnit4TraDto;
import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Debug;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.network.api.NetworkHost;
import ca.griis.speds.transport.exception.DeserializationException;
import ca.griis.speds.transport.serializer.SharedObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * @brief @~french Récupère des messages de la couche transport reçu par la couche réseau.
 * @par Details
 *      Respecte l'architecture de producteur/consommateur.
 * 
 *      Le producteur est un fils d'exécution qui récupère des messages via la couche réseau, et les
 *      trie dans la bonne file bloquante.
 *      Ily a trois files : requêtes, réponses, et exceptions.
 * 
 *      Le consommateur est celui qui récupére les messages transports (requête et réponse).
 *      Le consommateur consomme des files de messages bloquantes. Quand il n'y pas de messages, il
 *      attend d'en reçevoir un.
 *      En cas d'exception du producteur, le consommateur le sait, et lève l'exception à celui qui
 *      consomme.
 * 
 * @par Modèle
 *      S.O.
 * @par Conception
 *      S.O.
 * @par Limites
 *      S.O.
 *
 * @par Historique
 *      2025-06-20 [FO] - Refactorisation majeure<br>
 *      2025-05-14 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 * @todo 2025-06-20 [FO]-- Revoir l'entièreté de cette approche, sa pertinence dans une itération
 *       prochaine .
 */
public final class PollingManager implements AutoCloseable {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(PollingManager.class);

  private final LinkedBlockingQueue<InterfaceDataUnit45Dto> requests;
  private final LinkedBlockingQueue<InterfaceDataUnit45Dto> responses;
  private final LinkedBlockingQueue<Optional<RuntimeException>> exceptions;
  private final NetworkHost networkHost;
  private final ExecutorService executorService;
  private final AtomicBoolean alreadyStarted;
  private final AtomicBoolean closed;

  public PollingManager(NetworkHost networkHost) {
    logger.trace(Trace.ENTER_METHOD_1, "networkHost", networkHost);

    this.requests = new LinkedBlockingQueue<>();
    this.responses = new LinkedBlockingQueue<>();
    this.exceptions = new LinkedBlockingQueue<>();
    this.networkHost = networkHost;
    this.executorService = Executors.newSingleThreadExecutor();
    this.alreadyStarted = new AtomicBoolean(false);
    this.closed = new AtomicBoolean(false);
  }

  @Override
  public void close() {
    logger.trace(Trace.ENTER_METHOD_0);

    this.closed.set(true);

    shutdown();

    requests.clear();
    responses.clear();

    logger.trace(Trace.EXIT_METHOD_0);
  }

  /**
   * 
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Consommae une requête.
   * @exception DeserializationException En cas quele dernier message reçu a été impossible à
   *            désérialiser.
   * @exception RuntimeException En cas d'une erreur d'interruption.
   *
   * @par Tâches
   *      S.O.
   */
  public InterfaceDataUnit45Dto pollRequest() {
    logger.trace(Trace.ENTER_METHOD_0);

    InterfaceDataUnit45Dto idu = poll(requests);

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }

  /**
   * 
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Consomme une réponse.
   * @exception DeserializationException En cas quele dernier message reçu a été impossible à
   *            désérialiser.
   * @exception RuntimeException En cas d'une erreur d'interruption.
   *
   * @par Tâches
   *      S.O.
   */
  public InterfaceDataUnit45Dto pollResponse() {
    logger.trace(Trace.ENTER_METHOD_0);

    InterfaceDataUnit45Dto idu = poll(responses);

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }

  /**
   * 
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Consomme un message.
   * @param queue Le file bloquante de messages.
   * @exception DeserializationException En cas que le dernier message reçu a été impossible à
   *            désérialiser.
   * @exception RuntimeException En cas d'une erreur d'interruption.
   *
   * @par Tâches
   *      S.O.
   */
  protected InterfaceDataUnit45Dto poll(LinkedBlockingQueue<InterfaceDataUnit45Dto> queue) {
    logger.trace(Trace.ENTER_METHOD_0);

    produce();

    InterfaceDataUnit45Dto idu;
    try {
      Optional<RuntimeException> exception = exceptions.take();
      if (exception.isPresent()) {
        throw exception.get();
      }

      idu = queue.take();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new RuntimeException("Thread interrupted while polling", e);
    }

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Ajoute des messages transport reçu à la file de messages.
   *
   * @note 2025-06-20 - En cas de problème de désérialisation, on est capable de récupérer
   *       l'exception.
   *
   * @par Tâches
   *      S.O.
   */
  protected void produce() {
    if (alreadyStarted.compareAndSet(false, true)) {
      executorService.execute(() -> {
        while (closed.get() == false) {
          String iduString = networkHost.indication();

          if (iduString == null) {
            DeserializationException exception = new DeserializationException("Unknown IDU");
            exceptions.add(Optional.of(exception));
          } else {
            final InterfaceDataUnit45Dto receivedIdu;
            final Header45Dto receivedHeader;
            try {
              receivedIdu = SharedObjectMapper.getInstance().getMapper()
                  .readValue(iduString, InterfaceDataUnit45Dto.class);

              final String receivedStringPdu = receivedIdu.getMessage();
              final ProtocolDataUnit4TraDto receivedPdu =
                  SharedObjectMapper.getInstance().getMapper()
                      .readValue(receivedStringPdu, ProtocolDataUnit4TraDto.class);
              receivedHeader = receivedPdu.getHeader();

              exceptions.add(Optional.empty());
              if (receivedHeader.getMsgtype().value().endsWith("ENV")) {
                requests.add(receivedIdu);
              } else {
                responses.add(receivedIdu);
              }
            } catch (JsonProcessingException e) {
              DeserializationException exception = new DeserializationException(e.getMessage());
              exceptions.add(Optional.of(exception));
            }
          }
          sleep();
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
      Thread.sleep(100);
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

    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.debug(Debug.VARIABLE_LOGGING_1, "e", e);

      executorService.shutdownNow();

      Thread.currentThread().interrupt();
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }
}
