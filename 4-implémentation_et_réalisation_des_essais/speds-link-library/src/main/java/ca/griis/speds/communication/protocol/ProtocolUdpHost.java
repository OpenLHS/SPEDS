
package ca.griis.speds.communication.protocol;

import static ca.griis.logger.GriisLoggerFactory.getLogger;

import ca.griis.logger.GriisLogger;
import ca.griis.logger.statuscode.Error;
import ca.griis.logger.statuscode.Trace;
import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import ca.griis.speds.communication.protocol.unit.ProtocolPdu;
import ca.griis.speds.link.api.exception.ProtocolException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;

final class ProtocolUdpHost implements ProtocolHost {
  private static final GriisLogger logger = getLogger(ProtocolUdpHost.class);

  private final reactor.netty.udp.UdpClient client;
  private final reactor.netty.udp.UdpServer server;
  private final reactor.netty.Connection disposableServer;
  private final ObjectMapper objectMapper;
  private final ProtocolHostEvent hostEvent;

  ProtocolUdpHost(ObjectMapper objectMapper, String address, Integer port,
      ProtocolHostEvent hostEvent) {
    this.client = reactor.netty.udp.UdpClient.create();
    this.server = reactor.netty.udp.UdpServer.create();
    this.disposableServer = server
        .host(address)
        .port(port)
        .handle(
            (request, response) -> request.receive().asString().doOnNext(this::notifyContent)
                .then())
        .bindNow();

    this.objectMapper = objectMapper;
    this.hostEvent = hostEvent;
  }

  @Override
  public void close() {
    disposableServer.disposeNow();
  }

  @Override
  public void send(ProtocolIdu idu) {
    logger.trace(Trace.ENTER_METHOD_1, "idu", idu);

    ProtocolPdu pdu = new ProtocolPdu(idu.destinationUri(), idu.sdu());
    String content;
    try {
      content = objectMapper.writeValueAsString(pdu);
    } catch (JsonProcessingException e) {
      throw new ProtocolException("Json processing error", e);
    }

    URI uri = URI.create(idu.destinationUri());
    client
        .host(uri.getHost())
        .port(uri.getPort())
        .handle(
            (inbound, outbound) -> outbound.send(ByteBufFlux.fromString(Mono.just(content))).then())
        .connect()
        .doOnError(e -> logger.error(Error.IGNORED_ERROR, e))
        .subscribe();
  }

  protected void notifyContent(String content) {
    logger.trace(Trace.ENTER_METHOD_1, "content", content);

    ProtocolPdu pdu;
    try {
      pdu = objectMapper.readValue(content, ProtocolPdu.class);
      ProtocolIdu idu = new ProtocolIdu(pdu.remoteAddr(), pdu.sdu());
      hostEvent.notifyIdu(idu);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
