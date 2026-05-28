/**
 * @file
 *
 * @copyright @@GRIIS_COPYRIGHT@@
 *
 * @licence @@GRIIS_LICENCE@@
 *
 * @version @@GRIIS_VERSION@@
 *
 * @brief @~french Implémentation de la classe ProtocolHttpHost.
 * @brief @~english Implementation of the ProtocolHttpHost class.
 */

package ca.griis.speds.communication.protocol;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Info;
import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import ca.griis.speds.communication.protocol.unit.ProtocolPdu;
import ca.griis.speds.link.api.exception.ProtocolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

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
 * @brief @~french Offre un hôte HTTP/2.0.
 * @par Details
 *      <p>
 *      Offre la possibilité d'agir comme client et serveur HTTP ou HTTPS.
 *      Ajout de délai d'expiration: de requête et réponse de 10 secondes, d'inactivité de 30.
 *      Aussi, un ajout d'un nombre maximal de flux de données concurrents de 100.
 *      </p>
 * 
 *      <p>
 *      Le patron fire-and-forget est privilégié dans ce contexte. L’architecture du protocole SPEDS
 *      délègue aux couches utilisatrices la responsabilité de gérer la réémission en cas de besoin.
 *      En cas d'exception, celle-ci est journalisée et un événement est délégué à l'écouteur de
 *      l'événement.
 *      </p>
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
final class ProtocolHttpHost implements ProtocolHost {
  private static final GriisLogger logger = getLogger(ProtocolHttpHost.class);

  private final HttpClient client;
  private final DisposableServer server;
  private final ObjectMapper objectMapper;
  private final ProtocolHostEvent hostEvent;
  private final Integer maxContentLengthBytes;
  private final CircuitBreaker circuitBreaker;

  ProtocolHttpHost(
      ObjectMapper objectMapper,
      String address,
      Integer port,
      SslContext sslClient,
      SslContext sslServer,
      Integer maxContentLength,
      ProtocolHostEvent hostEvent) {

    this.objectMapper = objectMapper;
    this.hostEvent = hostEvent;
    this.maxContentLengthBytes = maxContentLength;

    this.client = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(10))
        .protocol(HttpProtocol.H2)
        .secure(spec -> spec.sslContext(sslClient));

    this.server = HttpServer.create()
        .option(ChannelOption.SO_BACKLOG, 100)
        .http2Settings(spec -> spec.maxConcurrentStreams(100))
        .idleTimeout(Duration.ofSeconds(30))
        .requestTimeout(Duration.ofSeconds(10))
        .httpRequestDecoder(spec -> spec.maxHeaderSize(8192).maxInitialLineLength(4096))
        .host(address)
        .port(port)
        .secure(spec -> spec.sslContext(sslServer))
        .handle(this::handleRequest)
        .protocol(HttpProtocol.H2)
        .bindNow();

    this.circuitBreaker = createCircuitBreaker();
  }

  ProtocolHttpHost(
      ObjectMapper objectMapper,
      String address,
      Integer port,
      Integer maxContentLengthBytes,
      ProtocolHostEvent hostEvent) {

    this.objectMapper = objectMapper;
    this.hostEvent = hostEvent;
    this.maxContentLengthBytes = maxContentLengthBytes;

    this.client = HttpClient.create();

    this.server = HttpServer.create()
        .host(address)
        .port(port)
        .handle(this::handleRequest)
        .bindNow();

    this.circuitBreaker = createCircuitBreaker();
  }

  @Override
  public void send(ProtocolIdu idu) {
    if (idu.sdu().length() > maxContentLengthBytes) {
      throw new ProtocolException("Payload too large");
    }

    ProtocolPdu pdu = new ProtocolPdu(idu.destinationUri(), idu.sdu());
    final String content;
    try {
      content = objectMapper.writeValueAsString(pdu);
    } catch (JsonProcessingException e) {
      throw new ProtocolException("JSON serialization error", e);
    }

    URI uri = URI.create(idu.destinationUri());

    client.post()
        .uri(uri)
        .send(ByteBufFlux.fromString(Mono.just(content)))
        .responseSingle((res, buf) -> {
          return buf.asString().defaultIfEmpty("").flatMap(body -> {
            Mono<String> result = Mono.just(body);

            Integer code = res.status().code();
            if (code < 200 || code >= 300) {
              var ex = new ProtocolException("The client received HTTP " + res.status().code());
              logger.error(Error.GENERIC_ERROR, ex);
              result = Mono.error(ex);
            }

            String contentType = res.responseHeaders().get("Content-Type");
            if (contentType == null || !contentType.toLowerCase().contains("application/json")) {
              var ex = new ProtocolException("Invalid or missing Content-Type: " + contentType);
              logger.error(Error.GENERIC_ERROR, ex);
              result = Mono.error(ex);
            }

            return result;
          });
        })
        .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
        .subscribe(
            ok -> logger.info(Info.VARIABLE_LOGGING_2, "state", "HTTP message sent", "uri", uri),
            err -> {
              Exception exception;
              if (err instanceof CallNotPermittedException) {
                exception = new ProtocolException("Circuit breaker OPEN for URI: " + uri);
              } else if (err instanceof Exception e) {
                exception = e;
              } else {
                exception = new ProtocolException("Unexpected error: " + err.getMessage(), err);
              }

              logger.error(Error.GENERIC_ERROR, exception);
              hostEvent.notifyException(exception);
            });
  }

  @Override
  public void close() {
    server.disposeNow();
  }

  private void notifyContent(String content) {
    try {
      ProtocolPdu pdu = objectMapper.readValue(content, ProtocolPdu.class);
      ProtocolIdu idu = new ProtocolIdu(
          pdu.remoteAddr(),
          pdu.sdu());

      hostEvent.notifyIdu(idu);
    } catch (JsonProcessingException e) {
      logger.error(Error.IGNORED_ERROR, e);
    }
  }

  private Mono<Void> handleRequest(HttpServerRequest request, HttpServerResponse response) {
    if (!"POST".equalsIgnoreCase(request.method().name())) {

      ProtocolException ex =
          new ProtocolException("The received HTTP request is not a POST request.");
      logger.error(Error.IGNORED_ERROR, ex);
      return response.status(405).send();
    }

    AtomicLong size = new AtomicLong();

    return ByteBufFlux.fromInbound(
        request.receive()
            .timeout(Duration.ofSeconds(5))
            .doOnNext(buf -> {
              Long total = size.addAndGet(buf.readableBytes());
              if (total > maxContentLengthBytes) {
                throw new ProtocolException("Payload too large for the server");
              }
            }))
        .aggregate()
        .asString(StandardCharsets.UTF_8)
        .flatMap(body -> {
          notifyContent(body);

          logger.info(Info.VARIABLE_LOGGING_3,
              "state", "HTTP request received and sending response",
              "request.remoteAddress()", request.remoteAddress(),
              "response.remoteAddress()", response.remoteAddress());

          response
              .header("Content-Type", "application/json")
              .header("X-Content-Type-Options", "nosniff")
              .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
              .header("Cache-Control", "no-store");
          return response.sendString(Mono.just("\"OK\"")).then();
        })
        .onErrorResume(ProtocolException.class, e -> {
          logger.error(Error.IGNORED_ERROR, e);

          return response
              .header("Content-Type", "application/json")
              .header("X-Content-Type-Options", "nosniff")
              .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
              .status(413).send();
        })
        .onErrorResume(e -> {
          logger.error(Error.IGNORED_ERROR, e);
          return response
              .header("Content-Type", "application/json")
              .header("X-Content-Type-Options", "nosniff")
              .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
              .status(500).send();
        });
  }

  private CircuitBreaker createCircuitBreaker() {
    CircuitBreakerConfig config =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();

    CircuitBreaker cb = CircuitBreaker.of("protocol-http-client", config);
    cb.getEventPublisher()
        .onStateTransition(event -> logger.info(Info.VARIABLE_LOGGING_2, "state",
            "Circuit breaker transition", "transition", event.getStateTransition()))
        .onFailureRateExceeded(event -> logger.error(
            Error.GENERIC_ERROR,
            new ProtocolException("Circuit breaker failure rate exceeded")));
    return cb;
  }
}
