
package ca.griis.speds.communication.protocol.https;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.ProtocolHost;
import ca.griis.speds.communication.protocol.ProtocolIdu;
import ca.griis.speds.communication.protocol.https.HttpPdu.Type;
import ca.griis.speds.link.api.exception.ProtocolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import java.util.concurrent.LinkedBlockingDeque;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.DisposableServer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.server.HttpServer;

public final class HttpsHost implements ProtocolHost {
  private static final GriisLogger logger = getLogger(HttpsHost.class);

  private final HttpClient client;
  private final HttpServer server;
  private final DisposableServer disposableServer;
  private final LinkedBlockingDeque<HttpPdu> requests;
  private final LinkedBlockingDeque<HttpPdu> responses;

  private final ObjectMapper objectMapper;

  public HttpsHost(ObjectMapper objectMapper, String address, Integer port, SslContext sslClient,
      SslContext sslServer) {
    this.server = HttpServer.create();
    this.client = HttpClient.create()
        .secure(spec -> spec.sslContext(sslClient));
    this.disposableServer = server
        .host(address)
        .port(port)
        .secure(spec -> spec.sslContext(sslServer), true)
        .handle(
            (request, response) -> request.receive().aggregate().asString().doOnNext(this::receive)
                .then())
        .bindNow();

    this.requests = new LinkedBlockingDeque<>();
    this.responses = new LinkedBlockingDeque<>();
    this.objectMapper = objectMapper;
  }

  @Override
  public void close() {
    disposableServer.disposeNow();
    requests.clear();
    responses.clear();
  }

  @Override
  public void request(ProtocolIdu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);
    HttpPdu pdu =
        new HttpPdu(idu.destinationUri(), Type.request, idu.messageIdentifier(), idu.sdu());

    String content = null;
    try {
      content = objectMapper.writeValueAsString(pdu);
    } catch (JsonProcessingException e) {
      throw new ProtocolException("Json processing error", e);
    }

    client
        .post()
        .uri(idu.destinationUri())
        .send(ByteBufFlux.fromString(Mono.just(content)))
        .response()
        .block();
  }

  @Override
  public ProtocolIdu confirm() {

    HttpPdu pdu = null;
    try {
      pdu = responses.take();
    } catch (InterruptedException e) {
      throw new ProtocolException("Interrupted thread error", e);
    }

    ProtocolIdu idu = new ProtocolIdu(pdu.remoteAddr(), pdu.id(), pdu.sdu());

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }

  @Override
  public void response(ProtocolIdu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    HttpPdu pdu =
        new HttpPdu(idu.destinationUri(), Type.response, idu.messageIdentifier(), idu.sdu());

    String content = null;
    try {
      content = objectMapper.writeValueAsString(pdu);
    } catch (JsonProcessingException e) {
      throw new ProtocolException("Json processing error", e);
    }

    client
        .post()
        .uri(idu.destinationUri())
        .send(ByteBufFlux.fromString(Mono.just(content)))
        .response()
        .block();
  }

  @Override
  public ProtocolIdu indicate() {

    HttpPdu pdu = null;
    try {
      pdu = requests.take();
    } catch (InterruptedException e) {
      throw new ProtocolException("Interrupted thread error", e);
    }

    ProtocolIdu idu = new ProtocolIdu(pdu.remoteAddr(), pdu.id(), pdu.sdu());

    logger.trace(Trace.EXIT_METHOD_1, "idu", idu);
    return idu;
  }

  /**
   * @brief @~english «Description of the function»
   * @param «parameter name» «Parameter description»
   * @exception «exception name» «Exception description»
   * @return «Return description»
   *
   * @brief @~french Recevoir le message de reponse HTTPS.
   * @param content Le message de reponse HTTPS
   *
   *
   * @par Tâches
   *      S.O.
   */
  private void receive(String content) {
    logger.trace(Trace.ENTER_METHOD_1, "content", content);

    HttpPdu pdu;
    try {
      pdu = objectMapper.readValue(content, HttpPdu.class);
      if (pdu.type() == HttpPdu.Type.request) {
        requests.add(pdu);
      } else {
        responses.add(pdu);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public DisposableServer getDisposableServer() {
    return disposableServer;
  }
}
