package ca.griis.speds.communication.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.griis.speds.communication.protocol.unit.ProtocolIdu;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimplifiedHttpHostTest implements ProtocolHostEvent {
  private ProtocolHttpHost server;
  private ProtocolHttpHost client;
  private ObjectMapper objectMapper = new ObjectMapper();
  private LinkedBlockingQueue<ProtocolIdu> idus = new LinkedBlockingQueue<>();

  @BeforeEach
  public void init() {
    server = new ProtocolHttpHost(objectMapper, "localhost", 8080, 1048576, this);
    client = new ProtocolHttpHost(objectMapper, "localhost", 8081, 1048576, this);
  }

  @AfterEach
  public void teardown() {
    client.close();
    server.close();
  }

  @Test
  public void exchange() throws InterruptedException {
    for (int i = 0; i < 200; ++i) {
      final ProtocolIdu request = new ProtocolIdu("http://localhost:8080", "Allo");
      client.send(request);

      final ProtocolIdu indication = idus.poll(2, TimeUnit.SECONDS);
      assertEquals(indication.destinationUri(), "http://localhost:8080");
      assertEquals(indication.sdu(), request.sdu());
    }
  }

  @Override
  public void notifyIdu(ProtocolIdu event) {
    idus.add(event);
  }

  @Override
  public void notifyException(Exception exception) {}
}
