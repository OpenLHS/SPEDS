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

package ca.griis.speds.session.internal.processing;


import ca.griis.logger.GriisLogger;
import ca.griis.logger.GriisLoggerFactory;
import ca.griis.logger.statuscode.Debug;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.session.api.exception.DeserializationException;
import ca.griis.speds.session.internal.handler.HandlerRegistry;
import ca.griis.speds.transport.api.TransportHost;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
 * @brief @~french Implémente une boucle d’attente active pour récupérer les messages.
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
 *      2025-05-14 [CB] - Implémentation initiale<br>
 *
 * @par Tâches
 *      S.O.
 */
public final class Poller {
  private static final GriisLogger logger = GriisLoggerFactory.getLogger(Poller.class);

  private final ExecutorService executor;
  private final AtomicBoolean running;
  private final MessageDispatcher dispatcher;
  private final TransportHost transportHost;
  private final Duration pollingInterval;
  private final AtomicReference<Future<?>> future;
  private final BlockingQueue<Exception> exceptions;

  public Poller(TransportHost transportHost, MessageDispatcher dispatcher,
      Duration pollingInterval) {
    logger.trace(Trace.ENTER_METHOD_3, "transportHost", transportHost, "dispatcher", dispatcher,
        "serverPollingInterval", pollingInterval);

    this.transportHost = transportHost;
    this.executor = Executors.newSingleThreadExecutor();
    this.running = new AtomicBoolean(false);
    this.dispatcher = dispatcher;
    this.pollingInterval = pollingInterval;
    this.future = new AtomicReference<>(null);
    this.exceptions = new LinkedBlockingDeque<>(10);
  }

  public Future<?> start() {
    logger.trace(Trace.ENTER_METHOD_0);
    if (running.compareAndSet(false, true)) {
      future.set(executor.submit(this::pollLoop));
    }

    Future<?> result = future.get();
    logger.trace(Trace.EXIT_METHOD_1, "result", result);
    return result;
  }

  public void stop() {
    logger.trace(Trace.ENTER_METHOD_0);
    running.set(false);
    shutdown();
    logger.trace(Trace.EXIT_METHOD_0);
  }

  private void pollLoop() {
    logger.trace(Trace.ENTER_METHOD_0);
    while (running.get()) {
      try {
        String msg = transportHost.dataReply();
        if (msg != null) {
          dispatcher.dispatch(msg);
        }

        if (running.get()) {
          Thread.sleep(pollingInterval.toMillis());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (DeserializationException e) {
        logger.error(Error.IGNORED_ERROR, e);
      } catch (Exception e) {
        if (!exceptions.offer(e)) {
          throw new RuntimeException(e);
        }
      }
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

    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.debug(Debug.VARIABLE_LOGGING_1, "e", e);

      executor.shutdownNow();

      Thread.currentThread().interrupt();
    }

    logger.trace(Trace.EXIT_METHOD_0);
  }

  public void registerHandlers(HandlerRegistry handlerRegistry) {
    this.dispatcher.registerHandlers(handlerRegistry);
  }

  public Exception getException() throws InterruptedException {
    return exceptions.take();
  }

  public void clearExceptions() {
    exceptions.clear();
  }
}
